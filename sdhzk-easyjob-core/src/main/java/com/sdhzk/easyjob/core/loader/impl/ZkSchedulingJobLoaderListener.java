package com.sdhzk.easyjob.core.loader.impl;

import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import com.sdhzk.easyjob.core.config.impl.ZkSchedulingConfigServiceImpl;
import com.sdhzk.easyjob.core.job.SchedulingJob;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoaderListener;
import org.springframework.util.CollectionUtils;

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
        if (CollectionUtils.isEmpty(jobs)) {
            if (schedulingConfigService instanceof ZkSchedulingConfigServiceImpl zkSchedulingConfigService) {
                zkSchedulingConfigService.deleteAllChildNodes();
            }
        } else {
            SchedulingConfig config;
            for (SchedulingJob job : jobs) {
                config = new SchedulingConfig();
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
}
