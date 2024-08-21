package com.sdhzk.easyjob.core.job;

/**
 * 定时任务适配器
 *
 * @author Linus.Lee
 */
public abstract class SchedulingJobAdapter implements SchedulingJob {
    @Override
    public void run() {
        try {
            this.execute();
        } catch (Throwable ex) {

        }
    }
}
