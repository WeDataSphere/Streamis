package com.webank.wedatasphere.streamis.jobmanager.manager.transform.parser

import com.webank.wedatasphere.streamis.jobmanager.manager.entity.{StreamJob, StreamJobVersion, StreamisFile}
import com.webank.wedatasphere.streamis.jobmanager.manager.transform.parser.AbstractFlinkJobContentParser.CONF_FILE_NAME
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Utils

abstract class AbstractFlinkJobContentParser extends AbstractJobContentParser{

  def getConfigFile(job: StreamJob, jobVersion: StreamJobVersion): StreamisFile = {
    Utils.tryQuietly(this.findFile(job, jobVersion, CONF_FILE_NAME.getValue))
  }
}
object AbstractFlinkJobContentParser{
  /**
   * Config file name
   */
  val CONF_FILE_NAME: CommonVars[String] = CommonVars.apply("wds.streamis.job.flink.conf-file", "flink-conf.zip")
}
