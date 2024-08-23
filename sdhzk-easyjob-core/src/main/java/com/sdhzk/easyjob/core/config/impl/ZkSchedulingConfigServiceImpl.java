package com.sdhzk.easyjob.core.config.impl;

import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import org.apache.curator.x.async.modeled.ModeledFramework;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
public class ZkSchedulingConfigServiceImpl implements SchedulingConfigService {

    private final ModeledFramework<SchedulingConfig> modeledClient;

    public ZkSchedulingConfigServiceImpl(ModeledFramework<SchedulingConfig> modeledClient) {
        this.modeledClient = modeledClient;
    }

    @Override
    public void update(SchedulingConfig config) {
        modeledClient.child(config.getJobKey()).set(config);
    }

    @Override
    public void delete(String jobKey) {
        modeledClient.child(jobKey).delete();
    }
}
