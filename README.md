# Zeke Scheduler
Scheduler架構專案，定義Armin中Job的Trigger以實現「批次業務邏輯」、「排程觸發時間」架構分離
當Job的觸發時間一到時，Zeke透過API呼叫Armin執行Job隨後結束作業，不用等待Armin執行結果
* 解決共用專案時排程間可能造成不預期的相互影響
* 解決共用專案下觸發程式不適合橫向擴充，連帶導致批次僅能單機執行

### 技術框架
* Java version 18
* Spring boot 3.3.0
* Spring Scheduling
* Maven
* Mybatis
* h2 (視專案情況替換成任何RDB

### 開發工具
* IntelliJ

### 版本資訊
* 0.0.1-SNAPSHOT
    * 初版

### 執行
* 打包jar
```
mvn install
```
* build image
```
docker build --tag zeke:latest .
```
* run
```
docker run --name ZEKE -p 8280:8280 -d zeke:latest
```

### Spring Scheduling介紹
#### 相關設定
* 定義於ZekeApplication中
* @EnableScheduling
* 定義schedule的thread pool上限
```java
@EnableScheduling
@SpringBootApplication
public class ZekeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZekeApplication.class, args);
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}
}
```
#### 基礎用法
* 於想要定時執行的method上方加上@Scheduled即可
```
// 每日0:00:00執行
@Scheduled(cron = "0 0 0 * * ?")
public void method() {
  // do something..
}
```
#### Cron trigger表達式
* 每個數字/符號由左至右依序為：
  * 秒 分 時 日 月 星期 年
  * 年可省略
  * 日、星期只能擇一，沒使用到的以?表示
  * 星期日為首日，編號從0~6；或直接用SUN,MON,...,SAT表示
    * 例："0 30 8 15 * ?"，每月15日8:30:00
    * 例："0 0 16 ? 5 MON"，五月的每個星期一16:00:00
* 常見變形
  * ,：表示「和」，例："0 0 9,17 * * ?"，每天9點和17點
  * /：表示「每」，例："0/10 * * * * ?"，每日0秒開始每10秒
  * \-：表示「至」，例："0 0 22 ? * MON-FRI"，每月週一至週五22:00:00
* 更詳細使用方式請參考[維基百科](https://en.wikipedia.org/eiki/Cron)

#### 動態載入/刷新排程觸發時間
* 參考package：scheduled
* 欲解決維運問題：傳統排程觸發時間以cron字串定義於程式碼中，若因業務需要欲調整觸發時間或停止輪巡，則需調整程式法後安排公司上線程序，曠日費時
* 作法概念：將排程觸發時間定義於資料表中，專案啟動後以一輪巡schedule(refreshJobTrigger)檢視資料表，將即時的排程設定更新進taskScheduler中
* 實作方法：為每支Armin Job定義專屬的trigger(實作Runnable)程式成bean，專案啟動後透由refreshJobTrigger取得資料表(BATCH_JOB_TRIGGER_CONFIG)中所有Job觸發設定，判斷或起或停或調整觸發時間後更新taskScheduler
* 運作模式：Trigger程式的觸發時間一到，透過HTTP呼叫Armin API /executeJob?beanName=，Armin再透由getBean執行run()開始跑批
```java
/* 觸發程式 */
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
```
```java
/* refreshJobTrigger，10秒輪巡一次 */
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
```