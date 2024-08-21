package com.sdhzk.easyjob.core.log;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;

/**
 * @author Linus.Lee
 * @date 2024-8-21
 */
public class SchedulingLogEventListener implements ApplicationListener<SchedulingLog> {
    private final ObjectProvider<SchedulingLogProcessor> schedulingLogProcessor;

    public SchedulingLogEventListener(ObjectProvider<SchedulingLogProcessor> schedulingLogProcessor) {
        this.schedulingLogProcessor = schedulingLogProcessor;
    }

    @Override
    public void onApplicationEvent(SchedulingLog log) {
        SchedulingLogProcessor logProcessor = schedulingLogProcessor.getIfAvailable();
        if(logProcessor != null){
            logProcessor.process(log);
        }
    }
}
