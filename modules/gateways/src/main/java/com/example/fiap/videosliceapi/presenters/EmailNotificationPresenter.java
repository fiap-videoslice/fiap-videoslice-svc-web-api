package com.example.fiap.videosliceapi.presenters;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;

public class EmailNotificationPresenter {

    public String finishedJobNotificationTitle(Job job) {
        if (job.status() == JobStatus.COMPLETE)
            return "Your Video slice job is complete";
        else if (job.status() == JobStatus.FAILED)
            return "YOUR VIDEO SLICE JOB HAS FAILED";
        else
            throw new IllegalStateException("Notification not expected for status " + job.status());
    }

    public String finishedJobNotificationBody(Job job, DownloadLink downloadLink) {
        if (job.status() == JobStatus.COMPLETE) {
            return completedTemplate(job)
                    .replace("__JOB_ID__", job.id().toString())
                    .replace("__DOWNLOAD_URL__", downloadLink.url())
                    .replace("__DOWNLOAD_EXPIRATION__", String.valueOf(downloadLink.expirationMinutes()));
        } else if (job.status() == JobStatus.FAILED) {
            return failedTemplate(job)
                    .replace("__JOB_ID__", job.id().toString())
                    .replace("__ERROR_MESSAGE__", job.errorMessage());
        } else {
            throw new IllegalStateException("Notification not expected for status " + job.status());
        }
    }

    private String completedTemplate(Job job) {
        return "<html>" +
               "<body>" +
               "<p>" +
               "Your VideoSlice job id=<span style=\"font-weight: bold\">__JOB_ID__</span> has completed successfully" +
               "</p>" +
               "<p>" +
               "<a href=\"__DOWNLOAD_URL__\">Click to download the result file</a>" +
               "</p>" +
               "<p>" +
               "    This link is valid for __DOWNLOAD_EXPIRATION__ minutes. After that you may go to the Job page and get a new download link" +
               "</p>" +
               "<em style=\"color: #7393c8\">* When we have a frontend the link will be here :-/</em>" +
               "</body>" +
               "</html>";
    }

    private String failedTemplate(Job job) {
        return "<html>" +
               "<body>" +
               "<p>" +
               "Your VideoSlice job id=<span style=\"font-weight: bold\">__JOB_ID__</span> has failed" +
               "</p>" +
               "<p>Reason:</p>" +
               "<p style=\"color: #ff0000\">__ERROR_MESSAGE__</p>" +
               "</body>" +
               "</html>";
    }
}
