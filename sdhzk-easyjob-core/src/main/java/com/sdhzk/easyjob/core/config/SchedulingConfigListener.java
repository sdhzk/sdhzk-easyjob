package com.sdhzk.easyjob.core.config;

import com.sdhzk.easyjob.core.manager.SchedulingManager;
import org.apache.curator.x.async.modeled.ZPath;
import org.apache.curator.x.async.modeled.cached.ModeledCacheListener;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
public class SchedulingConfigListener implements ModeledCacheListener<SchedulingConfig> {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfigListener.class);

    private final SchedulingManager schedulingManager;

    public SchedulingConfigListener(SchedulingManager schedulingManager) {
        this.schedulingManager = schedulingManager;
    }

    @Override
    public void accept(Type type, ZPath path, Stat stat, SchedulingConfig config) {
        switch (type) {
            case NODE_ADDED:
                logger.info("添加定时任务配置：{}", config);
                if (isLeadership()) {
                    schedulingManager.updateTask(config);
                }
                break;
            case NODE_UPDATED:
                logger.info("更新定时任务配置：{}", config);
                if (isLeadership()) {
                    schedulingManager.updateTask(config);
                }
                break;
            case NODE_REMOVED:
                logger.info("删除定时任务配置：{}", config);
                if (isLeadership()) {
                    schedulingManager.deleteTask(config.getJobKey());
                }
                break;
            default:
                break;
        }
    }

    private boolean isLeadership() {
        return schedulingManager.isLeadership();
    }
}
