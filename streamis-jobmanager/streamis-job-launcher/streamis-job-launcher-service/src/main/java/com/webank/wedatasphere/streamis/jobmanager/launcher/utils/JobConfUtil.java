package com.webank.wedatasphere.streamis.jobmanager.launcher.utils;

import com.webank.wedatasphere.streamis.jobmanager.launcher.conf.JobConstants;
import com.webank.wedatasphere.streamis.jobmanager.launcher.entity.JobConfValue;
import com.webank.wedatasphere.streamis.jobmanager.launcher.job.conf.JobConfKeyConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class JobConfUtil {

    private JobConfUtil(){}

    public static List<JobConfValue> valuesHandler (List<JobConfValue> jobConfValueList){
        String manageMode = JobConfKeyConstants.MANAGE_MODE_KEY().getValue();
        return jobConfValueList.stream()
                .filter(jobConfValue -> jobConfValue.getKey().equals(manageMode) && jobConfValue.getValue().isEmpty())
                .peek(jobConfValue -> jobConfValue.setValue(JobConstants.MANAGE_MODE_DETACH()))
                .collect(Collectors.toList());
    }


}
