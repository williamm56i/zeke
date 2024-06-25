package com.williamm56i.zeke.scheduled.trigger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("bertolt-job-trigger")
@Slf4j
public class BertoltJobTrigger implements Runnable{

    @Override
    public void run() {
        try {
            // just for demo. Armin doesn't have this job
            log.info("BertoltJob is triggered");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
