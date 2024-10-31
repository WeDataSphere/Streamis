package com.webank.wedatasphere.streamis.errorcode.test;

import com.webank.wedatasphere.streamis.errorcode.entity.StreamErrorCode;
import com.webank.wedatasphere.streamis.errorcode.handler.StreamisErrorCodeHandler;
import com.webank.wedatasphere.streamis.errorcode.manager.cache.StreamisErrorCodeCache;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestErrorCodeMatch {



    @BeforeAll
    public static void initErrorCodeMap() {
        // init errorcode map
        List<StreamErrorCode> errorCodeList = new ArrayList<>();
        StreamErrorCode errorCode1 = new StreamErrorCode();
        errorCode1.setErrorCode("001");
        errorCode1.setErrorRegex("FileNotFoundException: (\\S+) \\(Permission denied\\)");
        errorCode1.setErrorDesc("文件没有权限: %s");
        errorCode1.setErrorType(1);
        errorCodeList.add(errorCode1);
        StreamisErrorCodeCache.put("data", errorCodeList);
    }

    @Test
    public void matchFilePermission() {
        String errorMsg = "FileNotFoundException: /home/flink/lib/flink-metrics-prometheus_2.11-1.12.2.jar (Permission denied)";
        List<StreamErrorCode> result = StreamisErrorCodeHandler.getInstance().handle(errorMsg);
        assertNotNull(result);
        System.out.println(result.size());
        StreamErrorCode firstCode = result.get(0);
        System.out.println(firstCode.getErrorDesc());

    }


}
