package com.example.fiap.videosliceapi.adapters.datasource;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class JobRepositoryJdbcImpl implements JobRepository {
    @Language("SQL")
    private final static String SQL_INSERT_JOB = "insert into slice_job " +
                                                 "(job_id,input_file_uri,slice_interval_seconds,status,start_time,user_id) " +
                                                 "values (?,?,?,?,?,?)";

    @Language("SQL")
    private final static String SQL_SELECT_JOBS_BY_USER = "select " +
                                                          " job_id,input_file_uri,slice_interval_seconds,status," +
                                                          " output_file_uri,error_message,start_time,end_time,user_id " +
                                                          "from slice_job where user_id = ?";
    @Language("SQL")
    private final static String SQL_FIND_JOB_BY_ID = "select " +
                                                     " job_id,input_file_uri,slice_interval_seconds,status," +
                                                     " output_file_uri,error_message,start_time,end_time,user_id " +
                                                     "from slice_job where job_id = ?";

    @Language("SQL")
    private final static String SQL_UPDATE_JOB = "update slice_job " +
                                                 "set status = ?, output_file_uri = ?, error_message = ?, end_time = ? " +
                                                 "where job_id = ?";

    private final DatabaseConnection databaseConnection;

    public JobRepositoryJdbcImpl(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void saveNewJob(Job job) {
        try (var connection = databaseConnection.getConnection();
             var stmt = connection.prepareStatement(SQL_INSERT_JOB)) {

            stmt.setObject(1, job.id());
            stmt.setString(2, job.inputFileUri());
            stmt.setInt(3, job.sliceIntervalSeconds());
            stmt.setString(4, job.status().name());

            stmt.setTimestamp(5, java.sql.Timestamp.from(job.startTime()));
            stmt.setString(6, job.userId());

            int ret = stmt.executeUpdate();
            if (ret != 1)
                throw new IllegalStateException("Inconsistent insert result: " + ret);

        } catch (SQLException e) {
            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Job> findAllByUserId(String userId) {
        try (var connection = databaseConnection.getConnection();
             var stmt = connection.prepareStatement(SQL_SELECT_JOBS_BY_USER)) {

            stmt.setString(1, userId);

            List<Job> result = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Job job = mapJobFromRs(rs);
                    result.add(job);
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public Job findById(UUID id, boolean forUpdate) {
        String sql = SQL_FIND_JOB_BY_ID + (forUpdate ? " FOR UPDATE" : "");

        try (var connection = databaseConnection.getConnection();
             var stmt = connection.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapJobFromRs(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMutableAttributes(Job job) {
        try (var connection = databaseConnection.getConnection();
             var stmt = connection.prepareStatement(SQL_UPDATE_JOB)) {
    
            stmt.setString(1, job.status().name());
            stmt.setString(2, job.outputFileUri());
            stmt.setString(3, job.errorMessage());
            stmt.setTimestamp(4, job.endTime() != null ? java.sql.Timestamp.from(job.endTime()) : null);
            stmt.setObject(5, job.id());
    
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1)
                throw new IllegalStateException("Update failed, inconsistent result: " + rowsAffected);
        } catch (SQLException e) {
            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
        }
    }

    private static @NotNull Job mapJobFromRs(ResultSet rs) throws SQLException {
        return new Job(
                rs.getObject("job_id", UUID.class),
                rs.getString("input_file_uri"),
                rs.getInt("slice_interval_seconds"),
                JobStatus.valueOf(rs.getString("status")),
                rs.getString("output_file_uri"),
                rs.getString("error_message"),
                rs.getTimestamp("start_time").toInstant(),
                (rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toInstant() : null),
                rs.getString("user_id")
        );
    }
}
