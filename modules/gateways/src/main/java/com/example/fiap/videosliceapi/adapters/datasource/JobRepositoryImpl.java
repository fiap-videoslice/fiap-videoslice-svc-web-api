package com.example.fiap.videosliceapi.adapters.datasource;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.SliceJob;
import org.springframework.stereotype.Repository;

@Repository
public class JobRepositoryImpl implements JobRepository {
    @Override
    public SliceJob saveNewJob(SliceJob sliceJob) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
