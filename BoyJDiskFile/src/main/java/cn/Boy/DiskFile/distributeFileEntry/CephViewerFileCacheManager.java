package cn.Boy.DiskFile.distributeFileEntry;


import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.pojo.DocumentList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("CephViewerFileCacheManager")
public class CephViewerFileCacheManager implements IViewerFileCacheManager {

    @Value("${DiskFile.viewerCacheFileFolder:target/uploadFileCache/viewerCache}") String viewerCacheFileFolder;
    @Value("${platformArch.ViewerFileCache.defaultViewerCacheLength}")  long  defaultViewerCacheLength;

    @Autowired
    @Qualifier("CephFileMetaInfoRecord")
    private ICommonFileMetaInfoRecorder cephFileMetaInfoRecord;

    @Autowired
    @Qualifier("CephFileOperator")
    private AbsCommonFileOperator cephFileOperator;

    @Autowired
    private CommonHelper commonHelper;

    private final Log log = LogFactory.getLog(CephViewerFileCacheManager.class);

    private int lastClusterViewerFileCacheNum=0;

    @Override
    public boolean setViewerFileCache(DocumentList documentList, String viewerCacheFileName, Date viewerCacheTime, long viewerCacheTimeLength,File viewerCacheFile) {

        try {
             //verify whether the viewerFileCache dir has been under ceph cluster,if not existed,make it.
            if(!cephFileOperator.fileOrDir(viewerCacheFileFolder).equals("dir")
                &&!cephFileOperator.fileOrDir(viewerCacheFileFolder).equals("error"))
            {
                 if(cephFileOperator.mkDir(viewerCacheFileFolder).length <=0){
                     return false;
                 }
            }

            //verify whether the viewerFileCache has been under ceph cluster
            if (!cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName).equals("file")
                &&!cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName).equals("error") )
            {
                //if not existed,upload it to ceph cluster.

                if (cephFileOperator.writeToCluster(viewerCacheFileFolder, viewerCacheFileName, new FileInputStream(viewerCacheFile), viewerCacheFile.length())) {
                    //Then set them and save it into nodeDB,if success return true,if failed return false
                    documentList.setViewerCacheFileName(viewerCacheFileName);
                    documentList.setViewerCacheTime(viewerCacheTime);
                    documentList.setViewerCacheTimeLength(viewerCacheTimeLength);

                    if (cephFileMetaInfoRecord.updateDocumentList(documentList) != null) {
                        return true;
                    } else {
                        log.trace("set viewer file cache to db failed");
                        return false;
                    }
                } else {
                    return false;
                }
            } else if(cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName).equals("file"))
            {
                    //Then set them and save it into nodeDB,if success return true,if failed return false
                    documentList.setViewerCacheFileName(viewerCacheFileName);
                    documentList.setViewerCacheTime(viewerCacheTime);
                    documentList.setViewerCacheTimeLength(viewerCacheTimeLength);

                    if (cephFileMetaInfoRecord.updateDocumentList(documentList) != null) {
                        return true;
                    } else {
                        log.trace("set viewer file cache to db failed");
                        return false;
                    }
            }else {

                    return false;
            }
        }
        catch (FileNotFoundException e){

            log.trace(e.getStackTrace());
            return false;
        }
        catch (IOException e){

            log.trace(e.getStackTrace());
            return false;
        }


    }

    @Override
    public String getViewerFileCache(DocumentList documentList,long viewerCacheTimeLength) {

        //if viewerCacheFileName is not null and viewerCacheTime is not expired at this moment,then return back viewerCacheFileName
         if(documentList.getViewerCacheFileName()!=null&&documentList.getViewerCacheTime()!=null&&documentList.getViewerCacheTimeLength()!=null){
             Date now=new Date();
             Date compareDeadLineTime=new Date(documentList.getViewerCacheTime().getTime()+documentList.getViewerCacheTimeLength()*60*1000);
             if(!now.after(compareDeadLineTime)) {
                 return documentList.getViewerCacheFileName();

             }else {
                 //if found cache expired during execute get,remove the viewer file cache from ceph cluster,remove the viewer file cache on local file folder,remove the viewer file cache on nodeDb and return "" to trigger new viewer file cache.
                 if(cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + documentList.getViewerCacheFileName()).equals("file"))
                 {
                      cephFileOperator.delFile(viewerCacheFileFolder + File.separatorChar + documentList.getViewerCacheFileName() ,viewerCacheFileFolder);
                 }
                 //remove the viewer file cache on local file folder
                 File viewerCacheFile= new File(viewerCacheFileFolder,documentList.getViewerCacheFileName());
                 viewerCacheFile.delete();

                 //remove the viewer file cache on nodeDb
                 documentList.setViewerCacheFileName(null);
                 documentList.setViewerCacheTime(null);
                 documentList.setViewerCacheTimeLength(null);
                 cephFileMetaInfoRecord.updateDocumentList(documentList);
                 return "";
             }
         }else {
             //verify whether the ViewerCacheFile has been existed under ceph cluster.
             try {
                 //if existed,download it into local viewerCacheFile Folder from cluster & save the cache to NodeDb.
                 String viewerCacheFileName = commonHelper.getHashViewerCacheFileName(documentList.getFilePath(), documentList.getFileName(), documentList.getUploadedByPlatformUserGuid());
                 if (cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName).equals("file")) {

                     File viewerCacheFile = new File(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName);
                     //if it has been existed under local viewerCacheFile folder,save it into nodeDb ViewerCacheFile flag column and return back the viewerCacheFileName directly.
                     if(viewerCacheFile.exists()){
                         documentList.setViewerCacheFileName(viewerCacheFileName);
                         documentList.setViewerCacheTime(new Date(viewerCacheFile.lastModified()));
                         documentList.setViewerCacheTimeLength(viewerCacheTimeLength);
                         if(cephFileMetaInfoRecord.updateDocumentList(documentList)!=null){
                             return viewerCacheFileName;
                         }else {
                             log.trace("set viewer file cache to db failed");
                             return "";
                         }
                     }
                     //if it has not been existed under local viewerCacheFile folder,download it into local viewerCacheFile Folder from cluster & save the cache to NodeDb.
                     if (cephFileOperator.seekFromCluster(viewerCacheFileFolder, viewerCacheFileName, new FileOutputStream(viewerCacheFile))) {

                         documentList.setViewerCacheFileName(viewerCacheFileName);
                         documentList.setViewerCacheTime(new Date());
                         documentList.setViewerCacheTimeLength(viewerCacheTimeLength);
                         if(cephFileMetaInfoRecord.updateDocumentList(documentList)!=null){
                             return viewerCacheFileName;
                         }else {
                             log.trace("set viewer file cache to db failed");
                             return "";
                         }
                     } else {
                         return "";
                     }

                 } else {
                     //if not existed, return back ""
                     return "";
                 }
             }catch (FileNotFoundException e){

                    log.trace(e.getStackTrace());
                    return "";
             }catch (IOException e){

                    log.trace(e.getStackTrace());
                    return "";
             }
         }

    }

    @Override
    public void revokeExpiredViewerFileCache() {

        try {
                //loop all the document from CephFileMetaInfoRecord and verify whether the cached document is expired its viewerCacheTime
                List<DocumentList> cachedDcList = cephFileMetaInfoRecord.getAllDocumentList().stream().filter(x -> x.getViewerCacheFileName() != null && x.getViewerCacheTime() != null && x.getViewerCacheTimeLength() != null).collect(Collectors.toList());
                //if expired,delete it under viewerCache folder,update viewerCacheFileName,viewerCacheTime,viewerCacheTimeLength to null
                if (cachedDcList.size() > 0) {
                    Date now = new Date();
                    for (DocumentList cachedDc : cachedDcList) {

                        Date compareDeadLineTime = new Date(cachedDc.getViewerCacheTime().getTime() + cachedDc.getViewerCacheTimeLength() * 60 * 1000);
                        if (now.after(compareDeadLineTime)) {

                            //remove the viewer file cache from ceph cluster
                            if (cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + cachedDc.getViewerCacheFileName()).equals("file")) {
                                cephFileOperator.delFile(viewerCacheFileFolder + File.separatorChar + cachedDc.getViewerCacheFileName(), viewerCacheFileFolder);
                            }
                            //remove the viewer file cache on local file folder
                            File viewerCacheFile = new File(viewerCacheFileFolder, cachedDc.getViewerCacheFileName());
                            viewerCacheFile.delete();

                            //remove the viewer file cache on nodeDb
                            cachedDc.setViewerCacheFileName(null);
                            cachedDc.setViewerCacheTime(null);
                            cachedDc.setViewerCacheTimeLength(null);
                            cephFileMetaInfoRecord.updateDocumentList(cachedDc);

                            log.trace("revoke Expired viewer file cache at: " + viewerCacheFileFolder + File.separatorChar + cachedDc.getViewerCacheFileName() + " once.");
                        }
                    }
                }
                Thread.sleep(100);
                //loop all the document remain under viewer cache file folder at this moment and verify whether it's existed under ceph cluster.
                File viewerCacheDir = new File(viewerCacheFileFolder);
                File[] localRemainViewerCacheFiles = viewerCacheDir.listFiles();
                //if it's not existed, delete it.
                for (File localRemainViewerCacheFile : localRemainViewerCacheFiles) {

                    if (localRemainViewerCacheFile != null && localRemainViewerCacheFile.isFile()) {

                        String viewerCacheFileFullName = viewerCacheFileFolder + File.separatorChar + localRemainViewerCacheFile.getName();
                        if (cephFileOperator.fileOrDir(viewerCacheFileFullName).equals("null")) {

                            localRemainViewerCacheFile.delete();
                            log.trace("revoke Expired viewer file cache at: " + viewerCacheFileFullName + " once.");
                        }

                    }
                }
        }catch (InterruptedException e){

            log.trace(e.toString());
        }

    }


    public void  PullNonExpiredViewerFileCacheFromClusterToLocal(){

           try {
               //Verify whether viewerCacheFileFolder is existed under ceph cluster.if not existed, make it.
               if (!cephFileOperator.fileOrDir(viewerCacheFileFolder).equals("dir")
                       && !cephFileOperator.fileOrDir(viewerCacheFileFolder).equals("error")) {
                   if (cephFileOperator.mkDir(viewerCacheFileFolder).length <= 0) {

                       log.trace("make ceph file dir failed.");
                   }
               }
               //loop the file list from ceph cluster viewerCacheFileFolder
               String[] viewerCacheFileList = cephFileOperator.listDir(viewerCacheFileFolder);
               //loop all the document under current NodeDB and base on common viewercachefilename hash rule,verify whether it's existed under NodeDb.
               List<DocumentList> dcList=null;
               if(viewerCacheFileList.length>0) {
                  dcList = cephFileMetaInfoRecord.getAllDocumentList();
               }
                for(String viewerCacheFileName:viewerCacheFileList) {
                    boolean isUnderNodeDb=false;
                    DocumentList foundDc=null;
                    for (DocumentList dc : dcList) {
                        if(viewerCacheFileName.trim().equals(commonHelper.getHashViewerCacheFileName(dc.getFilePath(),dc.getFileName(),dc.getUploadedByPlatformUserGuid())))
                        {
                            isUnderNodeDb=true;
                            foundDc=dc;
                            break;
                        }
                    }
                    //if it is, download it from cluster to local viewerfile cache and update the cache flag to the document under NodeDb just like CDN performance.
                    if(isUnderNodeDb==true&&foundDc!=null){
                             //download,update into NodeDb
                        if (cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName).equals("file")){
                            File viewerCacheFile = new File(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName);
                            //before pull it to local, verify whether it has been existed under local viewercachefile folder by another request.
                            //if not existed,pull it.
                            if(!viewerCacheFile.exists()) {
                                if (cephFileOperator.seekFromCluster(viewerCacheFileFolder, viewerCacheFileName, new FileOutputStream(viewerCacheFile))) {

                                    log.trace("PullNonExpiredViewerFileCacheFromClusterToLocal:pull viewer file cache to local success.");
                                    foundDc.setViewerCacheFileName(viewerCacheFileName);
                                    foundDc.setViewerCacheTime(new Date());
                                    foundDc.setViewerCacheTimeLength(defaultViewerCacheLength);
                                    if (cephFileMetaInfoRecord.updateDocumentList(foundDc) != null) {
                                        log.trace("PullNonExpiredViewerFileCacheFromClusterToLocal:set viewer file cache to db success");
                                    } else {
                                        log.trace("PullNonExpiredViewerFileCacheFromClusterToLocal:set viewer file cache to db failed");
                                    }
                                } else {
                                    log.trace("PullNonExpiredViewerFileCacheFromClusterToLocal:pull viewer file cache to local failed");
                                }
                            }
                        }
                    }else {//if it isn't, that means it comes from other node and current node didn't receive any request from it.we need to pull it to local viewerfile cache just like CDN performance.

                        if (cephFileOperator.fileOrDir(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName).equals("file")) {
                            File viewerCacheFile = new File(viewerCacheFileFolder + File.separatorChar + viewerCacheFileName);
                            //before pull it to local, verify whether it has been existed under local viewercachefile folder by another request.
                               //if not existed,pull it.
                            if(!viewerCacheFile.exists()) {
                                if (cephFileOperator.seekFromCluster(viewerCacheFileFolder, viewerCacheFileName, new FileOutputStream(viewerCacheFile))) {

                                    log.trace("PullNonExpiredViewerFileCacheFromClusterToLocal:pull viewer file cache to local success.");
                                } else {
                                    log.trace("PullNonExpiredViewerFileCacheFromClusterToLocal:pull viewer file cache to local failed.");
                                }
                            }
                        }

                    }
                }


           }catch (FileNotFoundException e){
               log.trace(e.toString());
           } catch (IOException e){
               log.trace(e.toString());
           }


    }

}
