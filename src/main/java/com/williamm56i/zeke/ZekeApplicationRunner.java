package com.williamm56i.zeke;

import com.williamm56i.zeke.scheduled.JobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Execute immediately when project started
 */
@Component
@Slf4j
public class ZekeApplicationRunner implements ApplicationRunner {

    @Autowired
    JobScheduler jobScheduler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jobScheduler.loadJobTrigger();
        log.info("Start Scheduling...");
    }
}
