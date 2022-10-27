package com.webank.wedatasphere.streamis.jobmanager.manager.material

import com.webank.wedatasphere.streamis.jobmanager.manager.dao.StreamFileMapper
import com.webank.wedatasphere.streamis.jobmanager.manager.entity.{StreamContainerFile, StreamisFile}
import com.webank.wedatasphere.streamis.jobmanager.manager.material.StreamFileLocalContainer.{FILE_OWNER, STORE_PATH}
import com.webank.wedatasphere.streamis.jobmanager.manager.service.BMLService
import com.webank.wedatasphere.streamis.jobmanager.manager.util.ZipHelper
import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{JsonUtils, Logging}

import java.io.{File, FilenameFilter}
import java.util
import java.util.Calendar
import javax.annotation.{PostConstruct, Resource}
import scala.collection.JavaConverters.{asScalaBufferConverter, seqAsJavaListConverter}
/**
 * Local file container
 */
trait StreamFileLocalContainer extends StreamFileContainer with Logging{

  @Resource
  protected var bmlService: BMLService = _

  @Resource
  protected var streamFileMapper: StreamFileMapper = _
  /**
   * Resources contained
   */
  protected val resources: util.List[FileResource] = new util.ArrayList[FileResource]
  /**
   * Container path (composed by store path and container name)
   * @return
   */
  def getContainerPath: String = {
      val storePath = STORE_PATH.getValue
      if (storePath.endsWith("/")) storePath + getContainerName
      else storePath + "/" + getContainerName
    }

  @PostConstruct
  def init(): Unit ={
     val container = getContainerName
     info(s"Start to init stream file container: [${container}")
     val path = getContainerPath
     info(s"Scan the container path [${path}")
     val filesInContainer = Option(scanFileContainerPath(path: String))
       .getOrElse(new util.ArrayList[File]())
     if (!filesInContainer.isEmpty){
        val streamFileList = streamFileMapper.getFilesByContainerName(container)
        val streamFiles = streamFileList.asScala.map(streamFile => (streamFile.getFileName, streamFile)).toMap
        this.resources.addAll(filesInContainer.asScala.map(file => {
            streamFiles.get(file.getName) match {
              case Some(streamFile) =>
                if (streamFile.getUpdateTime.getTime < file.lastModified()){
                    // Ignore the other store type
                    if (StreamisFile.BML_STORE_TYPE.equals(streamFile.getStoreType)){
                        var storePath:util.Map[String, Object] = JsonUtils.jackson.readValue(streamFile.getStorePath, classOf[util.Map[String, Object]])
                        storePath = Option(storePath.get("resourceId")) match {
                          case Some(resourceId) =>
                            info(s"Update the bml resource: [id: ${resourceId}, version:[${storePath.get("version")}] from local: [${file.getAbsolutePath}]")
                            bmlService.update(streamFile.getCreateBy, resourceId.asInstanceOf[String], file.getAbsolutePath)
                          case  _ =>
                            info(s"Upload the local path:[${file.getAbsolutePath}] to bml")
                            bmlService.upload(streamFile.getCreateBy, file.getAbsolutePath)
                        }
                        streamFile.setStorePath(JsonUtils.jackson.writeValueAsString(storePath))
                        streamFile.setUpdateTime(Calendar.getInstance.getTime)
                        this.streamFileMapper.updateFileInContainer(streamFile)
                    }
                }
                FileResource(file, streamFile)
              case _ =>
                info(s"Upload the local path:[${file.getAbsolutePath}] to bml")
                val storePath = bmlService.upload(FILE_OWNER.getValue, file.getAbsolutePath)
                val streamFile = new StreamContainerFile(container, file.getName,
                  JsonUtils.jackson.writeValueAsString(storePath))
                this.streamFileMapper.storeFileInContainer(streamFile)
                FileResource(file, streamFile)
            }
        }).toList.asJava)
     }
  }


  /**
   * Get stream files
   *
   * @return
   */
  override def getStreamFiles: util.List[StreamisFile] = {
    this.resources.asScala.map(resource => resource.streamisFile).asJava
  }

  /**
   * Get stream files by match function
   *
   * @param matchFunc match function
   * @return
   */
  override def getStreamFiles(matchFunc: StreamisFile => Boolean): util.List[StreamisFile] = {
     val matchFiles: util.List[StreamisFile] = new util.ArrayList[StreamisFile]()
     this.resources.asScala.foreach(resource => {
       if (matchFunc.apply(resource.streamisFile)){
         matchFiles.add(resource.streamisFile)
       }
     })
     matchFiles
  }

  /**
   * Get stream file by basename, model name and suffix
   *
   * @param name   name
   * @param model  model
   * @param suffix suffix
   * @return
   */
  override def getStreamFile(name: String, model: String, suffix: String): StreamisFile = {
    if (StringUtils.isNotBlank(name)) {
      var fileName = name
      if (StringUtils.isNotBlank(model)){
        fileName += model
      }
      if (StringUtils.isNotBlank(suffix)){
        fileName += suffix
      }
      this.resources.asScala.find(_.localFile.getName.equals(fileName)) match {
        case Some(resource: FileResource) =>
          resource.streamisFile
        case _ => null
      }
    } else null
  }
/**
   * Scan the file container and zip/unzip the directory or package
   * @param path path
   * @return the fil
   */
  def scanFileContainerPath(path: String): util.List[File] = {
     val rootPath = new File(path)
     val fileList = new util.ArrayList[File]()
     if (rootPath.isDirectory){
        Option(rootPath.listFiles(new FilenameFilter {
          override def accept(dir: File, name: String): Boolean = !name.startsWith(".")
        })) match {
          case Some(localFiles) =>
            val skipFiles = new util.ArrayList[String]()
            // Scan the directories
            localFiles.filter(localFile => localFile.isDirectory)
              .foreach(dir => {
                  val dirTs = extractFileTime(dir, -1)
                  val zipFile = new File(dir.getParent, dir.getName + ".zip")
                  if ( (zipFile.exists() && zipFile.lastModified() < dirTs)
                        || !zipFile.exists()){
                     // Packet the directory
                     ZipHelper.packet(dir.toPath, zipFile.toPath)
                  }
                  fileList.add(zipFile)
                  skipFiles.add(zipFile.getName)
              })
            // Scan the regular files
            localFiles.filter(localFile => localFile.isFile && !skipFiles.contains(localFile.getName))
              .foreach(file => {
                 if (ZipHelper.isZip(file.getName)){
                    // Unpack to directory
                    ZipHelper.unPacket(file.toPath, file.toPath
                      .resolveSibling(StringUtils.substringBefore(file.getName, ".")))
                   // Modify the time
                   file.setLastModified(System.currentTimeMillis());
                 }
                 fileList.add(file)
              })
          case _ =>
        }
     }
     fileList
  }

  /**
   * Extract file timestamp
   * @param subFile sub file
   * @param timeStamp timestamp
   * @return
   */
  private def extractFileTime(subFile: File, timeStamp: Long): Long = {
     if (subFile.lastModified() > timeStamp){
        subFile.lastModified()
     } else if (subFile.isDirectory){
        Option(subFile.listFiles()) match {
          case Some(childFiles) =>
             childFiles.map(extractFileTime(_, timeStamp)).max
          case _ => timeStamp
        }
     } else {
       timeStamp
     }
  }

}

/**
 * To hold the connection between remote and local file
 * @param localFile local file
 * @param streamisFile streamis file (stored in bml)
 */
case class FileResource(localFile: File, streamisFile: StreamisFile)


object StreamFileLocalContainer{

  val FILE_OWNER: CommonVars[String] = CommonVars("wds.streamis.job.material.container.local.file-owner", System.getProperty("user.name", "hadoop"))
  val STORE_PATH: CommonVars[String] = CommonVars("wds.streamis.job.material.container.local.store-path", "material")
}