package com.sdhzk.easyjob.spring.boot.autoconfigure;

import com.sdhzk.easyjob.core.EasyJobConst;
import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigListener;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import com.sdhzk.easyjob.core.config.impl.DefaultSchedulingConfigServiceImpl;
import com.sdhzk.easyjob.core.config.impl.ZkSchedulingConfigServiceImpl;
import com.sdhzk.easyjob.core.leader.SchedulingLeaderSelector;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoader;
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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

/**
 * @author Linus.Lee
 * @date 2024-8-20
 */
@ConditionalOnProperty(
        name = "easyjob.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableScheduling
@AutoConfiguration
@EnableConfigurationProperties(EasyJobProperties.class)
public class EasyJobAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EasyJobAutoConfiguration.class);

    @ConditionalOnMissingBean(SchedulingManager.class)
    @Bean
    public SchedulingManager schedulingManager(EasyJobProperties properties,
                                               ObjectProvider<ModeledFramework<SchedulingConfig>> modeledClient,
                                               ObjectProvider<SchedulingConfigService> schedulingConfigService,
                                               ObjectProvider<SchedulingJobLoader> schedulingJobLoader) {
        if (schedulingJobLoader.getIfAvailable() == null) {
            throw new IllegalStateException("SchedulingJobLoader不能为空");
        }
        SchedulingManager schedulingManager = new SchedulingManager();
        schedulingManager.setClustered(properties.getCluster().getEnabled());
        schedulingManager.setSchedulingJobLoader(schedulingJobLoader.getIfAvailable());
        schedulingManager.setCorePoolSize(properties.getThreadPool().getCorePoolSize());
        schedulingManager.setMaxPoolSize(properties.getThreadPool().getMaxPoolSize());
        schedulingManager.setKeepAliveSeconds(properties.getThreadPool().getKeepAliveSeconds());
        if (schedulingManager.isClustered()) {
            ModeledFramework<SchedulingConfig> modeledFramework = modeledClient.getIfAvailable();
            if (Objects.isNull(modeledFramework)) {
                throw new IllegalStateException("ModeledFramework未配置");
            }
            CachedModeledFramework<SchedulingConfig> cached = modeledFramework.cached();
            cached.listenable().addListener(new SchedulingConfigListener(schedulingManager));
            cached.start();
            schedulingManager.setSchedulingJobLoaderListener(new ZkSchedulingJobLoaderListener(schedulingConfigService.getIfAvailable()));
        }

        return schedulingManager;
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            havingValue = "true",
            matchIfMissing = true
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
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnMissingBean(SchedulingLeaderSelector.class)
    @Bean(destroyMethod = "close")
    public SchedulingLeaderSelector schedulingLeaderSelector(EasyJobProperties properties,
                                                             Environment env,
                                                             CuratorFramework client,
                                                             SchedulingManager schedulingManager) {
        String leaderPath = EasyJobConst.DEFAULT_LEADER_PATH + "/" + properties.getCluster().getName() + "/" + properties.getCluster().getAppId();
        String id = NetworkUtils.getPreferredIpAddress(properties.getPreferredNetworks()) + ":" + env.getProperty("server.port", "8080");
        SchedulingLeaderSelector leaderSelector = new SchedulingLeaderSelector(client, leaderPath, id, schedulingManager);
        leaderSelector.start();
        logger.info("启动EasyJob成功");
        return leaderSelector;
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            havingValue = "true",
            matchIfMissing = true
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
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnMissingBean(SchedulingConfigService.class)
    @Bean
    public SchedulingConfigService zkSchedulingConfigService(ModeledFramework<SchedulingConfig> modeledClient) {
        return new ZkSchedulingConfigServiceImpl(modeledClient);
    }

    @ConditionalOnProperty(
            name = "easyjob.cluster.enabled",
            havingValue = "true",
            matchIfMissing = true
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
