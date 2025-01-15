package com.example.fiap.videosliceapi.adapters.datasource;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.valueobjects.JobProgress;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import com.example.fiap.videosliceapi.testUtils.RealDatabaseTestHelper;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobRepositoryJdbcImplIT {
    private static RealDatabaseTestHelper realDatabase;
    private DatabaseConnection databaseConnection;

    private JobRepositoryJdbcImpl repository;

    private static final UUID ID_1 = UUID.fromString("2e08ad6d-1c29-4d50-950e-7b7011c9f484");
    private static final Instant INSTANT_1 = Instant.ofEpochMilli(1736297743000L);

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
        Job job = Job.createJob(ID_1, "/input/2e08ad6d-1c29-4d50-950e-7b7011c9f484.mp4", 5,
                INSTANT_1, "user1@example");

        repository.saveNewJob(job);

        try (var conn = databaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("select * from slice_job where job_id = ?");
            stmt.setObject(1, ID_1);

            ResultSet rs = stmt.executeQuery();

            assertThat(rs.next()).isTrue();

            assertThat(rs.getObject("job_id", UUID.class)).isEqualTo(ID_1);
            assertThat(rs.getString("input_file_uri")).isEqualTo("/input/2e08ad6d-1c29-4d50-950e-7b7011c9f484.mp4");
            assertThat(rs.getInt("slice_interval_seconds")).isEqualTo(5);
            assertThat(rs.getString("status")).isEqualTo("CREATED");
            assertThat(rs.getTimestamp("start_time").toInstant()).isEqualTo(INSTANT_1);
            assertThat(rs.getString("user_email")).isEqualTo("user1@example");
        }
    }

    @Test
    void findAllByUserEmail_created_and_finished_examples() throws SQLException {
        List<Job> jobs = repository.findAllByUserEmail("user1@fiap.example.com");

        assertThat(jobs).hasSize(2);

        Job job1 = jobs.get(0);
        assertThat(job1.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(job1.inputFileUri()).isEqualTo("/input/123e4567-e89b-12d3-a456-426614174000.mp4");
        assertThat(job1.sliceIntervalSeconds()).isEqualTo(3);
        assertThat(job1.status()).isEqualTo(JobStatus.CREATED);
        assertThat(job1.progress()).isNull();
        assertThat(job1.outputFileUri()).isNull();
        assertThat(job1.errorMessage()).isNull();
        assertThat(job1.startTime()).isEqualTo(Instant.parse("2024-01-09T15:30:12Z"));
        assertThat(job1.endTime()).isNull();
        assertThat(job1.userEmail()).isEqualTo("user1@fiap.example.com");

        Job job2 = jobs.get(1);
        assertThat(job2.id()).isEqualTo(UUID.fromString("6c9dcf45-15e5-4ab9-babe-7fe089194beb"));
        assertThat(job2.inputFileUri()).isEqualTo("/input/6c9dcf45-15e5-4ab9-babe-7fe089194beb.mp4");
        assertThat(job2.sliceIntervalSeconds()).isEqualTo(5);
        assertThat(job2.status()).isEqualTo(JobStatus.FINISHED);
        assertThat(job2.progress()).isEqualTo(new JobProgress(45, 45));
        assertThat(job2.outputFileUri()).isEqualTo("/output/6c9dcf45-15e5-4ab9-babe-7fe089194beb.zip");
        assertThat(job2.errorMessage()).isNull();
        assertThat(job2.startTime()).isEqualTo(Instant.parse("2024-01-08T20:00:00Z"));
        assertThat(job2.endTime()).isEqualTo(Instant.parse("2024-01-08T22:45:46Z"));
        assertThat(job2.userEmail()).isEqualTo("user1@fiap.example.com");
    }

    @Test
    void findAllByUserEmail_processing_and_failed_examples() throws SQLException {
        List<Job> jobs = repository.findAllByUserEmail("user2@fiap.example.com");

        assertThat(jobs).hasSize(2);

        Job job1 = jobs.get(0);
        assertThat(job1.id()).isEqualTo(UUID.fromString("223e4567-e89b-42d3-a456-426614174001"));
        assertThat(job1.inputFileUri()).isEqualTo("/input/223e4567-e89b-42d3-a456-426614174001.mp4");
        assertThat(job1.sliceIntervalSeconds()).isEqualTo(4);
        assertThat(job1.status()).isEqualTo(JobStatus.PROCESSING);
        assertThat(job1.progress()).isEqualTo(new JobProgress(4, 50));
        assertThat(job1.outputFileUri()).isNull();
        assertThat(job1.errorMessage()).isNull();
        assertThat(job1.startTime()).isEqualTo(Instant.parse("2024-01-09T15:20:52Z"));
        assertThat(job1.endTime()).isNull();
        assertThat(job1.userEmail()).isEqualTo("user2@fiap.example.com");

        Job job2 = jobs.get(1);
        assertThat(job2.id()).isEqualTo(UUID.fromString("a31f6b5e-0d4e-4070-9fc9-f9cc5e5c61b1"));
        assertThat(job2.inputFileUri()).isEqualTo("/input/a31f6b5e-0d4e-4070-9fc9-f9cc5e5c61b1.mp4");
        assertThat(job2.sliceIntervalSeconds()).isEqualTo(6);
        assertThat(job2.status()).isEqualTo(JobStatus.FAILED);
        assertThat(job2.progress()).isNull();
        assertThat(job2.outputFileUri()).isNull();
        assertThat(job2.errorMessage()).isEqualTo("The video file is invalid");
        assertThat(job2.startTime()).isEqualTo(Instant.parse("2024-01-08T10:00:00Z"));
        assertThat(job2.endTime()).isEqualTo(Instant.parse("2024-01-08T10:00:01Z"));
        assertThat(job2.userEmail()).isEqualTo("user2@fiap.example.com");
    }
}