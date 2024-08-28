package com.sdhzk.easyjob.core.config.impl;

import com.sdhzk.easyjob.core.EasyJobConst;
import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import org.apache.curator.x.async.WatchMode;
import org.apache.curator.x.async.api.DeleteOption;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
public class ZkSchedulingConfigServiceImpl implements SchedulingConfigService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    public void deleteAllChildNodes() {
        modeledClient.unwrap()
                .delete()
                .withOptions(Set.of(DeleteOption.deletingChildrenIfNeeded))
                .forPath(EasyJobConst.DEFAULT_CONFIG_PATH)
                .whenComplete((result, ex)->{
                    if(ex != null) {
                        logger.error("delete all child nodes failed", ex);
                    } else {
                        logger.info("delete all child nodes success");
                    }
                });
    }
}
