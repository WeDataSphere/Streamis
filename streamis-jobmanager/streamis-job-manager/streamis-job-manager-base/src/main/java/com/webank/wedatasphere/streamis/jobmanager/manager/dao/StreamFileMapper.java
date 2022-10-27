package com.webank.wedatasphere.streamis.jobmanager.manager.dao;

import com.webank.wedatasphere.streamis.jobmanager.manager.entity.StreamContainerFile;

import java.util.List;

/**
 * Mapper of stream file(material)
 */
public interface StreamFileMapper {

    /**
     * Store the information of file from container
     * @param streamisFile streamis file
     */
    void storeFileInContainer(StreamContainerFile streamisFile);

    /**
     * Update the information of file from container
     * @param streamisFile streamis file
     */
    void updateFileInContainer(StreamContainerFile streamisFile);

    /**
     * Get the container files that stored in db
     * @param container container
     * @return streamis files
     */
    List<StreamContainerFile> getFilesByContainerName(String container);
}
