package com.sdhzk.easyjob.core.job;

import com.sdhzk.easyjob.core.log.SchedulingLog;
import com.sdhzk.easyjob.core.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定时任务适配器
 *
 * @author Linus.Lee
 */
public abstract class SchedulingJobAdapter implements SchedulingJob {
    private String jobName;
    private Map<String, Object> jobParams;
    private String cron;
    private boolean enabled = Boolean.FALSE;
    private boolean logEnabled = Boolean.FALSE;

    private final ApplicationContext applicationContext;

    public SchedulingJobAdapter() {
        this.applicationContext = SpringContextUtils.getApplicationContext();
    }

    @Override
    public void run() {
        boolean result = true;
        String errorMsg = "";
        try {
            this.execute();
        } catch (Throwable ex) {
            result = false;
            errorMsg = ex.getMessage();
        } finally {
            if (this.logEnabled()) {
                applicationContext.publishEvent(createSchedulingLog(result, errorMsg));
            }
        }
    }

    private SchedulingLog createSchedulingLog(boolean result, String errorMsg) {
        SchedulingLog log = new SchedulingLog(this);
        log.setJobKey(this.getJobKey());
        log.setJobParams(this.getJobParams());
        log.setCreateTime(LocalDateTime.now());
        log.setStatus(result);
        log.setErrorMsg(errorMsg);
        return log;
    }

    @Override
    public String getJobName() {
        return this.jobName;
    }

    @Override
    public Map<String, Object> getJobParams() {
        return this.jobParams;
    }

    @Override
    public String getCron() {
        return this.cron;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public boolean logEnabled() {
        return this.logEnabled;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public void setCron(String cron) {
        this.cron = cron;
    }

    @Override
    public void setJobParams(Map<String, Object> jobParams) {
        this.jobParams = jobParams;
    }

    @Override
    public void enable() {
        this.enabled = Boolean.TRUE;
    }

    @Override
    public void disable() {
        this.enabled = Boolean.FALSE;
    }

    @Override
    public void logDisable() {
        this.logEnabled = Boolean.FALSE;
    }

    @Override
    public void logEnable() {
        this.logEnabled = Boolean.TRUE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }
}
