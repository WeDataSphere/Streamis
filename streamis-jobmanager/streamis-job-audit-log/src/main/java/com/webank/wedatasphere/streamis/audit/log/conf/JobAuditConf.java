package com.webank.wedatasphere.streamis.audit.log.conf;

import org.apache.linkis.common.conf.CommonVars;

public class JobAuditConf {

    public static final CommonVars<Boolean> AUDIT_LOG_ENABLE = CommonVars.apply("wds.streamis.log.audit.store.enable", true);

    /*
    divided by comma
     */
    public static final CommonVars<String> AUDIT_LOG_URI_SKIP = CommonVars.apply("wds.streamis.log.audit.uri.skip", "/api/rest_j/v1/streamis/streamJobManager/log/collect/events,/api/rest_j/v1/streamis/streamJobManager/log/heartbeat,/api/rest_j/v1/streamis/streamProjectManager/project/files/list,/api/rest_j/v1/streamis/streamProjectManager/project/files/version/list,/api/rest_j/v1/streamis/streamJobManager/job/list,/api/rest_j/v1/streamis/streamJobManager/job/execute/history,/api/rest_j/v1/streamis/streamJobManager/job/logs,/api/rest_j/v1/streamis/streamJobManager/job/alert,/api/rest_j/v1/streamis/streamJobManager/job/status,/api/rest_j/v1/streamis/streamJobManager/highAvailable/username");


}
