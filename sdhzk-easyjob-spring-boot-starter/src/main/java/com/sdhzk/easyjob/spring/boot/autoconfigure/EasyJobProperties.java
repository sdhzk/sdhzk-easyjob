package com.sdhzk.easyjob.spring.boot.autoconfigure;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Linus.Lee
 * @date 2024-8-20
 */
@ConfigurationProperties(prefix = "easyjob")
public class EasyJobProperties {
    private Boolean enabled = Boolean.TRUE;
    private String preferredNetworks;
    private ThreadPool threadPool = new ThreadPool();
    private Zk zk;
    private Cluster cluster = new Cluster();


    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPreferredNetworks() {
        return preferredNetworks;
    }

    public void setPreferredNetworks(String preferredNetworks) {
        this.preferredNetworks = preferredNetworks;
    }

    public static class ThreadPool {
        private Integer corePoolSize;
        private Integer maxPoolSize;
        private Integer keepAliveSeconds;

        public Integer getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(Integer corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public Integer getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(Integer maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public Integer getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(Integer keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }
    }

    public static class Zk {
        private String connectionString;
        private Integer baseSleepTimeMs;
        private Integer maxRetries;

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public Integer getBaseSleepTimeMs() {
            return baseSleepTimeMs;
        }

        public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
            this.baseSleepTimeMs = baseSleepTimeMs;
        }

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }
    }

    public static class Cluster {
        private Boolean enabled = Boolean.FALSE;
        private String name = "default";
        private String appId = "default";

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public Zk getZk() {
        return zk;
    }

    public void setZk(Zk zk) {
        this.zk = zk;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
}
