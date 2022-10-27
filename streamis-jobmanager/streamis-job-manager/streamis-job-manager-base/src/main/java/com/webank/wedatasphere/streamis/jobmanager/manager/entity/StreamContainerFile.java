package com.webank.wedatasphere.streamis.jobmanager.manager.entity;

import java.util.Calendar;
import java.util.Date;

/**
 * File from container
 */
public class StreamContainerFile implements StreamisFile {

    private Long id;

    private String fileName;

    /**
     * Name of container
     */
    private String containerName;

    /**
     * Store path
     */
    private String storePath;
    /**
     * BML
     */
    private String storeType = StreamisFile.BML_STORE_TYPE;

    /**
     * Update time
     */
    private Date updateTime;

    private Date createTime;

    private String createBy;

    public StreamContainerFile(){

    }

    public StreamContainerFile(String containerName, String fileName, String storePath){
        this.containerName = containerName;
        this.fileName = fileName;
        this.storePath = storePath;
        Calendar calendar = Calendar.getInstance();
        this.createTime = calendar.getTime();
        this.updateTime = calendar.getTime();
    }
    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getVersion() {
        return "--";
    }

    @Override
    public String getStorePath() {
        return this.storePath;
    }

    @Override
    public String getStoreType() {
        return this.storeType;
    }

    @Override
    public String getCreateBy() {
        return this.createBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
