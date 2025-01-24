package com.example.fiap.videosliceapi.config;

import com.example.fiap.videosliceapi.adapters.auth.CognitoJwksApi;
import com.example.fiap.videosliceapi.adapters.auth.DefaultUserTokenParser;
import com.example.fiap.videosliceapi.adapters.auth.DummyTokenParser;
import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import com.example.fiap.videosliceapi.domain.external.NotificationSender;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import com.example.fiap.videosliceapi.domain.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DomainServiceBeans {
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainServiceBeans.class);

    @Bean
    public Clock clock() {
        return new Clock();
    }

    @Bean
    public LoggedUserTokenParser loggedUserTokenParser(Environment environment, CognitoJwksApi cognitoJwksApi) {
        if (environment.containsProperty(DummyTokenParser.ENABLE_DUMMY_TOKENS_ENV_KEY)
            && Boolean.parseBoolean(environment.getProperty(DummyTokenParser.ENABLE_DUMMY_TOKENS_ENV_KEY))) {

            LOGGER.warn("""
                    -------------------------------------------------------------------
                    DUMMY AUTHENTICATION TOKENS FOR DEVELOPMENT ENVIRONMENT ARE ENABLED
                    -------------------------------------------------------------------
                    """);
            return new DummyTokenParser();
        }

        return new DefaultUserTokenParser(cognitoJwksApi);
    }

    @Bean
    public JobUseCases jobUseCases(JobRepository jobRepository,
                                   VideoEngineService videoEngineService,
                                   MediaStorage mediaStorage,
                                   NotificationSender notificationSender,
                                   Clock clock) {
        return new JobUseCases(jobRepository, videoEngineService, mediaStorage, notificationSender, clock, new IdGenerator());
    }

}
