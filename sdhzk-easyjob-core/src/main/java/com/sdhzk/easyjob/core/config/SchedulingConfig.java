package com.sdhzk.easyjob.core.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
public class SchedulingConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String jobKey;

    private String jobName;

    private Map<String, Object> jobParams;

    private String cron;

    private boolean enabled = Boolean.FALSE;

    private boolean logEnabled = Boolean.FALSE;

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Map<String, Object> getJobParams() {
        return jobParams;
    }

    public void setJobParams(Map<String, Object> jobParams) {
        this.jobParams = jobParams;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
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

    @Override
    public String toString() {
        return "SchedulingConfig{" +
                "jobKey='" + jobKey + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobParams=" + jobParams +
                ", cron='" + cron + '\'' +
                ", enabled=" + enabled +
                ", logEnabled=" + logEnabled +
                '}';
    }
}
