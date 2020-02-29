package cn.Boy.DiskFile.distributeFileEntry;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IFileNodeMetaInfoRecorderSync {

    //For vim upload sync between file local cache folder, Node DB meta Record and Ceph Cluster
    //uploadLazySync_checkAndSave, uploadLazySync_StatusUpdate
     public Map<String, Object>  uploadLazySync_checkAndSave(String userGuid, String filePath, String fileName, long docTypeId, long fileExtID, MultipartFile currentUploadFile, boolean isBig);

     public boolean uploadLazySync_StatusUpdate(String docGuid,int clusterStorageStatus);

     //For cat download sync between file local cache folder, Node DB meta Record and Ceph Cluster.
    //downloadLazySync_checkAndSave
    public Map<String, Object>  downloadLazySync_checkAndSave(String userGuid,String filePath, String fileName, long docTypeId, long fileExtID);

    public Map<String,Object> deleteLazySync_checkAndSave(String userGuid,String filePath, String fileName, long docTypeId, long fileExtID);

    public Map<String,Object> renameLazySync_checkAndSave(String userGuid,String filePath, String oldFileName,String newFileName, long docTypeId, long fileExtID);

}
