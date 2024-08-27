package com.sdhzk.easyjob.spring.boot.autoconfigure;

import com.sdhzk.easyjob.core.EasyJobConst;
import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigListener;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import com.sdhzk.easyjob.core.config.impl.DefaultSchedulingConfigServiceImpl;
import com.sdhzk.easyjob.core.config.impl.ZkSchedulingConfigServiceImpl;
import com.sdhzk.easyjob.core.leader.SchedulingLeaderSelector;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoader;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoaderListener;
import com.sdhzk.easyjob.core.loader.impl.ZkSchedulingJobLoaderListener;
import com.sdhzk.easyjob.core.log.SchedulingLogEventListener;
import com.sdhzk.easyjob.core.log.SchedulingLogProcessor;
import com.sdhzk.easyjob.core.manager.SchedulingManager;
import com.sdhzk.easyjob.core.util.NetworkUtils;
import com.sdhzk.easyjob.core.util.SpringContextUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;
import org.apache.curator.x.async.modeled.cached.CachedModeledFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Linus.Lee
 * @date 2024-8-20
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnProperty(
        name = "easyjob.enabled",
        matchIfMissing = true,
        havingValue = "true"
)
@EnableConfigurationProperties(EasyJobProperties.class)
public class EasyJobAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EasyJobAutoConfiguration.class);

    @ConditionalOnMissingBean(SchedulingManager.class)
    @Bean
    public SchedulingManager schedulingManager(EasyJobProperties properties,
                                               ModeledFramework<SchedulingConfig> modeledClient,
                                               SchedulingConfigService schedulingConfigService,
                                               ObjectProvider<SchedulingJobLoader> schedulingJobLoader) {
        if (schedulingJobLoader.getIfAvailable() == null) {
            throw new IllegalStateException("SchedulingJobLoader不能为空");
        }
        SchedulingManager schedulingManager = new SchedulingManager();
        schedulingManager.setClustered(properties.getCluster().getEnabled());
        schedulingManager.setSchedulingJobLoader(schedulingJobLoader.getIfAvailable());
        if (properties.getThreadPool().getCorePoolSize() != null) {
            if (properties.getThreadPool().getCorePoolSize() < 1) {
                throw new IllegalArgumentException("easyjob.thread-pool.corePoolSize必须大于0");
            }
            schedulingManager.setCorePoolSize(properties.getThreadPool().getCorePoolSize());
        }
        if (properties.getThreadPool().getMaxPoolSize() != null) {
            if (properties.getThreadPool().getCorePoolSize() < 1) {
                throw new IllegalArgumentException("easyjob.thread-pool.maxPoolSize必须大于0");
            }
            if (properties.getThreadPool().getCorePoolSize() != null
                    && properties.getThreadPool().getCorePoolSize() > properties.getThreadPool().getMaxPoolSize()) {
                throw new IllegalArgumentException("easyjob.thread-pool.corePoolSize不能大于easyjob.thread-pool.maxPoolSize");
            }
            schedulingManager.setMaxPoolSize(properties.getThreadPool().getMaxPoolSize());
        }
        if (properties.getThreadPool().getKeepAliveSeconds() != null) {
            if (properties.getThreadPool().getKeepAliveSeconds() < 0) {
                throw new IllegalArgumentException("easyjob.thread-pool.keepAliveSeconds不能是负数");
            }
            schedulingManager.setKeepAliveSeconds(properties.getThreadPool().getKeepAliveSeconds());
        }

        if(schedulingManager.isClustered()){
            CachedModeledFramework<SchedulingConfig> cached = modeledClient.cached();
            cached.listenable().addListener(new SchedulingConfigListener(schedulingManager));
            cached.start();
            schedulingManager.setSchedulingJobLoaderListener(new ZkSchedulingJobLoaderListener(schedulingConfigService));
        }

        return schedulingManager;
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            matchIfMissing = true, havingValue = "true"
    )
    @ConditionalOnMissingBean(CuratorFramework.class)
    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework(EasyJobProperties properties) {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(properties.getZk().getConnectionString())
                .retryPolicy(new ExponentialBackoffRetry(properties.getZk().getBaseSleepTimeMs(), properties.getZk().getMaxRetries()))
                .build();
        client.start();
        logger.info("CuratorFramework启动成功");
        return client;
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            matchIfMissing = true, havingValue = "true"
    )
    @ConditionalOnMissingBean(SchedulingLeaderSelector.class)
    @Bean(destroyMethod = "close")
    public SchedulingLeaderSelector schedulingLeaderSelector(EasyJobProperties properties,
                                                             Environment env,
                                                             CuratorFramework client,
                                                             SchedulingManager schedulingManager) {
        String leaderPath = EasyJobConst.DEFAULT_LEADER_PATH + properties.getCluster().getName() + "/" + properties.getCluster().getAppId();
        String id = NetworkUtils.getPreferredIpAddress(properties.getPreferredNetworks()) + ":" + env.getProperty("server.port", "8080");
        SchedulingLeaderSelector leaderSelector = new SchedulingLeaderSelector(client, leaderPath, id, schedulingManager);
        leaderSelector.start();
        logger.info("启动EasyJob成功");
        return leaderSelector;
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            matchIfMissing = true, havingValue = "true"
    )
    @Bean
    public ModeledFramework<SchedulingConfig> modeledClient(EasyJobProperties properties,
                                                            CuratorFramework client) {
        JacksonModelSerializer<SchedulingConfig> serializer = JacksonModelSerializer.build(SchedulingConfig.class);
        ZPath path = ZPath.parseWithIds(EasyJobConst.DEFAULT_CONFIG_PATH + "/" + properties.getCluster().getName() + "/" + properties.getCluster().getAppId());
        ModelSpec<SchedulingConfig> modelSpec = ModelSpec.builder(path, serializer).build();
        ModeledFramework<SchedulingConfig> modeledClient = ModeledFramework.wrap(AsyncCuratorFramework.wrap(client), modelSpec);
        logger.info("启动easyjob定时任务配置监听器");
        return modeledClient;
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            matchIfMissing = true, havingValue = "true"
    )
    @ConditionalOnMissingBean(SchedulingConfigService.class)
    @Bean
    public SchedulingConfigService zkSchedulingConfigService(ModeledFramework<SchedulingConfig> modeledClient) {
        return new ZkSchedulingConfigServiceImpl(modeledClient);
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            matchIfMissing = true, havingValue = "false"
    )
    @ConditionalOnMissingBean(SchedulingConfigService.class)
    @Bean
    public SchedulingConfigService defaultSchedulingConfigService(SchedulingManager schedulingManager) {
        return new DefaultSchedulingConfigServiceImpl(schedulingManager);
    }

    @ConditionalOnMissingBean(SpringContextUtils.class)
    @Bean
    public SpringContextUtils springContextUtils() {
        return new SpringContextUtils();
    }

    @ConditionalOnMissingBean(SchedulingLogEventListener.class)
    @Bean
    public SchedulingLogEventListener schedulingLogEventListener(ObjectProvider<SchedulingLogProcessor> schedulingLogProcessor) {
        return new SchedulingLogEventListener(schedulingLogProcessor);
    }
}
