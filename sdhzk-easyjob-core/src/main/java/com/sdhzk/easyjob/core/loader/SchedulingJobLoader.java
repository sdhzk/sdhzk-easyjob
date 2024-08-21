package com.sdhzk.easyjob.core.loader;

import com.sdhzk.easyjob.core.job.SchedulingJob;

import java.util.List;

/**
 * 定时任务Loader
 *
 * @author Linus.Lee
 */
public interface SchedulingJobLoader {
    List<SchedulingJob> load();
}
