package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;

public interface NotificationSender {
    /**
     * @param job Job that must be in a final state (COMPLETE or FAILED)
     * @param downloadLink Only applies when a job is finished successfully
     */
    void sendFinishedJobNotification(Job job, DownloadLink downloadLink);
}
