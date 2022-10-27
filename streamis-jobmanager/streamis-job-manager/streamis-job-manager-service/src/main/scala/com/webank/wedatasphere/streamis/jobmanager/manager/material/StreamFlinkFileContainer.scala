package com.webank.wedatasphere.streamis.jobmanager.manager.material

import org.springframework.stereotype.Component

/**
 * Flink file container
 */
@Component("flinkFileContainer")
class StreamFlinkFileContainer extends StreamFileLocalContainer {
  /**
   * Container name
   *
   * @return
   */
  override def getContainerName: String = "flink"
}
