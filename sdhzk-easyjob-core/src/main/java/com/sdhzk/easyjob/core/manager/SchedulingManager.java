package com.sdhzk.easyjob.core.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sdhzk.easyjob.core.job.SchedulingJob;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author Linus.Lee
 */
public class SchedulingManager implements SchedulingConfigurer, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingManager.class);
    private final Map<String, ScheduledFuture<?>> scheduledFutureMap = Maps.newConcurrentMap();
    private final Map<String, SchedulingJob> jobMap = Maps.newConcurrentMap();
    private ScheduledTaskRegistrar registrar;
    private SchedulingJobLoader schedulingJobLoader;

    private boolean needStarted = false;

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

    public boolean isNeedStarted() {
        return needStarted;
    }

    public void setNeedStarted(boolean needStarted) {
        this.needStarted = needStarted;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setScheduler(new ConcurrentTaskScheduler(newScheduledExecutor()));
        this.registrar = registrar;
        if(this.needStarted){
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
        List<SchedulingJob> list = this.schedulingJobLoader.load();
        if (CollectionUtils.isEmpty(list)) {
            clear();
            return;
        }
        list.forEach(job -> {
            if (job.enabled()) {
                if (!CronExpression.isValidExpression(job.getCron())) {
                    logger.warn("easyjob定时任务cron表达式不合法：{}", job);
                    return;
                }
                ScheduledFuture<?> scheduledFuture = Objects.requireNonNull(registrar.getScheduler())
                        .schedule(job, triggerContext -> new CronTrigger(job.getCron()).nextExecution(triggerContext));
                scheduledFutureMap.put(job.getJobKey(), scheduledFuture);
                jobMap.put(job.getJobKey(), job);
                logger.info("启用定时任务:{}", job);
            } else {
                stopTask(job.getJobKey());
            }
        });
        logger.info("加载easyjob定时任务成功");
    }

    public void stopTask(String jobKey) {
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(jobKey);
        if (Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(false);
            scheduledFutureMap.remove(jobKey);
            jobMap.remove(jobKey);
            logger.info("停用easyjob定时任务:{}", jobKey);
        }
    }

    public void clear() {
        if (!scheduledFutureMap.isEmpty()) {
            scheduledFutureMap.values().forEach(scheduledFuture -> scheduledFuture.cancel(false));
        }
        scheduledFutureMap.clear();
        jobMap.clear();
        logger.info("清除全部easyjob定时任务成功");
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
