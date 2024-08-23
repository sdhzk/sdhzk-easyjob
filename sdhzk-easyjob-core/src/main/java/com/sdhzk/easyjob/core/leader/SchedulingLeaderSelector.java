package com.sdhzk.easyjob.core.leader;

import com.sdhzk.easyjob.core.manager.SchedulingManager;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Linus.Lee
 * @date 2024-8-20
 */
public class SchedulingLeaderSelector extends LeaderSelectorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingLeaderSelector.class);

    private final SchedulingManager schedulingManager;

    public SchedulingLeaderSelector(CuratorFramework client, String leaderPath, String id, SchedulingManager schedulingManager) {
        super(client, leaderPath, id);
        this.schedulingManager = schedulingManager;
    }

    public SchedulingManager getSchedulingManager() {
        return schedulingManager;
    }

    @Override
    public void isLeader() {
        logger.info("easyjob当前节点被选举为leader");
        try {
            schedulingManager.start();
        } catch (Exception ex) {
            logger.error("启动SchedulingManager失败", ex);
        }
    }

    @Override
    public void notLeader() {
        logger.info("easyjob当前节点放弃leader");
        try {
            schedulingManager.clear();
        } catch (Exception ex) {
            logger.error("SchedulingManager清除全部定时任务失败", ex);
        }
    }
}
