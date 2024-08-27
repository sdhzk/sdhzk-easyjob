package com.sdhzk.easyjob.core.loader;

import com.sdhzk.easyjob.core.job.SchedulingJob;

import java.util.List;

/**
 * 定时任务Loader监听器
 *
 * @author Linus.Lee
 */
public interface SchedulingJobLoaderListener {
    void onLoaded(List<SchedulingJob> jobs);
}
