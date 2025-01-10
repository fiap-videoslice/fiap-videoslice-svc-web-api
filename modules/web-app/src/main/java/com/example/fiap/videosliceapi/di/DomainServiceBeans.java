package com.example.fiap.videosliceapi.di;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecases.SliceJobUseCases;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceBeans {

    @Bean
    public Clock clock() {
        return new Clock();
    }

    @Bean
    public SliceJobUseCases sliceJobUseCases(JobRepository jobRepository,
                                             VideoEngineService videoEngineService) {
        return new SliceJobUseCases(jobRepository, videoEngineService);
    }

}
