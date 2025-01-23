package com.example.fiap.videosliceapi.presenters;

import com.example.fiap.videosliceapi.domain.entities.Job;
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

    public String finishedJobNotificationBody(Job job) {
        if (job.status() == JobStatus.COMPLETE) {
            return completedTemplate(job).replace("__JOB_ID__", job.id().toString());
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
               "    Go to the application to get the download link" +
               "</p>" +
               "<em style=\"color: #7393c8\">When we have a frontend the link will be here :-/</em>" +
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
