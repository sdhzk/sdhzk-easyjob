package com.sdhzk.easyjob.core.config;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
public interface SchedulingConfigService {
    void update(SchedulingConfig config);

    void delete(String jobKey);
}
