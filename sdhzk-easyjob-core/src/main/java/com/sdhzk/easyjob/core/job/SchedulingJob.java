package com.sdhzk.easyjob.core.job;

import java.util.Map;

/**
 * 定时任务接口
 *
 * @author Linus.Lee
 */
public interface SchedulingJob extends Runnable {
    void execute();

    String getJobKey();

    String getJobName();

    Map<String, Object> getJobParams();

    String getCron();

    boolean enabled();

    boolean logEnabled();

    void setJobName(String jobName);

    void setCron(String cron);

    void setJobParams(Map<String, Object> jobParams);

    void enable();

    void disable();

    void logDisable();

    void logEnable();
}
