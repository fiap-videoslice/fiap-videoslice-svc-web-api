package com.example.fiap.videosliceapi.adapters.datasource;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.valueobjects.JobProgress;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.intellij.lang.annotations.Language;
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
                                                 "(job_id,input_file_uri,slice_interval_seconds,status,start_time,user_email) " +
                                                 "values (?,?,?,?,?,?)";

    @Language("SQL")
    private final static String SQL_SELECT_JOBS_BY_EMAIL = "select " +
                                                           " job_id,input_file_uri,slice_interval_seconds,status,progress_current," +
                                                           " progress_total,output_file_uri,error_message,start_time,end_time,user_email " +
                                                           "from slice_job where user_email = ?";

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
            stmt.setString(6, job.userEmail());

            int ret = stmt.executeUpdate();
            if (ret != 1)
                throw new IllegalStateException("Inconsistent insert result: " + ret);

        } catch (SQLException e) {
            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Job> findAllByUserEmail(String email) {
        try (var connection = databaseConnection.getConnection();
             var stmt = connection.prepareStatement(SQL_SELECT_JOBS_BY_EMAIL)) {

            stmt.setString(1, email);

            List<Job> result = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int progressCurrent = rs.getInt("progress_current");
                    int progressTotal = rs.getInt("progress_total");
                    Job job = new Job(
                            rs.getObject("job_id", UUID.class),
                            rs.getString("input_file_uri"),
                            rs.getInt("slice_interval_seconds"),
                            JobStatus.valueOf(rs.getString("status")),
                            progressCurrent == 0 && progressTotal == 0 ? null : new JobProgress(
                                    progressCurrent,
                                    progressTotal
                            ),
                            rs.getString("output_file_uri"),
                            rs.getString("error_message"),
                            rs.getTimestamp("start_time").toInstant(),
                            (rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toInstant() : null),
                            rs.getString("user_email")
                    );
                    result.add(job);
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
        }
    }
}
