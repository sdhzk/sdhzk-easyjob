package com.sdhzk.easyjob.core.log;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class SchedulingLog extends ApplicationEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String jobKey;

    private Map<String, Object> jobParams;

    private Boolean status;

    private String errorMsg;

    private LocalDateTime createTime;

    public SchedulingLog(Object source) {
        super(source);
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public Map<String, Object> getJobParams() {
        return jobParams;
    }

    public void setJobParams(Map<String, Object> jobParams) {
        this.jobParams = jobParams;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SchedulingLog{" +
                "jobKey='" + jobKey + '\'' +
                ", jobParams=" + jobParams +
                ", status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}