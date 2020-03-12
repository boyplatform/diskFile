package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.PlatformDocTypeFileExtRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;


@Repository
public interface PlatformDocTypeFileExtRelationDao {

    @Select("select a.* from platformDocTypeFileExtRelation a left join  platformFileExt b on a.platformFileExtID=b.fileExtID where b.fileExtName=#{fileExtName} and a.isActive=1")
    public List<PlatformDocTypeFileExtRelation> getListByFileExtName(String fileExtName);

    @Select("select * from platformDocTypeFileExtRelation where docTypeFileExtRelationID=#{docTypeFileExtRelationID}")
    public PlatformDocTypeFileExtRelation getOneById(long docTypeFileExtRelationID);

    @Select("select * from platformDocTypeFileExtRelation where docTypeFileExtRelationGuid=#{docTypeFileExtRelationGuid}")
    public PlatformDocTypeFileExtRelation getOneByGuid(String docTypeFileExtRelationGuid);

    @Select("select * from platformDocTypeFileExtRelation where docTypeId=#{docTypeId} and platformFileExtID=#{platformFileExtID}")
    public List<PlatformDocTypeFileExtRelation> getOneByForeignKeysGroup(long docTypeId,long platformFileExtID);

    @Select("select * from platformDocTypeFileExtRelation where docTypeId=#{docTypeId} and platformFileExtID=#{platformFileExtID} and docTypeFileExtRelationGuid<>#{docTypeFileExtRelationGuid}")
    public List<PlatformDocTypeFileExtRelation> getOneByForeignKeysGroupWhenUpdate(long docTypeId,long platformFileExtID,String docTypeFileExtRelationGuid);

    @Select("select * from platformDocTypeFileExtRelation where isActive=1")
    public List<PlatformDocTypeFileExtRelation> getAll();

    @Update("update platformDocTypeFileExtRelation set docTypeId=#{docTypeId},platformFileExtID=#{platformFileExtID} where docTypeFileExtRelationGuid=#{docTypeFileExtRelationGuid}")
    public int update(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation);

    @Insert({
            "insert into platformDocTypeFileExtRelation (docTypeFileExtRelationGuid,docTypeId,platformFileExtID,createTime)",
            " values (#{docTypeFileExtRelationGuid},#{docTypeId},#{platformFileExtID},#{createTime})"})
    public int insert(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation);

    @Delete("delete from platformDocTypeFileExtRelation where docTypeFileExtRelationID=#{docTypeFileExtRelationID}")
    public int deleteById(long docTypeFileExtRelationID);

    @Delete("delete from platformDocTypeFileExtRelation where docTypeFileExtRelationGuid=#{docTypeFileExtRelationGuid}")
    public int deleteByGuid(String docTypeFileExtRelationGuid);

    @Update("update platformDocTypeFileExtRelation set isActive=0 where docTypeFileExtRelationID=#{docTypeFileExtRelationID}")
    public int setToNotActive(long docTypeFileExtRelationID);

    @Update("update platformDocTypeFileExtRelation set isActive=1 where docTypeFileExtRelationID=#{docTypeFileExtRelationID}")
    public int setToActive(long docTypeFileExtRelationID);

    //-------------------------------------------------------------------------------------------------------------------------------//
    //根据平台文件类型返回下属文件扩展名

}
