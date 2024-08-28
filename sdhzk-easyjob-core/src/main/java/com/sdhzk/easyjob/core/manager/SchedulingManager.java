package com.sdhzk.easyjob.core.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.job.SchedulingJob;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoader;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Linus.Lee
 */
public class SchedulingManager implements SchedulingConfigurer, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingManager.class);
    private final Map<String, ScheduledTask> scheduledTaskMap = Maps.newConcurrentMap();
    private final Map<String, SchedulingJob> jobMap = Maps.newConcurrentMap();
    private ScheduledTaskRegistrar registrar;
    private SchedulingJobLoader schedulingJobLoader;
    private SchedulingJobLoaderListener schedulingJobLoaderListener;
    private CountDownLatch waitForInit = new CountDownLatch(1);
    private volatile boolean initialized = false;
    private volatile boolean clustered = false;
    private volatile boolean leadership = false;
    private volatile boolean started = false;

    private int corePoolSize = 8;
    private int maxPoolSize = 8;
    private int keepAliveSeconds = 0;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public SchedulingJobLoader getSchedulingJobLoader() {
        return schedulingJobLoader;
    }

    public void setSchedulingJobLoader(SchedulingJobLoader schedulingJobLoader) {
        this.schedulingJobLoader = schedulingJobLoader;
    }

    public SchedulingJobLoaderListener getSchedulingJobLoaderListener() {
        return schedulingJobLoaderListener;
    }

    public void setSchedulingJobLoaderListener(SchedulingJobLoaderListener schedulingJobLoaderListener) {
        this.schedulingJobLoaderListener = schedulingJobLoaderListener;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isLeadership() {
        return leadership;
    }

    public void setLeadership(boolean leadership) {
        this.leadership = leadership;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setScheduler(new ConcurrentTaskScheduler(newScheduledExecutor()));
        this.registrar = registrar;
        waitForInit.countDown();
        if (!this.isClustered()) {
            this.start();
        }
    }


    private ScheduledExecutorService newScheduledExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("schedule-pool-%d").build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, namedThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setCorePoolSize(corePoolSize);
        executor.setMaximumPoolSize(maxPoolSize);
        executor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    public void start() {
        clusterCheckLeadship("不能启动SchedulingManager");
        if (!initialized) {
            try {
                waitForInit.await();
                initialized = true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        List<SchedulingJob> jobs = loadAndVerifyJobs();
        if (this.schedulingJobLoaderListener != null) {
            this.schedulingJobLoaderListener.onLoaded(jobs);
        }
        if (CollectionUtils.isEmpty(jobs)) {
            clear();
            this.started = true;
            return;
        }
        jobs.forEach(job -> {
            if (job.enabled()) {
                if (!CronExpression.isValidExpression(job.getCron())) {
                    logger.warn("cron表达式不合法：{}", job);
                    return;
                }
                ScheduledTask scheduledTask = this.registrar.scheduleCronTask(new CronTask(job, job.getCron()));
                scheduledTaskMap.put(job.getJobKey(), scheduledTask);
                logger.info("启用定时任务:{}", job);
            }
            jobMap.put(job.getJobKey(), job);
        });
        this.started = true;
        logger.info("加载easyjob定时任务成功");
    }

    private List<SchedulingJob> loadAndVerifyJobs() {
        List<SchedulingJob> jobs = this.schedulingJobLoader.load();
        if(CollectionUtils.isEmpty(jobs)){
            return null;
        }
        Set<String> jobKeys = Sets.newHashSet();
        for (SchedulingJob job : jobs) {
            if(jobKeys.contains(job.getJobKey())){
                throw new IllegalArgumentException("jobKey重复，jobKey：" + job.getJobKey());
            }
            if (!CronExpression.isValidExpression(job.getCron())) {
                throw new IllegalArgumentException("cron表达式不合法，jobKey：" + job.getJobKey());
            }
            jobKeys.add(job.getJobKey());
        }
        return jobs;
    }


    public boolean startTask(String jobKey) {
        clusterCheckLeadship("不能启动定时任务");
        Preconditions.checkState(initialized, "SchedulingManager初始化未完成");
        Preconditions.checkState(jobMap.containsKey(jobKey), "定时任务jobKey：" + jobKey + "没有注册");
        Preconditions.checkState(!scheduledTaskMap.containsKey(jobKey), "定时任务jobKey:" + jobKey + "已经启动");
        SchedulingJob job = jobMap.get(jobKey);
        ScheduledTask scheduledTask = this.registrar.scheduleCronTask(new CronTask(job, job.getCron()));
        scheduledTaskMap.put(job.getJobKey(), scheduledTask);
        return true;
    }

    public boolean stopTask(String jobKey) {
        clusterCheckLeadship("不能停止定时任务");
        Preconditions.checkState(initialized, "SchedulingManager初始化未完成");
        ScheduledTask scheduledTask = scheduledTaskMap.get(jobKey);
        if (Objects.nonNull(scheduledTask)) {
            scheduledTaskMap.remove(jobKey);
            scheduledTask.cancel(true);
            logger.info("停用easyjob定时任务:{}", jobKey);
            return true;
        }
        return false;
    }

    public boolean updateTask(SchedulingConfig config) {
        clusterCheckLeadship("不能更新定时任务");
        Preconditions.checkState(initialized, "SchedulingManager初始化未完成");
        Preconditions.checkArgument(jobMap.containsKey(config.getJobKey()), "定时任务jobKey：" + config.getJobKey() + "不存在");
        Preconditions.checkArgument(CronExpression.isValidExpression(config.getCron()), "定时任务cron：" + config.getCron() + "表达式不合法");
        SchedulingJob job = jobMap.get(config.getJobKey());
        if (config.isEnabled()) {
            job.enable();
        } else {
            job.disable();
        }
        if (config.isLogEnabled()) {
            job.logEnable();
        } else {
            job.logDisable();
        }
        job.setJobName(config.getJobName());
        job.setJobParams(config.getJobParams());
        job.setCron(config.getCron());
        ScheduledTask scheduledTask = scheduledTaskMap.get(config.getJobKey());
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            if (job.enabled()) {
                scheduledTask = this.registrar.scheduleCronTask(new CronTask(job, job.getCron()));
                scheduledTaskMap.put(job.getJobKey(), scheduledTask);
            }
        } else {
            if (job.enabled()) {
                scheduledTask = this.registrar.scheduleCronTask(new CronTask(job, job.getCron()));
                scheduledTaskMap.put(job.getJobKey(), scheduledTask);
            }
        }
        jobMap.put(job.getJobKey(), job);
        logger.info("更新定时任务:{}", job);
        return true;
    }

    public boolean deleteTask(String jobKey) {
        clusterCheckLeadship("不能删除定时任务");
        Preconditions.checkState(initialized, "SchedulingManager初始化未完成");
        Preconditions.checkArgument(jobMap.containsKey(jobKey), "定时任务jobKey：" + jobKey + "不存在");
        ScheduledTask scheduledTask = scheduledTaskMap.get(jobKey);
        if (Objects.nonNull(scheduledTask)) {
            scheduledTask.cancel(true);
        }
        scheduledTaskMap.remove(jobKey);
        logger.info("删除定时任务：{}", jobKey);
        return true;
    }

    private void clusterCheckLeadship(String msg) {
        if (clustered && !leadership) {
            throw new IllegalStateException("当前节点不是leader节点，" + msg);
        }
    }

    public void clear() {
        if (!scheduledTaskMap.isEmpty()) {
            scheduledTaskMap.values().forEach(task -> task.cancel(true));
        }
        scheduledTaskMap.clear();
        jobMap.clear();
        logger.info("清除全部easyjob定时任务成功");
    }

    public boolean hasJob(String jobKey) {
        return jobMap.containsKey(jobKey);
    }

    public void refresh() {
        start();
    }

    public List<SchedulingJob> getRunningJobs() {
        return ImmutableList.copyOf(jobMap.values());
    }

    @Override
    public void destroy() {
        this.registrar.destroy();
    }
}
