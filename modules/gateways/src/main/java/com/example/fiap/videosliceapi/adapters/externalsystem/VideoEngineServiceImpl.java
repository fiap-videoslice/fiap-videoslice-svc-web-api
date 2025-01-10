package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.SliceJob;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import org.springframework.stereotype.Service;

@Service
public class VideoEngineServiceImpl implements VideoEngineService {
    @Override
    public void startProcess(SliceJob sliceJob) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
