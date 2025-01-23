package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.entities.Job;

public interface NotificationSender {
    void sendFinishedJobNotification(Job job);
}
