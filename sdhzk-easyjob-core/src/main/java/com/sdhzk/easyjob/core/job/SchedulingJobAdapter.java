package com.sdhzk.easyjob.core.job;

import com.sdhzk.easyjob.core.log.SchedulingLog;
import com.sdhzk.easyjob.core.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;

/**
 * 定时任务适配器
 *
 * @author Linus.Lee
 */
public abstract class SchedulingJobAdapter implements SchedulingJob {

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
}
