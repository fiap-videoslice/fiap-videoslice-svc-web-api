package com.example.fiap.videosliceapi.domain.datagateway;

import com.example.fiap.videosliceapi.domain.entities.SliceJob;

public interface JobRepository {
    SliceJob saveNewJob(SliceJob sliceJob);
}
