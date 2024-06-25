package com.williamm56i.zeke.scheduled;

import com.williamm56i.zeke.persistence.dao.BatchJobTriggerConfigDao;
import com.williamm56i.zeke.persistence.vo.BatchJobTriggerConfig;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class JobScheduler {

    @Autowired
    BatchJobTriggerConfigDao batchJobTriggerConfigDao;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    TaskScheduler taskScheduler;
    Map<String, ScheduledFuture<?>> jobsMap = new HashMap<>();

    public void loadJobTrigger() {
        List<BatchJobTriggerConfig> triggerList = batchJobTriggerConfigDao.selectEnableJobTrigger();
        if (CollectionUtils.isNotEmpty(triggerList)) {
            triggerList.forEach(config -> {
                Runnable trigger = (Runnable) applicationContext.getBean(config.getTriggerName());
                ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(trigger, new CronTrigger(config.getCronTrigger()));
                jobsMap.put(config.getJobName(), scheduledFuture);
            });
            log.info("All Job Triggers are loaded");
        } else {
            log.warn("No Job is Enable");
        }
    }
}
