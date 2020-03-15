package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.AppPlatformDocTypeRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPlatformDocTypeRelationDao {

    @Select("select * from AppPlatformDocTypeRelation where appdocTypeRelationID=#{appdocTypeRelationID}")
    public AppPlatformDocTypeRelation getOneById(int appdocTypeRelationID);

    @Select("select * from AppPlatformDocTypeRelation where appdocTypeRelationGuid=#{appdocTypeRelationGuid}")
    public AppPlatformDocTypeRelation getOneByGuid(String appdocTypeRelationGuid);

    @Select("select * from AppPlatformDocTypeRelation where appId=#{appId} and docTypeId=#{docTypeId}")
    public List<AppPlatformDocTypeRelation> getOneByForeignKeysGroup(long appId,long docTypeId);

    @Select("select * from AppPlatformDocTypeRelation where appId=#{appId} and docTypeId=#{docTypeId} and appdocTypeRelationGuid<>#{appdocTypeRelationGuid}")
    public List<AppPlatformDocTypeRelation> getOneByForeignKeysGroupWhenUpdate(long appId,long docTypeId,String appdocTypeRelationGuid);

    @Select("select * from AppPlatformDocTypeRelation where isActive=1")
    public List<AppPlatformDocTypeRelation> getAll();

    @Update("update AppPlatformDocTypeRelation set appId=#{appId},docTypeId=#{docTypeId},downloadFlag=#{downloadFlag},uploadFlag=#{uploadFlag},deleteFlag=#{deleteFlag},isActive=#{isActive} where appdocTypeRelationGuid=#{appdocTypeRelationGuid}")
    public int update(AppPlatformDocTypeRelation appPlatformDocTypeRelation);

    @Insert({"insert into AppPlatformDocTypeRelation(appdocTypeRelationGuid,appId,docTypeId,downloadFlag,uploadFlag,deleteFlag,isActive,createTime)"
            ," values(#{appdocTypeRelationGuid},#{appId},#{docTypeId},#{downloadFlag},#{uploadFlag},#{deleteFlag},#{isActive},#{createTime})"})
    public int insert(AppPlatformDocTypeRelation appPlatformDocTypeRelation);

    @Delete("delete from AppPlatformDocTypeRelation where appdocTypeRelationID=#{appdocTypeRelationID}")
    public int deleteById(int appdocTypeRelationID);

    @Delete("delete from AppPlatformDocTypeRelation where appdocTypeRelationGuid=#{appdocTypeRelationGuid}")
    public int deleteByGuid(String appdocTypeRelationGuid);

    @Update("update AppPlatformDocTypeRelation set isActive=0 where appdocTypeRelationID=#{appdocTypeRelationID}")
    public int setToNotActive(long appdocTypeRelationID);

    @Update("update AppPlatformDocTypeRelation set isActive=1 where appdocTypeRelationID=#{appdocTypeRelationID}")
    public int setToActive(long appdocTypeRelationID);

}
