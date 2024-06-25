INSERT INTO BATCH_JOB_TRIGGER_CONFIG(JOB_NAME, JOB_DESC, BEAN_NAME, TRIGGER_NAME, CRON_TRIGGER, ENABLE, CREATE_ID, CREATE_DATE, UPDATE_ID, UPDATE_DATE)
VALUES ('ExampleJob', '範例作業', 'example-job', 'example-job-trigger', '0 * * * * ?', 'Y', 'SYS', NOW(), null, null);
