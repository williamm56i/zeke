package com.williamm56i.zeke.scheduled.trigger;

import com.williamm56i.zeke.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("example-job-trigger")
@Slf4j
public class ExampleJobTrigger implements Runnable{

    @Value("${module.armin}")
    String batchUrl;

    @Override
    public void run() {
        try {
            String jobId = HttpUtils.get(batchUrl + "/executeJob?beanName=example-job");
            log.info("ExampleJob is triggered, jobId: {}", jobId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
