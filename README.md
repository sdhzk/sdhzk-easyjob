# 基于springboot+zk的简单分布式任务

## 示例

参考sdhzk-easyjob-samples项目

## 添加依赖

引入sdhzk-easyjob-spring-boot-starter依赖

```xml
    <dependency>
        <groupId>com.sdhzk.easyjob</groupId>
        <artifactId>sdhzk-easyjob-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
```

## 添加@EnableScheduling注解

```java
@EnableScheduling
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

## 实现SchedulingJobLoader接口

```java
@Component
public class MyAppSchedulingJobLoader implements SchedulingJobLoader {

    @Override
    public List<SchedulingJob> load() {
        MyJob myJob = new MyJob();
        myJob.setJobName("MyJob");
        myJob.setJobParams(null);
        myJob.setCron("0/10 * * * * ?");
        myJob.setEnabled(true);
        myJob.setLogEnabled(true);
        return List.of(myJob);
    }
}
```

## 实现SchedulingLogProcessor接口【可选】

```java
@Component
public class MyAppSchedulingLogProcessor implements SchedulingLogProcessor {
    @Override
    public void process(SchedulingLog log) {
        System.out.println("接收到日志：" + log);
    }
}
```

## 配置
```yaml
easyjob:
  enabled: true # 是否启用
  preferred-networks: 172.18.179 # 多网卡时首选的网段
  thread-pool: # 线程池配置
    core-pool-size: 8 # 核心线程数
    max-pool-size: 8 # 最大线程数
    keep-alive-seconds: 0 # 线程空闲时间
  cluster: # 集群配置
    name: mycloud # 集群名称
    app-id: myapp # 应用id
    enabled: true # 是否启用
  zk: # zookeeper配置
    connectionString: ${zk.connectionString} # zookeeper连接串
    baseSleepTimeMs: 1000 # 睡眠时间 单位：毫秒
    maxRetries: 3 # 最大重试次数
```
