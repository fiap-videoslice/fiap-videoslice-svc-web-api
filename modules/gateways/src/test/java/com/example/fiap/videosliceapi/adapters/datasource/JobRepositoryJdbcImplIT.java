package com.example.fiap.videosliceapi.adapters.datasource;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import com.example.fiap.videosliceapi.testUtils.RealDatabaseTestHelper;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JobRepositoryJdbcImplIT {
    private static RealDatabaseTestHelper realDatabase;
    private DatabaseConnection databaseConnection;

    private JobRepositoryJdbcImpl repository;

    @BeforeAll
    static void beforeAll() throws Exception {
        realDatabase = new RealDatabaseTestHelper();
        realDatabase.beforeAll();
    }

    @AfterAll
    static void afterAll() {
        realDatabase.afterAll();
    }

    @BeforeEach
    void setUp() throws Exception {
        databaseConnection = realDatabase.getConnectionPool();
        repository = new JobRepositoryJdbcImpl(databaseConnection);
    }

    @AfterEach
    void tearDown() {
        databaseConnection.close();
    }

    @Test
    void saveNewJob() throws SQLException {
        UUID uuid = UUID.fromString("2e08ad6d-1c29-4d50-950e-7b7011c9f484");
        Instant createdAt = Instant.ofEpochMilli(1736297743000L);

        Job job = Job.createJob(uuid, "/input/2e08ad6d-1c29-4d50-950e-7b7011c9f484.mp4", 5,
                createdAt, "Test-User-Abc");

        repository.saveNewJob(job);

        verifyJobFromDatabase(uuid, (ResultSet rs) -> {
            assertThat(rs.getObject("job_id", UUID.class)).isEqualTo(uuid);
            assertThat(rs.getString("input_file_uri")).isEqualTo("/input/2e08ad6d-1c29-4d50-950e-7b7011c9f484.mp4");
            assertThat(rs.getInt("slice_interval_seconds")).isEqualTo(5);
            assertThat(rs.getString("status")).isEqualTo("CREATED");
            assertThat(rs.getTimestamp("start_time").toInstant()).isEqualTo(createdAt);
            assertThat(rs.getString("user_id")).isEqualTo("Test-User-Abc");
        });
    }

    @Test
    void findAllByUserId_created_and_complete_examples() throws SQLException {
        List<Job> jobs = repository.findAllByUserId("Test_User_1");

        assertThat(jobs).hasSize(2);

        Job job1 = jobs.get(0);
        assertThat(job1.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(job1.inputFileUri()).isEqualTo("/input/123e4567-e89b-12d3-a456-426614174000.mp4");
        assertThat(job1.sliceIntervalSeconds()).isEqualTo(3);
        assertThat(job1.status()).isEqualTo(JobStatus.CREATED);
        assertThat(job1.outputFileUri()).isNull();
        assertThat(job1.errorMessage()).isNull();
        assertThat(job1.startTime()).isEqualTo(Instant.parse("2024-01-09T15:30:12Z"));
        assertThat(job1.endTime()).isNull();
        assertThat(job1.userId()).isEqualTo("Test_User_1");

        Job job2 = jobs.get(1);
        assertThat(job2.id()).isEqualTo(UUID.fromString("6c9dcf45-15e5-4ab9-babe-7fe089194beb"));
        assertThat(job2.inputFileUri()).isEqualTo("/input/6c9dcf45-15e5-4ab9-babe-7fe089194beb.mp4");
        assertThat(job2.sliceIntervalSeconds()).isEqualTo(5);
        assertThat(job2.status()).isEqualTo(JobStatus.COMPLETE);
        assertThat(job2.outputFileUri()).isEqualTo("/output/6c9dcf45-15e5-4ab9-babe-7fe089194beb.zip");
        assertThat(job2.errorMessage()).isNull();
        assertThat(job2.startTime()).isEqualTo(Instant.parse("2024-01-08T20:00:00Z"));
        assertThat(job2.endTime()).isEqualTo(Instant.parse("2024-01-08T22:45:46Z"));
        assertThat(job2.userId()).isEqualTo("Test_User_1");
    }

    @Test
    void findAllByUserId_processing_and_failed_examples() throws SQLException {
        List<Job> jobs = repository.findAllByUserId("Test_User_2");

        assertThat(jobs).hasSize(2);

        Job job1 = jobs.get(0);
        assertThat(job1.id()).isEqualTo(UUID.fromString("223e4567-e89b-42d3-a456-426614174001"));
        assertThat(job1.inputFileUri()).isEqualTo("/input/223e4567-e89b-42d3-a456-426614174001.mp4");
        assertThat(job1.sliceIntervalSeconds()).isEqualTo(4);
        assertThat(job1.status()).isEqualTo(JobStatus.PROCESSING);
        assertThat(job1.outputFileUri()).isNull();
        assertThat(job1.errorMessage()).isNull();
        assertThat(job1.startTime()).isEqualTo(Instant.parse("2024-01-09T15:20:52Z"));
        assertThat(job1.endTime()).isNull();
        assertThat(job1.userId()).isEqualTo("Test_User_2");

        Job job2 = jobs.get(1);
        assertThat(job2.id()).isEqualTo(UUID.fromString("a31f6b5e-0d4e-4070-9fc9-f9cc5e5c61b1"));
        assertThat(job2.inputFileUri()).isEqualTo("/input/a31f6b5e-0d4e-4070-9fc9-f9cc5e5c61b1.mp4");
        assertThat(job2.sliceIntervalSeconds()).isEqualTo(6);
        assertThat(job2.status()).isEqualTo(JobStatus.FAILED);
        assertThat(job2.outputFileUri()).isNull();
        assertThat(job2.errorMessage()).isEqualTo("The video file is invalid");
        assertThat(job2.startTime()).isEqualTo(Instant.parse("2024-01-08T10:00:00Z"));
        assertThat(job2.endTime()).isEqualTo(Instant.parse("2024-01-08T10:00:01Z"));
        assertThat(job2.userId()).isEqualTo("Test_User_2");
    }

    @Test
    void findById_forUpdate_false() throws SQLException {
        Job job = repository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), false);

        assertThat(job).isNotNull();
        assertThat(job.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(job.inputFileUri()).isEqualTo("/input/123e4567-e89b-12d3-a456-426614174000.mp4");
        assertThat(job.sliceIntervalSeconds()).isEqualTo(3);
        assertThat(job.status()).isEqualTo(JobStatus.CREATED);
        assertThat(job.outputFileUri()).isNull();
        assertThat(job.errorMessage()).isNull();
        assertThat(job.startTime()).isEqualTo(Instant.parse("2024-01-09T15:30:12Z"));
        assertThat(job.endTime()).isNull();
        assertThat(job.userId()).isEqualTo("Test_User_1");
    }

    @Test
    void findById_notFound() throws SQLException {
        Job job = repository.findById(UUID.fromString("e60d41dd-074f-413c-8550-892b4f60f465"), true);

        assertThat(job).isNull();
    }

    @Test
    void updateMutableAttributes_withSuccessOutput() throws SQLException {
        UUID uuid = UUID.fromString("9cdda85e-f56e-4b5f-81f6-031145a4f43e");
        Instant createdAt = Instant.ofEpochMilli(1737227558034L);

        Job job = Job.createJob(uuid, "/input/9cdda85e-f56e-4b5f-81f6-031145a4f43e.mp4", 15,
                createdAt, "user3_Test");

        repository.saveNewJob(job);

        Job updated = job.completeProcessing("/output/9cdda85e-f56e-4b5f-81f6-031145a4f43e.zip", createdAt.plusSeconds(10));

        //
        repository.updateMutableAttributes(updated);

        verifyJobFromDatabase(uuid, (ResultSet rs) -> {
            assertThat(rs.getObject("job_id", UUID.class)).isEqualTo(uuid);
            assertThat(rs.getString("input_file_uri")).isEqualTo("/input/9cdda85e-f56e-4b5f-81f6-031145a4f43e.mp4");
            assertThat(rs.getInt("slice_interval_seconds")).isEqualTo(15);
            assertThat(rs.getString("status")).isEqualTo("COMPLETE");
            assertThat(rs.getTimestamp("start_time").toInstant()).isEqualTo(createdAt);
            assertThat(rs.getString("user_id")).isEqualTo("user3_Test");
            assertThat(rs.getTimestamp("end_time").toInstant()).isEqualTo(createdAt.plusSeconds(10));
            assertThat(rs.getString("output_file_uri")).isEqualTo("/output/9cdda85e-f56e-4b5f-81f6-031145a4f43e.zip");
            assertThat(rs.getString("error_message")).isNull();
        });
    }

    @Test
    void updateMutableAttributes_withErrorMessage() throws SQLException {
        UUID uuid = UUID.fromString("083a7d53-7ae3-4364-992e-6ecef7c2ba85");
        Instant createdAt = Instant.ofEpochMilli(1737227558034L);

        Job job = Job.createJob(uuid, "/input/083a7d53-7ae3-4364-992e-6ecef7c2ba85.mp4", 15,
                createdAt, "user3_Test");

        repository.saveNewJob(job);

        Job updated = job.errorProcessing("Video is invalid", createdAt.plusSeconds(10));

        //
        repository.updateMutableAttributes(updated);

        verifyJobFromDatabase(uuid, (ResultSet rs) -> {
            assertThat(rs.getString("status")).isEqualTo("FAILED");
            assertThat(rs.getTimestamp("end_time").toInstant()).isEqualTo(createdAt.plusSeconds(10));
            assertThat(rs.getString("output_file_uri")).isNull();
            assertThat(rs.getString("error_message")).isEqualTo("Video is invalid");
        });
    }

    @Test
    public void verifyJobFromDatabaseSanityCheck() throws SQLException {
        /*
        Make sure that the test utility method is doing what it is supposed to
         */
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        AtomicBoolean verificationsRun = new AtomicBoolean(false);

        verifyJobFromDatabase(uuid, rs -> {
            assertThat(rs.getObject("job_id", UUID.class)).isEqualTo(uuid);
            verificationsRun.set(true);
        });

        assertThat(verificationsRun).isTrue();
    }

    private void verifyJobFromDatabase(UUID uuid, ResultSetConsumer verifications) throws SQLException {
        try (var conn = databaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("select * from slice_job where job_id = ?");
            stmt.setObject(1, uuid);

            ResultSet rs = stmt.executeQuery();

            assertThat(rs.next()).isTrue();

            verifications.verifyRecord(rs);
        }
    }

    private interface ResultSetConsumer {
        void verifyRecord(ResultSet rs) throws SQLException;
    }
}