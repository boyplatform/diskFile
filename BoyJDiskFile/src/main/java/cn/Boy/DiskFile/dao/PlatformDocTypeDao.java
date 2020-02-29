package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.PlatformDocType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformDocTypeDao {

    @Select("select * from platformDocType where docTypeId=#{docTypeId}")
    public PlatformDocType getOneById(long docTypeId);

    @Select("select * from platformDocType where docTypeGuid=#{docTypeGuid}")
    public PlatformDocType getOneByGuid(String docTypeGuid);

    @Select("select * from platformDocType where docTypeName=#{docTypeName}")
    public List<PlatformDocType> getOneByName(String docTypeName);

    @Select("select * from platformDocType where docTypeName=#{docTypeName} and docTypeGuid<>#{docTypeGuid}")
    public List<PlatformDocType> getOneByNameWhenUpdate(String docTypeName,String docTypeGuid);

    @Select("select * from platformDocType where isActive=1")
    public List<PlatformDocType> getAll();

    @Update("update platformDocType set docTypeName=#{docTypeName},docTypeDesc=#{docTypeDesc},maxFileSize=#{maxFileSize},fileShareFolder=#{fileShareFolder},comment=#{comment},isActive=#{isActive} where docTypeGuid=#{docTypeGuid}")
    public int update(PlatformDocType platformDocType);

    @Insert({"insert into platformDocType(docTypeGuid,docTypeName,docTypeDesc,maxFileSize,fileShareFolder,comment,isActive,createTime)",
            " values(#{docTypeGuid},#{docTypeName},#{docTypeDesc},#{maxFileSize},#{fileShareFolder},#{comment},#{isActive},#{createTime})"})
    public int insert(PlatformDocType platformDocType);

    @Delete("delete from platformDocType where docTypeId=#{docTypeId}")
    public int deleteById(long docTypeId);

    @Delete("delete from platformDocType where docTypeGuid=#{docTypeGuid}")
    public int deleteByGuid(String docTypeGuid);

    @Update("update platformDocType set isActive=0 where docTypeId=#{docTypeId}")
    public int setToNotActive(long docTypeId);

    @Update("update platformDocType set isActive=1 where docTypeId=#{docTypeId}")
    public int setToActive(long docTypeId);
}
