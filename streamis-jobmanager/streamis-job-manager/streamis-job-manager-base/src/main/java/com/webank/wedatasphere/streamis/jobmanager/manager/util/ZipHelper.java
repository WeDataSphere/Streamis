/*
 * Copyright 2021 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.streamis.jobmanager.manager.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipHelper {
    private static final Logger logger = LoggerFactory.getLogger(ZipHelper.class);
    private static final String ZIP_CMD = "zip";
    private static final String UN_ZIP_CMD = "unzip";
    private static final String RECURSIVE = "-r";
    private static final String ZIP_TYPE = ".zip";
    private static final Integer BUFFER_SIZE = 2 * 1024;

    public static String unzip(String dirPath)throws Exception { //"D:\\tmp\\streamis\\20210922\\johnnwang\\ab_175950\\ab.zip"
        File file = new File(dirPath);
        if(!file.exists()){
            logger.error("{} does not exist, can not unzip", dirPath);
            throw new Exception(dirPath + " does not exist, can not unzip");
        }
        //First use simple approach, call new process to zip(先用简单的方法，调用新进程进行压缩)
        String[] strArr = dirPath.split(File.separator);
        String shortPath = strArr[strArr.length - 1];
        String workPath = dirPath.substring(0, dirPath.length() - shortPath.length() - 1);
        List<String> list = new ArrayList<>();
        list.add(UN_ZIP_CMD);
        String longZipFilePath = dirPath.replace(ZIP_TYPE,"");
        list.add(shortPath);
        ProcessBuilder processBuilder = new ProcessBuilder(list);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(workPath));
        BufferedReader infoReader = null;
        BufferedReader errorReader = null;
        try{
            Process process = processBuilder.start();
            infoReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String infoLine = null;
            while((infoLine = infoReader.readLine()) != null){
                logger.info("process output: {} ", infoLine);
            }
            String errorLine = null;
            StringBuilder errMsg = new StringBuilder();
            while((errorLine = errorReader.readLine()) != null){
                if (StringUtils.isNotEmpty(errorLine)){
                    errMsg.append(errorLine).append("\n");
                }
                logger.error("process error: {} ", errorLine);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0){
                throw new Exception(errMsg.toString());
            }
        }catch(final Exception e){
            logger.error("Fail to unzip file(解压缩 zip 文件失败), reason: ", e);
            Exception exception = new Exception(dirPath + " to zip file failed");
            exception.initCause(e);
            throw exception;
        } finally {
            logger.info("generate unzip catalogue{}", workPath);
            IOUtils.closeQuietly(infoReader);
            IOUtils.closeQuietly(errorReader);
        }
        return longZipFilePath;
    }

    public static void packet(Path source, Path target) throws IOException {
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)){
            logger.info("Drop the duplicate file: {}" + target.toFile().getName());
            Files.delete(target);
        }
        Files.createFile(target);
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(target))) {
            packet(source.toFile().getName(), source, zipOutputStream);
        }
        logger.info("Success to packet [{}] to [{}]", source.toFile().getName(), target.toFile().getName());
    }
    /**
     * Un packet
     * @param source source path
     * @param target target path
     * @throws IOException
     */
    public static void unPacket(Path source, Path target) throws IOException {
        if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)){
            ZipFile zipFile = new ZipFile(source.toFile());
            InputStream inputStream = Files.newInputStream(source);
            try(ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry zipEntry = null;
                while (null != (zipEntry = zipInputStream.getNextEntry())) {
                    Path entryPath = target.resolve(zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        if (!Files.isDirectory(entryPath)) {
                            Files.createDirectories(entryPath);
                        }
                    } else {
                        try (InputStream entryStream = zipFile.getInputStream(zipEntry)) {
                            try (OutputStream outputStream = Files.newOutputStream(entryPath, StandardOpenOption.CREATE_NEW)) {
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int pos = -1;
                                while ((pos = entryStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, pos);
                                }
                                outputStream.flush();
                            }
                        }
                    }
                }
            }
            logger.info("Success to un packet [{}] to [{}]", source.toFile().getName(), target.toFile().getName());
        }
    }

    /**
     * Packet path source
     * @param name name
     * @param source source path
     * @param outputStream stream
     * @throws IOException
     */
    private static void packet(String name, Path source, ZipOutputStream outputStream) throws IOException {
        if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)){
            name = name + IOUtils.DIR_SEPARATOR_UNIX;
            // Accept empty directory
            ZipEntry zipEntry = new ZipEntry(name);
            outputStream.putNextEntry(zipEntry);
            outputStream.closeEntry();
            for(Path child : Files.list(source).collect(Collectors.toList())) {
                packet(name + child.toFile().getName(), child, outputStream);
            }
        } else if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)){
            packet(name, Files.newInputStream(source), outputStream);
        }
    }

    /**
     * Packet input  stream
     * @param name name
     * @param inputStream input stream
     * @param outputStream output stream
     * @throws IOException
     */
    private static void packet(String name, InputStream inputStream, ZipOutputStream outputStream) throws IOException{
        if (Objects.nonNull(inputStream)) {
            ZipEntry zipEntry = new ZipEntry(name);
            outputStream.putNextEntry(zipEntry);
            byte[] buffer = new byte[BUFFER_SIZE];
            int pos = -1;
            while ((pos = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, pos);
            }
            outputStream.closeEntry();
        }
    }

    public static boolean isZip(String fileName){
        return fileName.substring(fileName.lastIndexOf('.')).equals(ZIP_TYPE);
    }
}
