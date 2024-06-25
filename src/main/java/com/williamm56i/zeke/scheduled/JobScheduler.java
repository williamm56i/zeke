package com.williamm56i.zeke.scheduled;

import com.williamm56i.zeke.persistence.dao.BatchJobTriggerConfigDao;
import com.williamm56i.zeke.persistence.vo.BatchJobTriggerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JobScheduler {

    @Autowired
    BatchJobTriggerConfigDao batchJobTriggerConfigDao;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    TaskScheduler taskScheduler;
    Map<String, ScheduledTask> jobsMap = new HashMap<>();

    @Scheduled(fixedDelay = 10000)
    public void refreshJobTrigger() {
        List<BatchJobTriggerConfig> configList = batchJobTriggerConfigDao.selectAll();
        configList.forEach( config -> {
            String jobName = config.getJobName();
            String triggerName = config.getTriggerName();
            String cronExpression = config.getCronTrigger();
            String enable = config.getEnable();
            ScheduledTask task = jobsMap.get(jobName);
            // close
            if (StringUtils.equals(enable, "N")) {
                if (task != null) {
                    // when open, close it
                    task.getScheduledFuture().cancel(true);
                    jobsMap.remove(jobName);
                    log.info("close job trigger: {}", jobName);
                }
            }
            // open
            if (StringUtils.equals(enable, "Y")) {
                if (task == null) {
                    // when close, open it
                    Runnable newTrigger = (Runnable) applicationContext.getBean(triggerName);
                    ScheduledTask newTask = new ScheduledTask(taskScheduler.schedule(newTrigger, new CronTrigger(cronExpression)), cronExpression);
                    jobsMap.put(jobName, newTask);
                    log.info("open job trigger: {} {}", jobName, cronExpression);
                } else {
                    // when open, check cronExpression
                    if (!StringUtils.equals(cronExpression, task.getCronExpression())) {
                        // cronExpression has been changed, close then reopen
                        task.getScheduledFuture().cancel(true);
                        jobsMap.remove(jobName);
                        Runnable newTrigger = (Runnable) applicationContext.getBean(triggerName);
                        ScheduledTask newTask = new ScheduledTask(taskScheduler.schedule(newTrigger, new CronTrigger(cronExpression)), cronExpression);
                        jobsMap.put(jobName, newTask);
                        log.info("change job trigger {} from {} to {}", jobName, task.getCronExpression(), cronExpression);
                    }
                }
            }
        });
        log.info("refresh job trigger completed!");
    }
}
