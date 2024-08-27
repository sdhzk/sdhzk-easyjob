package com.sdhzk.easyjob.core.loader.impl;

import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import com.sdhzk.easyjob.core.job.SchedulingJob;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoaderListener;

import java.util.List;

/**
 * @author Linus.Lee
 * @date 2024-8-27
 */
public class ZkSchedulingJobLoaderListener implements SchedulingJobLoaderListener {

    private final SchedulingConfigService schedulingConfigService;

    public ZkSchedulingJobLoaderListener(SchedulingConfigService schedulingConfigService) {
        this.schedulingConfigService = schedulingConfigService;
    }

    @Override
    public void onLoaded(List<SchedulingJob> jobs) {
        for (SchedulingJob job : jobs) {
            SchedulingConfig config = new SchedulingConfig();
            config.setJobKey(job.getJobKey());
            config.setJobName(job.getJobName());
            config.setJobParams(job.getJobParams());
            config.setCron(job.getCron());
            config.setEnabled(job.enabled());
            config.setLogEnabled(job.logEnabled());
            schedulingConfigService.update(config);
        }
    }
}
