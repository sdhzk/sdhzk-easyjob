package com.sdhzk.easyjob.core.leader;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;

/**
 * @author Linus.Lee
 * @date 2024-8-20
 */
public abstract class LeaderSelectorAdapter implements Closeable, LeaderLatchListener {
    private final CuratorFramework client;
    private final String leaderPath;
    private final String id;
    private final LeaderSelector leaderSelector;

    public LeaderSelectorAdapter(CuratorFramework client, String leaderPath, String id) {
        this.client = client;
        this.leaderPath = leaderPath;
        this.id = id;
        this.leaderSelector = new LeaderSelector(client, leaderPath, new DefaultLeaderSelectorListener());
        this.leaderSelector.setId(id);
        this.leaderSelector.autoRequeue();
    }

    private class DefaultLeaderSelectorListener extends LeaderSelectorListenerAdapter {
        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {
            try {
                LeaderSelectorAdapter.this.isLeader();
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                LeaderSelectorAdapter.this.notLeader();
            }
        }
    }

    public void start() {
        this.leaderSelector.start();
    }

    public boolean hasLeadership() {
        return this.leaderSelector.hasLeadership();
    }

    @Override
    public void close() {
        this.leaderSelector.close();
    }

    public CuratorFramework getClient() {
        return client;
    }

    public String getLeaderPath() {
        return leaderPath;
    }

    public String getId() {
        return id;
    }

    public LeaderSelector getLeaderSelector() {
        return leaderSelector;
    }
}
