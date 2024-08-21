package com.sdhzk.easyjob.samples;

import com.sdhzk.easyjob.core.log.SchedulingLog;
import com.sdhzk.easyjob.core.log.SchedulingLogProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Linus.Lee
 * @date 2024-8-21
 */
@Component
public class MyAppSchedulingLogProcessor implements SchedulingLogProcessor {
    @Override
    public void process(SchedulingLog log) {
        System.out.println("接收到日志：" + log);
    }
}
