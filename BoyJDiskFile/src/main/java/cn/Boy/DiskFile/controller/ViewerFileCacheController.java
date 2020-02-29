package cn.Boy.DiskFile.controller;


import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.distributeFileEntry.AbsCommonFileOperator;
import cn.Boy.DiskFile.distributeFileEntry.CephViewerFileCacheManager;
import cn.Boy.DiskFile.distributeFileEntry.ICommonFileMetaInfoRecorder;
import cn.Boy.DiskFile.pojo.DocumentList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class ViewerFileCacheController {

    @Value("${DiskFile.viewerCacheFileFolder:target/uploadFileCache/viewerCache}") String viewerCacheFileFolder;
    @Value("${spring.mvc.static-path-pattern:/nodeFile/}") String staticFilePath;

    @Autowired
    @Qualifier("CephFileOperator")
    private AbsCommonFileOperator cephFileOperator;

    @Autowired
    @Qualifier("CephFileMetaInfoRecord")
    private ICommonFileMetaInfoRecorder cephFileMetaInfoRecord;

    @Autowired
    private CommonHelper commonHelper;

    private final Log log = LogFactory.getLog(ViewerFileCacheController.class);

    @RequestMapping(value="/nodeFileTemp/{viewerCacheFileName}",method = {RequestMethod.POST,RequestMethod.GET})
    public String getViewerCacheFile(@PathVariable String viewerCacheFileName,
                                     @RequestParam(value = "fextId") long platformfileExtID,
                                     @RequestParam(value = "docTid") long docTypeId,
                                     @RequestParam(value = "fName") String fileName,
                                     @RequestParam(value = "fPath") String filePath,
                                     @RequestParam(value = "V") long version,
                                     @RequestParam(value = "mslt") Date memorySeaLevelTime,
                                     @RequestParam(value = "mlts") long memoryLiveTimeSec,
                                     @RequestParam(value = "mht") long memoryHitTimes,
                                     @RequestParam(value = "mphcus") long memoryPerHitComeUpSeconds,
                                     @RequestParam(value = "sct") int storageClusterType,
                                     @RequestParam(value = "ugid") String uploadedByPlatformUserGuid,
                                     @RequestParam(value = "clss") int clusterStorageStatus,
                                     @RequestParam(value = "vctl") long viewerCacheTimeLength){

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            String fullViewerCacheFileName = viewerCacheFileFolder + File.separatorChar + viewerCacheFileName;
            File localViewerCacheFile = new File(fullViewerCacheFileName);




            //verify whether the request file was under local viewerCachefile folder
            if (!localViewerCacheFile.exists()) {
                //if not existed,verify whether existed under ceph cluster,if existed download it from ceph cluster.
                if (cephFileOperator.fileOrDir(fullViewerCacheFileName).equals("file")) {
                    //after download,redirect.
                    if (cephFileOperator.seekFromCluster(viewerCacheFileFolder, viewerCacheFileName, new FileOutputStream(localViewerCacheFile))) {

                        //seek out the document per input parameter
                        ThreadLocalContext.setDbKey("main");

                        List<DocumentList> dcList=new ArrayList<>();
                        dcList= cephFileMetaInfoRecord.getAllDocumentList().stream().filter(x->x.getFileName().equals(fileName) && x.getFilePath().equals(filePath) && x.getDocTypeId().equals(docTypeId) && x.getPlatformfileExtID().equals(platformfileExtID) && x.getUploadedByPlatformUserGuid().equals(uploadedByPlatformUserGuid)).collect(Collectors.toList());
                          //if existed. update the viewer file cache into NodeDb.
                          if(dcList.size()>0){
                              log.trace("enter dcList.size>0");
                              dcList.get(0).setViewerCacheFileName(viewerCacheFileName);
                              dcList.get(0).setViewerCacheTime(new Date());
                              dcList.get(0).setViewerCacheTimeLength(viewerCacheTimeLength);
                              cephFileMetaInfoRecord.updateDocumentList(dcList.get(0));
                          }else {
                              log.trace("enter dcList.size<=0");
                              //if not existed, add the viewer file cache with doc parameter into NodeDb.
                               DocumentList newDcList=new DocumentList();
                               newDcList.setDocGuid(CommonHelper.getInstance().getNodeUUID());
                               newDcList.setDocTypeId(docTypeId);
                               newDcList.setPlatformfileExtID(platformfileExtID);
                               newDcList.setFileName(fileName);
                               newDcList.setFilePath(filePath);
                               newDcList.setFileSize(commonHelper.getMbFromByte(localViewerCacheFile.length()));
                               newDcList.setVersion(version);
                               newDcList.setCreateTime(new Date());
                               newDcList.setMemorySeaLevelTime(memorySeaLevelTime);
                               newDcList.setMemoryLiveTimeSec(memoryLiveTimeSec);
                               newDcList.setMemoryHitTimes(memoryHitTimes);
                               newDcList.setMemoryPerHitComeUpSeconds(memoryPerHitComeUpSeconds);
                               newDcList.setStorageClusterType(storageClusterType);
                               newDcList.setUploadedByPlatformUserGuid(uploadedByPlatformUserGuid);
                               newDcList.setClusterStorageStatus(clusterStorageStatus);
                               newDcList.setFileExtName(fileName.split("\\.")[1]);
                               newDcList.setViewerCacheFileName(viewerCacheFileName);
                               newDcList.setViewerCacheTime(new Date());
                               newDcList.setViewerCacheTimeLength(viewerCacheTimeLength);
                              cephFileMetaInfoRecord.addDocumentList(newDcList);
                          }


                        byte[] buffer=new byte[1024];
                        FileInputStream fis=null;
                        BufferedInputStream bis=null;
                        try{
                            fis= new FileInputStream(localViewerCacheFile);
                            bis= new BufferedInputStream(fis);
                            OutputStream os=response.getOutputStream();
                            int i=bis.read(buffer);
                            while (i!=-1){
                                os.write(buffer,0,i);
                                i=bis.read(buffer);
                            }
                            log.trace("show file successfully!");
                        }catch (Exception e){

                            log.trace("show file failed!");
                        }finally
                        {
                            if (bis != null) {
                                try {
                                    bis.close();
                                } catch (IOException e) {
                                    log.trace(e.getMessage());
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    log.trace(e.getMessage());
                                }
                            }
                        }
                        return "";
                    } else {
                        return "Request resource was not avaliable.";
                    }
                } else {
                    //if not existed under ceph cluster also,return "request resource was not avaliable."
                    return "Request resource was not avaliable.";
                }
            } else {
                   //if existed, show it directly.
                    byte[] buffer=new byte[1024];
                    FileInputStream fis=null;
                    BufferedInputStream bis=null;
                    try{
                        fis= new FileInputStream(localViewerCacheFile);
                        bis= new BufferedInputStream(fis);
                        OutputStream os=response.getOutputStream();
                        int i=bis.read(buffer);
                        while (i!=-1){
                            os.write(buffer,0,i);
                            i=bis.read(buffer);
                        }
                        log.trace("show file successfully!");
                    }catch (Exception e){

                        log.trace("show file failed!");
                    }finally
                    {
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException e) {
                                log.trace(e.getMessage());
                            }
                        }
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                log.trace(e.getMessage());
                            }
                        }
                    }
                return "";
            }
        }catch (FileNotFoundException e){

            log.trace(e.toString());
            return "Request resource was not avaliable.";

        }catch (IOException e){

            log.trace(e.toString());
            return "Request resource was not avaliable.";
        }

    }
}
