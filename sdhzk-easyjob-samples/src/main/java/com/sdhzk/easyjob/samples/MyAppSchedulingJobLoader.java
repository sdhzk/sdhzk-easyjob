package com.sdhzk.easyjob.samples;

import com.sdhzk.easyjob.core.job.SchedulingJob;
import com.sdhzk.easyjob.core.loader.SchedulingJobLoader;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Linus.Lee
 * @date 2024-8-21
 */
@Component
public class MyAppSchedulingJobLoader implements SchedulingJobLoader {

    @Override
    public List<SchedulingJob> load() {
        MyJob myJob = new MyJob();
        myJob.setJobKey("MyJob");
        myJob.setJobName("MyJob");
        myJob.setJobParams(null);
        myJob.setCron("0/10 * * * * ?");
        myJob.setStatus(true);
        myJob.setLogEnabled(true);
        return List.of(myJob);
    }
}
