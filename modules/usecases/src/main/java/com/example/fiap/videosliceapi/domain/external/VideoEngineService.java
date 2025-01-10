package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.entities.SliceJob;

public interface VideoEngineService {
    void startProcess(SliceJob sliceJob);
}
