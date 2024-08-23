package com.sdhzk.easyjob.samples;

import com.sdhzk.easyjob.core.job.SchedulingJobAdapter;
import java.time.LocalDateTime;

/**
 * @author Linus.Lee
 * @date 2024-8-21
 */
public class MyJob extends SchedulingJobAdapter {


    @Override
    public void execute() {
        System.out.println("执行MyJob：" + LocalDateTime.now());
    }

    @Override
    public String getJobKey() {
        return "MyJob";
    }

}
