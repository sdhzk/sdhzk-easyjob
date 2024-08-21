package com.sdhzk.easyjob.samples;

import com.sdhzk.easyjob.core.job.SchedulingJobAdapter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Linus.Lee
 * @date 2024-8-21
 */
public class MyJob extends SchedulingJobAdapter {
    private String jobKey;
    private String jobName;
    private Map<String, Object> jobParams;
    private String cron;
    private Boolean status = Boolean.FALSE;
    private Boolean logEnabled = Boolean.FALSE;

    @Override
    public void execute() {
        System.out.println("执行MyJob："+ LocalDateTime.now());
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setJobParams(Map<String, Object> jobParams) {
        this.jobParams = jobParams;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(Boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    @Override
    public String getJobKey() {
        return this.jobKey;
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
        return this.status;
    }

    @Override
    public boolean logEnabled() {
        return this.logEnabled;
    }
}
