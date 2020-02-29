package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.DocumentList;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentListDao {

    @Select("select * from documentList where docId=#{docId}")
    public DocumentList getOneById(int docId);

    @Select("select * from documentList where docGuid=#{docGuid}")
    public DocumentList getOneByGuid(String docGuid);

    @Select("select * from documentList where filePath=#{filePath} and fileName=#{fileName} and platformfileExtID=#{platformfileExtID} and docTypeId=#{docTypeId} and (uploadedByPlatformUserGuid=#{uploadedByPlatformUserGuid} or lastModifiedByPlatformUserGuid=#{uploadedByPlatformUserGuid})")
    public List<DocumentList> getOneByGroupConditionColumn(String filePath,String fileName,long platformfileExtID,long docTypeId,String uploadedByPlatformUserGuid);

    @Select("select * from documentList where filePath=#{filePath} and fileName=#{fileName} and platformfileExtID=#{platformfileExtID} and docTypeId=#{docTypeId} and (uploadedByPlatformUserGuid=#{lastModifiedByPlatformUserGuid} or lastModifiedByPlatformUserGuid=#{lastModifiedByPlatformUserGuid}) and docGuid<>#{docGuid}")
    public List<DocumentList> getOneByGroupConditionColumnWhenUpdate(String filePath,String fileName,long platformfileExtID,long docTypeId,String lastModifiedByPlatformUserGuid,String docGuid);

    @Select("select * from documentList where isActive=1")
    public List<DocumentList> getAllActive();

    @Select("select * from documentList")
    public List<DocumentList> getAll();

    @Update("update documentList set platformfileExtID=#{platformfileExtID},docTypeId=#{docTypeId},fileName=#{fileName},filePath=#{filePath},fileSize=#{fileSize},version=#{version},isActive=#{isActive},createTime=#{createTime},memorySeaLevelTime=#{memorySeaLevelTime},memoryLiveTimeSec=#{memoryLiveTimeSec},memoryHitTimes=#{memoryHitTimes},memoryPerHitComeUpSeconds=#{memoryPerHitComeUpSeconds},storageClusterType=#{storageClusterType},uploadedByPlatformUserGuid=#{uploadedByPlatformUserGuid},lastModifiedByPlatformUserGuid=#{lastModifiedByPlatformUserGuid},clusterStorageStatus=#{clusterStorageStatus},fileExtName=#{fileExtName},viewerCacheFileName=#{viewerCacheFileName},viewerCacheTimeLength=#{viewerCacheTimeLength},viewerCacheTime=#{viewerCacheTime} where docGuid=#{docGuid}")
    public int update(DocumentList documentList);

    @Insert({"insert into documentList(platformfileExtID,docTypeId,docGuid,fileName,filePath,fileSize,version,isActive,createTime,memorySeaLevelTime,memoryLiveTimeSec,memoryHitTimes,memoryPerHitComeUpSeconds,storageClusterType,uploadedByPlatformUserGuid,lastModifiedByPlatformUserGuid,clusterStorageStatus,fileExtName)",
            " values(#{platformfileExtID},#{docTypeId},#{docGuid},#{fileName},#{filePath},#{fileSize},#{version},#{isActive},#{createTime},#{memorySeaLevelTime},#{memoryLiveTimeSec},#{memoryHitTimes},#{memoryPerHitComeUpSeconds},#{storageClusterType},#{uploadedByPlatformUserGuid},#{lastModifiedByPlatformUserGuid},#{clusterStorageStatus},#{fileExtName})"})
    public int insert(DocumentList documentList);

    @Delete("delete from documentList where docId=#{docId}")
    public int deleteById(int docId);

    @Delete("delete from documentList where docGuid=#{docGuid}")
    public int deleteByGuid(String docGuid);

    @Delete("delete from documentList where filePath=#{filePath} and fileName=#{fileName} and docTypeId=#{docTypeId} and platformfileExtID=#{platformfileExtID} and fileExtName=#{fileExtName} and uploadedByPlatformUserGuid=#{uploadedByPlatformUserGuid}")
    public int deleteByGroupConditionColumn(String filePath,String fileName,long docTypeId,long platformfileExtID,String fileExtName,String uploadedByPlatformUserGuid);

    @Update("update documentList set isActive=0 where docGuid=#{docGuid}")
    public int setToNotActive(String docGuid);

    @Update("update documentList set isActive=1 where docGuid=#{docGuid}")
    public int setToActive(String docGuid);


    //For Poseidon Seek
    @Select("select * from documentList where isActive=0")
    public List<DocumentList> getAllNotActive();

    @Select("select * from documentList where filePath=#{filePath} and fileName=#{fileName} and platformfileExtID=#{platformfileExtID} and docTypeId=#{docTypeId} and (uploadedByPlatformUserGuid=#{uploadedByPlatformUserGuid} or lastModifiedByPlatformUserGuid=#{uploadedByPlatformUserGuid}) and isActive=0")
    public List<DocumentList> getOneNotActiveByGroupConditionColumn(String filePath,String fileName,long platformfileExtID,long docTypeId,String uploadedByPlatformUserGuid);

}
