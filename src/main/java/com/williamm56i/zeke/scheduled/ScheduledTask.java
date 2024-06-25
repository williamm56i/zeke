package com.williamm56i.zeke.scheduled;

import lombok.Data;

import java.util.concurrent.ScheduledFuture;

@Data
public class ScheduledTask {
    private ScheduledFuture<?> scheduledFuture;
    private String cronExpression;

    public ScheduledTask(ScheduledFuture<?> scheduledFuture, String cronExpression) {
        this.scheduledFuture = scheduledFuture;
        this.cronExpression = cronExpression;
    }
}
