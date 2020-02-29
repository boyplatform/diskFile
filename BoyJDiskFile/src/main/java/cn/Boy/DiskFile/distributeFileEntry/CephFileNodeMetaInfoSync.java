package cn.Boy.DiskFile.distributeFileEntry;


import cn.Boy.DiskFile.pojo.DocumentList;
import com.ceph.fs.CephStat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.common.CommonEnums;
import org.springframework.web.multipart.MultipartFile;

@Service("CephFileNodeMetaInfoSync")
@Scope("prototype")
public class CephFileNodeMetaInfoSync implements IFileNodeMetaInfoRecorderSync {

    @Autowired
    @Qualifier("CephFileMetaInfoRecord")
    private ICommonFileMetaInfoRecorder cephFileMetaInfoRecord;


    @Autowired
    @Qualifier("CephFileOperator")
    private AbsCommonFileOperator cephFileOperator;


    @Autowired
    @Qualifier("PoseidonOptimizeSeek")
    private PoseidonOptimizeSeek poseidonOptimizeSeek;

    @Autowired
    private CommonHelper commonHelper;

    private final Log log = LogFactory.getLog(CephFileNodeMetaInfoSync.class);


    public Map<String, Object> uploadLazySync_checkAndSave(String userGuid, String filePath, String fileName, long docTypeId, long fileExtID,MultipartFile currentUploadFile,boolean isBig) {

        log.trace("point0");
        Map<String,Object> result=new Hashtable<String,Object>();
        //Verfiy whether the fileExtID and the file doctypeId relationship is existed under Node DB.
         if(cephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation().stream().filter(x->x.getDocTypeId().equals(docTypeId) && x.getPlatformFileExtID().equals(fileExtID)).collect(Collectors.toList()).size()<=0)
         {
               result.put("isGoOn",false);
               result.put("desc","Base on your input parameter,there is no relationship of these doctype and file extension type under boy platform Node DB pre-define.");
               log.trace("point1:"+docTypeId+"||"+fileExtID);
               return  result;
         }

        //Take the file extend name from filename,Verify whether the fileExtID is mapped to current file extendName under Node DB.
            String fileExtName="";
            if(fileName.trim().split("\\.").length==2) {
                  fileExtName = fileName.trim().split("\\.")[1].trim();
                  if(cephFileMetaInfoRecord.getOnePlatformFileExtByName(fileExtName)==null
                          ||cephFileMetaInfoRecord.getOnePlatformFileExtByName(fileExtName).getFileExtID().equals(fileExtID)==false){

                      result.put("isGoOn",false);
                      result.put("desc","Input fileExtID and file's fileExtName is not matched as per boy platform Node DB pre-define.");
                      log.trace("point2");
                      return  result;
                  }

            }else{
                result.put("isGoOn",false);
                result.put("desc","Input fileName without extension name,Please check and vim upload later.");
                log.trace("point3");
                return  result;
            }

         //Verify whether file path dir has been existed under ceph cluster
         if(cephFileOperator.fileOrDir(filePath).equals("dir"))
         {
             //if existed, then verify whether the same is existed under local file cache folder--if not,make it, go on next step;if existed, go on next step directly.
             File userFileFolder = new File(filePath);
             if(!userFileFolder.exists()) {
                 userFileFolder.mkdirs();
             }

         }else if(!cephFileOperator.fileOrDir(filePath).equals("error")) {
                 //if not existed, then verify whether it is existed under local file cache folder--if existed, make it on ceph cluster;if not existed,make it on local and ceph cluster both.
                 File userFileFolder = new File(filePath);
                 if(!userFileFolder.exists())
                 {
                     if(!userFileFolder.mkdirs()){
                         result.put("isGoOn",false);
                         result.put("desc","Local file cache folder was failed to be created.");
                         log.trace("point4");
                         return  result;
                     }
                 }
                 try {
                     if (cephFileOperator.mkDir(filePath).length > 0) {
                         log.trace("create ceph dir successfully");
                     } else {
                         log.trace("create ceph dir failed");
                         result.put("isGoOn",false);
                         result.put("desc","create ceph dir failed.");
                         return  result;
                     }
                 }catch (IOException e)
                 {
                     log.trace("create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                     result.put("isGoOn",false);
                     result.put("desc","create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                     return  result;
                 }

         }
        //Verify whether doc has been existed under ceph cluster
         String cephFullFileName=filePath + File.separatorChar + fileName;

        if(cephFileOperator.fileOrDir(cephFullFileName).equals("file")) {
                 log.trace("point5");
                //if existed,then check whether the doc has been existed under local file cache folder.
                File userFileFolder = new File(filePath);
                File cacheFileOld=new File(userFileFolder, fileName);
                long fileVersion=0l;
                Map<String,Object> fileHashFreshNameMap=commonHelper.getFileHashFreshName(fileName.split("\\.")[0]);
               if(cacheFileOld.exists()) {
                   //if the doc existed under the local file cache folder, then rename its name with hash file rename rules .
                   fileName=fileHashFreshNameMap.get("renewFileName")+"."+fileName.split("\\.")[1];
                   fileVersion=(long)fileHashFreshNameMap.get("renewFileVersion");
               }else {
                   //else the doc not existed under the local file cache folder, then download it from ceph cluster, and meanwhile rename its name with hash file rename rules .
                   try {
                       cephFileOperator.seekFromCluster(filePath, fileName, new FileOutputStream(cacheFileOld));
                       fileName = fileHashFreshNameMap.get("renewFileName")+"."+fileName.split("\\.")[1];
                       fileVersion=(long)fileHashFreshNameMap.get("renewFileVersion");
                   } catch (IOException e){

                       log.trace("create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                       result.put("isGoOn",false);
                       result.put("desc","create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                       return  result;
                   }
               }
                //create new one base on the renew filename under local file cache folder.
                File cacheFileNew=new File(userFileFolder, fileName);
                log.trace("point6");
                if(!cacheFileNew.exists()){

                    try {
                        FileOutputStream fos = new FileOutputStream(cacheFileNew);
                        if (!isBig) {
                            if (IOUtils.copy(currentUploadFile.getInputStream(), fos) > 0) {
                                log.trace("File cached successfully.");
                            } else {
                                log.trace("File cached failed.");
                            }
                            fos.close();
                        } else {
                            if (IOUtils.copyLarge(currentUploadFile.getInputStream(), fos) > 0) {
                                log.trace("Big File cached successfully.");
                            } else {
                                log.trace("Big File cached failed.");
                            }
                            fos.close();
                        }

                        if (!cacheFileNew.exists()) {
                            result.put("isGoOn", false);
                            result.put("desc", "create upload file cache failed on current node,please try again!");
                            return result;
                        }
                    }catch (IOException e){
                        result.put("isGoOn", false);
                        result.put("desc", "create upload file cache failed on current node,please try again!");
                        return result;
                    }
                }
                //save it into Node DB.
                DocumentList dcList=new DocumentList();
                dcList.setVersion(fileVersion);
                dcList.setIsActive(true);
                dcList.setDocGuid(commonHelper.getNodeUUID());
                dcList.setCreateTime(new Date());
                dcList.setUploadedByPlatformUserGuid(userGuid);
                dcList.setLastModifiedByPlatformUserGuid(userGuid);
                dcList.setDocTypeId(docTypeId);
                dcList.setPlatformfileExtID(fileExtID);
                dcList.setFilePath(filePath);
                dcList.setFileName(fileName);
                dcList.setFileSize(commonHelper.getMbFromByte(cacheFileNew.length()));
                dcList.setMemorySeaLevelTime(new Date());
                dcList.setMemoryHitTimes((long)0);
                dcList.setMemoryLiveTimeSec((long)commonHelper.getPoseidongParameter().get("memoryLiveTimeSec"));
                dcList.setMemoryPerHitComeUpSeconds((long)commonHelper.getPoseidongParameter().get("memoryPerHitComeUpSeconds"));

                log.trace("memoryLiveTimeSec:"+commonHelper.getPoseidongParameter().get("memoryLiveTimeSec"));
                log.trace("memoryPerHitComeUpSeconds:"+commonHelper.getPoseidongParameter().get("memoryPerHitComeUpSeconds"));

                dcList.setStorageClusterType(CommonEnums.StorageClusterType.ceph.getClusterType());
                dcList.setClusterStorageStatus(CommonEnums.ClusterStorageStatus.didnotsaved.getClusterStorageStatus());
                dcList.setFileExtName(fileName.split("\\.")[1].trim());
                if(cephFileMetaInfoRecord.addDocumentList(dcList)!=null){
                    result.put("isGoOn", true);
                    result.put("File",cacheFileNew);
                    result.put("DocumentList",dcList);
                    result.put("desc", "File upload Lazy Sync successfully");
                    return result;  //return the termianl process result map.
                }else {
                    result.put("isGoOn", false);
                    result.put("File",cacheFileNew);
                    result.put("DocumentList",dcList);
                    result.put("desc", "File upload Lazy Sync failed");
                    return result;  //return the termianl process result map.
                }

        }else if(!cephFileOperator.fileOrDir(cephFullFileName).equals("error")) {
            log.trace("point7");
            //if not existed, then check whether the doc has been not existed under local file cache folder.
            File userFileFolder = new File(filePath);
            File cacheFile=new File(userFileFolder, fileName);
            //if the doc existed under the local file cache folder, then clean it under cache folder.
            if(cacheFile.exists()){
                cacheFile.delete();
            }else {
                //else the doc not existed  the local file cache folder, meanwhile clean it under node DB.
                cephFileMetaInfoRecord.hardDeleteDocumentList(filePath,fileName,docTypeId,fileExtID,fileExtName,userGuid);
            }
            //creat new one base on the filename under local file cache folder.
            if(!cacheFile.exists()){

                try {
                    FileOutputStream fos = new FileOutputStream(cacheFile);
                    if (!isBig) {
                        if (IOUtils.copy(currentUploadFile.getInputStream(), fos) > 0) {
                            log.trace("File cached successfully.");
                        } else {
                            log.trace("File cached failed.");
                        }
                        fos.close();
                    } else {
                        if (IOUtils.copyLarge(currentUploadFile.getInputStream(), fos) > 0) {
                            log.trace("Big File cached successfully.");
                        } else {
                            log.trace("Big File cached failed.");
                        }
                        fos.close();
                    }

                    if (!cacheFile.exists()) {
                        log.trace("point8");
                        result.put("isGoOn", false);
                        result.put("desc", "create upload file cache failed on current node,please try again!");
                        return result;
                    }
                }catch (IOException e){
                    log.trace("point9");
                    result.put("isGoOn", false);
                    result.put("desc", "create upload file cache failed on current node,please try again!");
                    return result;
                }
            }
                //save it into Node DB.
                DocumentList dcList=new DocumentList();
                dcList.setIsActive(true);
                dcList.setDocGuid(commonHelper.getNodeUUID());
                dcList.setCreateTime(new Date());
                dcList.setUploadedByPlatformUserGuid(userGuid);
                dcList.setLastModifiedByPlatformUserGuid(userGuid);
                dcList.setDocTypeId(docTypeId);
                dcList.setPlatformfileExtID(fileExtID);
                dcList.setFilePath(filePath);
                dcList.setFileName(fileName);
                dcList.setFileSize(commonHelper.getMbFromByte(cacheFile.length()));
                dcList.setMemorySeaLevelTime(new Date());
                dcList.setMemoryHitTimes((long)1);
                dcList.setMemoryLiveTimeSec((long)commonHelper.getPoseidongParameter().get("memoryLiveTimeSec"));
                dcList.setMemoryPerHitComeUpSeconds((long)commonHelper.getPoseidongParameter().get("memoryPerHitComeUpSeconds"));

                log.trace("memoryLiveTimeSec:"+commonHelper.getPoseidongParameter().get("memoryLiveTimeSec"));
                log.trace("memoryPerHitComeUpSeconds:"+commonHelper.getPoseidongParameter().get("memoryPerHitComeUpSeconds"));

                dcList.setStorageClusterType(CommonEnums.StorageClusterType.ceph.getClusterType());
                dcList.setClusterStorageStatus(CommonEnums.ClusterStorageStatus.pendingSaved.getClusterStorageStatus());
                dcList.setFileExtName(fileName.split("\\.")[1].trim());
                if(cephFileMetaInfoRecord.addDocumentList(dcList)!=null){
                    log.trace("point10");
                    result.put("isGoOn", true);
                    result.put("File",cacheFile);
                    result.put("DocumentList",dcList);
                    result.put("desc", "File pre-upload Lazy Sync successfully");
                    return result;  //return the termianl process result map.
                }else {
                    log.trace("point11");
                    result.put("isGoOn", false);
                    result.put("File",cacheFile);
                    result.put("DocumentList",dcList);
                    result.put("desc", "File pre-upload Lazy Sync failed");
                    return result;  //return the termianl process result map.
                }

        }else {
            result.put("isGoOn", false);
            result.put("desc", "met error during vim upload your file to platform disk.");
            return  result;
        }

    }


    public boolean uploadLazySync_StatusUpdate(String docGuid,int clusterStorageStatus) {

        //seek the doc entity from Node db base on docGuid at first.
        DocumentList dcList=cephFileMetaInfoRecord.getOneDocumentListByGuid(docGuid);

        if(dcList!=null) {
            //Modify the entity'clusterStorageStatus and update into Node db.
            dcList.setClusterStorageStatus(clusterStorageStatus);
            if(cephFileMetaInfoRecord.updateDocumentList(dcList)!=null){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }

    }


    public Map<String, Object> downloadLazySync_checkAndSave(String userGuid,String filePath, String fileName, long docTypeId, long fileExtID){

        log.trace("point-0");
        Map<String,Object> result=new Hashtable<String,Object>();
        //Verfiy whether the fileExtID and the file doctypeId relationship is existed under Node DB.
        if(cephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation().stream().filter(x->x.getDocTypeId().equals(docTypeId) && x.getPlatformFileExtID().equals(fileExtID)).collect(Collectors.toList()).size()<=0)
        {
            result.put("isGoOn",false);
            result.put("desc","Base on your input parameter,there is no relationship of these doctype and file extension type under boy platform Node DB pre-define.");
            return  result;
        }
        //Take the file extend name from filename,Verify whether the fileExtID is mapped to current file extendName under Node DB.
        log.trace("point-1");
        String fileExtName="";
        if(fileName.trim().split("\\.").length==2) {
            fileExtName = fileName.trim().split("\\.")[1].trim();
            if(cephFileMetaInfoRecord.getOnePlatformFileExtByName(fileExtName).getFileExtID().equals(fileExtID)==false){

                result.put("isGoOn",false);
                result.put("desc","Input fileExtID and file's fileExtName is not matched as per boy platform Node DB pre-define.");
                return  result;
            }

        }else{
            result.put("isGoOn",false);
            result.put("desc","Input fileName without extension name,Please check and download later.");
            return  result;
        }

        //Verify whether file path dir has been existed under ceph cluster
        log.trace("point-2:"+filePath);
        if(cephFileOperator.fileOrDir(filePath).equals("dir"))
        {
                //if existed, then verify whether the same is existed under local file cache folder--if not,make it, go on next step;if existed, go on next step directly.
                File userFileFolder = new File(filePath);
                if(!userFileFolder.exists()) {
                    userFileFolder.mkdirs();
                }

        }else if(!cephFileOperator.fileOrDir(filePath).equals("error")) {
                //if not existed, then verify whether it is existed under local file cache folder--if existed, make it on ceph cluster;if not existed,make it on local and ceph cluster both.
            log.trace("point-2.1:"+filePath);
            File userFileFolder = new File(filePath);
                if(!userFileFolder.exists())
                {
                    log.trace("point-2.2:"+filePath);
                    if(!userFileFolder.mkdirs()){
                        result.put("isGoOn",false);
                        result.put("desc","Local file cache folder was failed to be created.");
                        return  result;
                    }
                }
                try {
                    log.trace("point-2.3:"+filePath);
                    if (cephFileOperator.mkDir(filePath).length > 0) {
                        log.trace("create ceph dir successfully");
                    } else {
                        log.trace("create ceph dir failed");
                        result.put("isGoOn",false);
                        result.put("desc","create ceph dir failed.");
                        return  result;
                    }
                }catch (IOException e)
                {
                    log.trace("create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                    result.put("isGoOn",false);
                    result.put("desc","create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                    return  result;
                }
        }

        //Verify whether doc has been existed under ceph cluster
        log.trace("point-3");
        String cephFullFileName=filePath + File.separatorChar + fileName;
        if(cephFileOperator.fileOrDir(cephFullFileName).equals("file")) {
            File cacheFile=new File(cephFullFileName);
            //if existed,Check whether the doc has been existed under Node DB via Poseidon calculation search--if not existed, add it into Node DB.
            List<DocumentList> dcLists=poseidonOptimizeSeek.seek(userGuid,filePath,fileName,docTypeId,fileExtID);
            if(dcLists.size()<=0){

                DocumentList dcList=new DocumentList();
                dcList.setDocGuid(commonHelper.getNodeUUID());
                dcList.setCreateTime(new Date());
                dcList.setIsActive(true);
                dcList.setUploadedByPlatformUserGuid(userGuid);
                dcList.setLastModifiedByPlatformUserGuid(userGuid);
                dcList.setDocTypeId(docTypeId);
                dcList.setPlatformfileExtID(fileExtID);
                dcList.setFilePath(filePath);
                dcList.setFileName(fileName);
                dcList.setFileSize(commonHelper.getMbFromByte(((CephStat)cephFileOperator.getFileStatByPath(cephFullFileName)).size));
                dcList.setMemorySeaLevelTime(new Date());
                dcList.setMemoryHitTimes((long)1);
                dcList.setMemoryLiveTimeSec((long)commonHelper.getPoseidongParameter().get("memoryLiveTimeSec"));
                dcList.setMemoryPerHitComeUpSeconds((long)commonHelper.getPoseidongParameter().get("memoryPerHitComeUpSeconds"));
                dcList.setStorageClusterType(CommonEnums.StorageClusterType.ceph.getClusterType());
                dcList.setClusterStorageStatus(CommonEnums.ClusterStorageStatus.pendingSaved.getClusterStorageStatus());
                dcList.setFileExtName(fileName.split("\\.")[1].trim());

                //Then return back true with desc map result to trigger the download
                if(cephFileMetaInfoRecord.addDocumentList(dcList)!=null){
                    log.trace("point-4");
                    result.put("isGoOn", true);
                    result.put("File",cacheFile);
                    result.put("DocumentList",dcList);
                    result.put("desc", "File pre-download Lazy Sync successfully");
                    return result;  //return the termianl process result map.
                }else {
                    log.trace("point-5");
                    result.put("isGoOn", false);
                    result.put("File",cacheFile);
                    result.put("DocumentList",dcList);
                    result.put("desc", "File pre-download Lazy Sync failed");
                    return result;  //return the termianl process result map.
                }
            }else{
                log.trace("point-6");
                result.put("isGoOn", true);
                result.put("File",cacheFile);
                result.put("DocumentList",dcLists.get(0));
                result.put("desc", "File pre-download Lazy Sync successfully");
                return result;  //return the termianl process result map.
            }

        }else if(!cephFileOperator.fileOrDir(cephFullFileName).equals("error")) {
            log.trace("point-7");
            //if not existed, then check whether doc has been existed under local file cache path.
            File cacheFile=new File(cephFullFileName);
            if(cacheFile.exists()){
                cacheFile.delete();//if existed under local file cache path,delete it
            }

            //then check whether the doc has been existed under Node DB via Poseidon calculation search--if existed, delete it under Node DB
            if(poseidonOptimizeSeek.seek(userGuid,filePath,fileName,docTypeId,fileExtID).size()>0)
            {
                cephFileMetaInfoRecord.hardDeleteDocumentList(filePath,fileName,docTypeId,fileExtID,fileExtName,userGuid);
            }
            //Then return back false with desc map result to ban the download processing.
            result.put("isGoOn", false);
            result.put("desc", "The file you cat now has not been existed under platform disk");
            return  result;

        }else {
            result.put("isGoOn", false);
            result.put("desc", "met error during cat your file from platform disk.");
            return  result;
        }

    }


    //delete sync
    public Map<String,Object> deleteLazySync_checkAndSave(String userGuid,String filePath, String fileName, long docTypeId, long fileExtID){

        Map<String,Object> result=new Hashtable<String,Object>();
        //Verfiy whether the fileExtID and the file doctypeId relationship is existed under Node DB.
        if(cephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation().stream().filter(x->x.getDocTypeId().equals(docTypeId) && x.getPlatformFileExtID().equals(fileExtID)).collect(Collectors.toList()).size()<=0)
        {
            result.put("result",false);
            result.put("desc","Base on your input parameter,there is no relationship of these doctype and file extension type under boy platform Node DB pre-define.");
            return  result;
        }
        //Take the file extend name from filename,Verify whether the fileExtID is mapped to current file extendName under Node DB.
        String fileExtName="";
        if(fileName.trim().split("\\.").length==2) {
            fileExtName = fileName.trim().split("\\.")[1].trim();
            if(cephFileMetaInfoRecord.getOnePlatformFileExtByName(fileExtName)==null
                    ||cephFileMetaInfoRecord.getOnePlatformFileExtByName(fileExtName).getFileExtID().equals(fileExtID)==false){

                result.put("result",false);
                result.put("desc","Input fileExtID and file's fileExtName is not matched as per boy platform Node DB pre-define.");
                return  result;
            }

        }else{
            result.put("result",false);
            result.put("desc","Input fileName without extension name, Please check and delete later.");
            return  result;
        }

      //Verify whether file path dir has been existed under ceph cluster
        if(cephFileOperator.fileOrDir(filePath).equals("dir"))
        {
            //if existed, then verify whether the same is existed under local file cache folder--if not,make it, go on next step;if existed, go on next step directly.
            File userFileFolder = new File(filePath);
            if(!userFileFolder.exists()) {
                userFileFolder.mkdirs();
            }

        }else if(!cephFileOperator.fileOrDir(filePath).equals("error")) {
            //if not existed, then verify whether it is existed under local file cache folder--if existed, make it on ceph cluster;if not existed,make it on local and ceph cluster both.
            File userFileFolder = new File(filePath);
            if(!userFileFolder.exists())
            {
                if(!userFileFolder.mkdirs()){
                    result.put("result",false);
                    result.put("desc","Local file cache folder was failed to be created.");
                    return  result;
                }
            }
            try {
                if (cephFileOperator.mkDir(filePath).length > 0) {
                    log.trace("create ceph dir successfully");
                } else {
                    log.trace("create ceph dir failed");
                    result.put("result",false);
                    result.put("desc","create ceph dir failed.");
                    return  result;
                }
            }catch (IOException e)
            {
                log.trace("create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                result.put("result",false);
                result.put("desc","create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                return  result;
            }
        }

      //Verify whether doc has been existed under ceph cluster
        String cephFullFileName=filePath + File.separatorChar + fileName;
        //if existed, then delete it on ceph cluster; then verify whether doc has been existed under local cache folder,if existed,then delete it also; Verify whether doc has been existed under NodeDB, if existed,then delete it also. Finally set result as true with desc.
        if(cephFileOperator.fileOrDir(cephFullFileName).equals("file"))
        {
             String[] delFileDirList=cephFileOperator.delFile(cephFullFileName,filePath);
             if(delFileDirList!=null){
                 File cacheFile=new File(cephFullFileName);
                 if(cacheFile.exists()){
                     if(cacheFile.delete()){
                         result.put("result",true);
                         result.put("delFileDirList",delFileDirList);
                         result.put("desc","Delete your file from platform disk successfully.");

                     }else {
                         result.put("result",false);
                         result.put("desc","Delete your file from platform disk successfully,but delete from current node file cache folder failed.");
                         return result;
                     }
                 }
                 cephFileMetaInfoRecord.hardDeleteDocumentList(filePath,fileName,docTypeId,fileExtID,fileExtName,userGuid);
             }else {
                 result.put("result",false);
                 result.put("desc","Delete your file from platform disk failed.");
                 return result;
             }

        }else if(!cephFileOperator.fileOrDir(cephFullFileName).equals("error")) {
            //if not existed, then verify whether doc has been existed under local cache folder,if existed,then delete it;Then,verify whether doc has been existed under Node DB,if existed, then delete it under Node DB.  Finally set result as false with not existed desc.
           try {
               File cacheFile = new File(cephFullFileName);
               String[] delFileDirList = cephFileOperator.listDir(filePath);
               if (cacheFile.exists()) {
                   if (cacheFile.delete()) {
                       result.put("result", true);
                       result.put("delFileDirList", delFileDirList);
                       result.put("desc", "Your file has been already not existed under platform disk.");
                   } else {
                       result.put("result", false);
                       result.put("desc", "Your file has been already not existed under platform disk,but it was deleted from current node file cache folder failed.");
                       return result;
                   }
               }

               cephFileMetaInfoRecord.hardDeleteDocumentList(filePath, fileName, docTypeId, fileExtID, fileExtName, userGuid);
           }catch (IOException e){

               log.trace(e.getMessage());
               result.put("result", false);
               result.put("desc", "met IOException during delete your file from platform disk");
               return  result;

           }

        }else{

            result.put("result", false);
            result.put("desc", "met error during delete your file from platform disk");
            return  result;
        }

      //return the result.
        return result;
    }

    //rename sync
    public Map<String,Object> renameLazySync_checkAndSave(String userGuid,String filePath, String oldFileName,String newFileName, long docTypeId, long fileExtID){

        Map<String,Object> result=new Hashtable<String,Object>();
        //Verfiy whether the fileExtID and the file doctypeId relationship is existed under Node DB.
        if(cephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation().stream().filter(x->x.getDocTypeId().equals(docTypeId) && x.getPlatformFileExtID().equals(fileExtID)).collect(Collectors.toList()).size()<=0)
        {
            result.put("result",false);
            result.put("desc","Base on your input parameter,there is no relationship of these doctype and file extension type under boy platform Node DB pre-define.");
            return  result;
        }
        //Take the file extend name from filename,Verify whether the fileExtID is mapped to current file extendName under Node DB.
        String oldFileExtName="";
        String newFileExtName="";
        if(oldFileName.trim().split("\\.").length==2
                && newFileName.trim().split("\\.").length==2) {
            oldFileExtName = oldFileName.trim().split("\\.")[1].trim();
            newFileExtName=newFileName.trim().split("\\.")[1].trim();

            if(!oldFileExtName.equals(newFileExtName)){

                result.put("result",false);
                result.put("desc","newFileExtName is not the same as oldFileExtName, Please check your input parameter.");
                return  result;
            }

            if(cephFileMetaInfoRecord.getOnePlatformFileExtByName(oldFileExtName)==null
                    ||cephFileMetaInfoRecord.getOnePlatformFileExtByName(oldFileExtName).getFileExtID().equals(fileExtID)==false)
            {
                result.put("result",false);
                result.put("desc","Input fileExtID and file's fileExtName is not matched as per boy platform Node DB pre-define.");
                return  result;
            }

        }else{
            result.put("result",false);
            result.put("desc","Input fileName without extension name,Please check and rename later.");
            return  result;
        }


        //Verify whether file path dir has been existed under ceph cluster
        if(cephFileOperator.fileOrDir(filePath).equals("dir"))
        {
            //if existed, then verify whether the same is existed under local file cache folder--if not,make it, go on next step;if existed, go on next step directly.
            File userFileFolder = new File(filePath);
            if(!userFileFolder.exists()) {
                userFileFolder.mkdirs();
            }

        }else if(!cephFileOperator.fileOrDir(filePath).equals("error")) {
            //if not existed, then verify whether it is existed under local file cache folder--if existed, make it on ceph cluster;if not existed,make it on local and ceph cluster both.
            File userFileFolder = new File(filePath);
            if(!userFileFolder.exists())
            {
                if(!userFileFolder.mkdirs()){
                    result.put("result",false);
                    result.put("desc","Local file cache folder was failed to be created.");
                    return  result;
                }
            }
            try {
                if (cephFileOperator.mkDir(filePath).length > 0) {
                    log.trace("create ceph dir successfully");
                } else {
                    log.trace("create ceph dir failed");
                    result.put("result",false);
                    result.put("desc","create ceph dir failed.");
                    return  result;
                }
            }catch (IOException e)
            {
                log.trace("create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                result.put("result",false);
                result.put("desc","create ceph dir failed by IO exception as below:\r\n"+e.getMessage());
                return  result;
            }
        }

       //Verify whether doc has been existed under ceph cluster by oldFileName
        String oldCephFullFileName=filePath + File.separatorChar + oldFileName;
        String newCephFullFileName=filePath + File.separatorChar + newFileName;
        if(cephFileOperator.fileOrDir(oldCephFullFileName).equals("file")) {
            //if existed,at first,seek the document from node db by newFileName via Poseidong search,if already existed,get the fixed newFileName
            if(poseidonOptimizeSeek.seek(userGuid,filePath,newFileName,docTypeId,fileExtID).size()>0) {

                Map<String, Object> fileHashFreshNameMap = commonHelper.getFileHashFreshName(newFileName.split("\\.")[0]);
                newFileName=fileHashFreshNameMap.get("renewFileName")+"."+newFileName.split("\\.")[1];
                newCephFullFileName=filePath + File.separatorChar + newFileName;
            }
            //Then verify whether it's existed under local file cache folder
            File oldCacheFile=new File(oldCephFullFileName);
            if(oldCacheFile.exists()) {
                //if existed,then rename both on ceph cluster & local file cache folder per newFileName;
                String[] dirList=cephFileOperator.renameDirOrFile(oldCephFullFileName,newCephFullFileName,filePath);
                if(dirList!=null){

                    if(oldCacheFile.renameTo(new File(newCephFullFileName))){
                        //update newFileName into nodeDB from oldFileName
                        DocumentList oldDoc=poseidonOptimizeSeek.seek(userGuid,filePath,oldFileName,docTypeId,fileExtID).stream().findFirst().get();
                        oldDoc.setFileName(newFileName); //change old document name into new document name.
                        cephFileMetaInfoRecord.updateDocumentList(oldDoc);
                        result.put("result",true);
                        result.put("dirList",dirList);
                        result.put("desc","Your file has been renamed successfully on platform disk.");
                    }else {
                        result.put("result",false);
                        result.put("desc","Your file has been renamed successfully on platform disk,but be renamed failed under current node cached file folder.");
                        return result;
                    }
                }else {

                    result.put("result",false);
                    result.put("desc","Your file has been renamed failed on platform disk.");
                    return result;
                }
            }else {
                //if not existed,then download it from ceph cluster by oldFileName at first and rename both per newFileName on Ceph cluster & local fileCache folder.
                try {

                    if (cephFileOperator.seekFromCluster(filePath, oldFileName, new FileOutputStream(oldCacheFile))) {

                        String[] dirList=cephFileOperator.renameDirOrFile(oldCephFullFileName,newCephFullFileName,filePath);
                        if(dirList!=null){

                            if(oldCacheFile.renameTo(new File(newCephFullFileName))){
                                //update newFileName into nodeDB from oldFileName
                                DocumentList oldDoc=poseidonOptimizeSeek.seek(userGuid,filePath,oldFileName,docTypeId,fileExtID).stream().findFirst().get();
                                oldDoc.setFileName(newFileName); //change old document name into new document name.
                                cephFileMetaInfoRecord.updateDocumentList(oldDoc);
                                result.put("result",true);
                                result.put("dirList",dirList);
                                result.put("desc","Your file has been renamed successfully on platform disk.");
                            }else {
                                result.put("result",false);
                                result.put("desc","Your file has been renamed successfully on platform disk,but be renamed failed under current node cached file folder.");
                                return result;
                            }
                        }else {

                            result.put("result",false);
                            result.put("desc","Your file has been renamed failed on platform disk.");
                            return result;
                        }

                    }else {

                        result.put("result",false);
                        result.put("desc","seek your file from ceph cluster failed during rename it on platform disk.");
                        return result;
                    }

                }catch (IOException e){

                    result.put("result",false);
                    result.put("desc","met error during rename your file on platform disk.");
                    return result;
                }

            }

        }else if(!cephFileOperator.fileOrDir(oldCephFullFileName).equals("error")) {
            //if not existed, then verify whether it's existed under local file cache folder
            File oldCacheFile=new File(oldCephFullFileName);
            if(oldCacheFile.exists()){
                //if existed, then delete it as the same as ceph cluster.
                  oldCacheFile.delete();
            }
            //Then verify whether it's existed under Node DB,if existed,then delete it also.
            cephFileMetaInfoRecord.hardDeleteDocumentList(filePath,oldFileName,docTypeId,fileExtID,oldFileExtName,userGuid);
            result.put("result",false);
            result.put("desc","The file you want to rename is not existed on platform disk.");
            return  result;

        }else {

            // return the terminal result.
            result.put("result",false);
            result.put("desc","met error during rename your file on platform disk.");

        }

        return  result;
    }

   //clear the LocalFile which has Not Existed On Ceph Cluster
    public  void clearLocalFileNotExistedOnCluster(){

        //loop all the document on current local nodeDb,verify whether each one is existed under ceph cluster.
          List<DocumentList> dcList=cephFileMetaInfoRecord.getAllDocumentList();
            //if not existed under ceph cluster already,remove it on local file Cache folder and NodeDb.
            for(DocumentList dc:dcList){

                if(cephFileOperator.fileOrDir(dc.getFilePath()+ File.separatorChar +dc.getFileName()).equals("null")){

                      File cachedFile=new File(dc.getFilePath()+ File.separatorChar +dc.getFileName());
                      cachedFile.delete();
                      cephFileMetaInfoRecord.hardDeleteDocumentList(dc.getFilePath(),dc.getFileName(),dc.getDocTypeId(),dc.getPlatformfileExtID(),dc.getFileExtName(),dc.getUploadedByPlatformUserGuid());
                      log.trace("one not existed file:"+dc.getFilePath()+ File.separatorChar +dc.getFileName()+" has been removed from nodeDb.");
                }

            }
    }
}
