package com.webank.wedatasphere.streamis.jobmanager.manager.utils;

import com.google.gson.JsonSyntaxException;
import com.webank.wedatasphere.streamis.jobmanager.launcher.job.conf.JobConf;
import com.webank.wedatasphere.streamis.jobmanager.manager.entity.vo.JobHighAvailableVo;
import com.webank.wedatasphere.streamis.jobmanager.manager.project.service.impl.ProjectPrivilegeServiceImpl;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SourceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SourceUtils.class);

    public static JobHighAvailableVo manageJobProjectFile(String highAvailablePolicy, String source) {
        highAvailablePolicy = Optional.ofNullable(highAvailablePolicy).orElse(JobConf.HIGHAVAILABLE_DEFAULT_POLICY().getHotValue());
        JobHighAvailableVo highAvailableVo = new JobHighAvailableVo();
        if (!Boolean.parseBoolean(JobConf.HIGHAVAILABLE_ENABLE().getValue().toString())) {
            highAvailableVo.setHighAvailable(true);
            highAvailableVo.setMsg("管理员未开启高可用配置");
            return highAvailableVo;
        } else {
            if (highAvailablePolicy.equals(JobConf.HIGHAVAILABLE_POLICY().getValue())) {
                Map map = new HashMap<>();
                try {
                    map = BDPJettyServerHelper.gson().fromJson(source, Map.class);
                } catch (JsonSyntaxException e) {
                    LOG.error("Failed to source parse JSON");
                }
                if (map.containsKey("source")) {
                    String sourceValue = map.get("source").toString();
                    if (sourceValue.equals(JobConf.HIGHAVAILABLE_SOURCE().getValue())) {
                        if (map.containsKey("isHighAvailable")) {
                            highAvailableVo.setHighAvailable(Boolean.parseBoolean(map.get("isHighAvailable").toString()));
                        }
                        highAvailableVo.setMsg(map.getOrDefault("highAvailableMessage", "高可用信息为空，请联系管理员").toString());
                        return highAvailableVo;
                    } else {
                        highAvailableVo.setHighAvailable(false);
                        highAvailableVo.setMsg("非标准来源,不检测高可用,source: " +source);
                        return highAvailableVo;
                    }
                } else {
                    highAvailableVo.setHighAvailable(true);
                    highAvailableVo.setMsg("不存在来源信息，无法检测高可用");
                    return highAvailableVo;
                }
            } else {
                highAvailableVo.setHighAvailable(true);
                highAvailableVo.setMsg("任务未开启高可用");
                return highAvailableVo;
            }
        }
    }
}
