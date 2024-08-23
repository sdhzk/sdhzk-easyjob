package com.sdhzk.easyjob.samples.controller;

import com.sdhzk.easyjob.core.config.SchedulingConfig;
import com.sdhzk.easyjob.core.config.SchedulingConfigService;
import com.sdhzk.easyjob.core.manager.SchedulingManager;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Linus.Lee
 * @date 2024-8-22
 */
@RestController
public class MyAppController {
    @Resource
    private SchedulingConfigService schedulingConfigService;

    @Resource
    private SchedulingManager schedulingManager;

    @PostMapping("/update")
    public String update(@RequestBody SchedulingConfig config) {
        schedulingConfigService.update(config);
        return "ok";
    }

    @PostMapping("/delete")
    public String delete(String jobKey) {
        schedulingConfigService.delete(jobKey);
        return "ok";
    }

    @PostMapping("/stopTask")
    public String stopTask(String jobKey) {
        schedulingManager.stopTask(jobKey);
        return "ok";
    }

    @PostMapping("/startTask")
    public String startTask(String jobKey) {
        schedulingManager.startTask(jobKey);
        return "ok";
    }
}
