package com.sdhzk.easyjob.core.config.impl;

import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import com.sdhzk.easyjob.core.manager.SchedulingManager;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
public class DefaultSchedulingConfigServiceImpl implements SchedulingConfigService {
    private final SchedulingManager schedulingManager;


    public DefaultSchedulingConfigServiceImpl(SchedulingManager schedulingManager) {
        this.schedulingManager = schedulingManager;
    }

    @Override
    public void update(SchedulingConfig config) {
        schedulingManager.updateTask(config);
    }

    @Override
    public void delete(String jobKey) {
        schedulingManager.deleteTask(jobKey);
    }
}
