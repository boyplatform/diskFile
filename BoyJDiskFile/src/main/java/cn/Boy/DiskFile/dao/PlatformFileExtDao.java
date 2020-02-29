package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.PlatformFileExt;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformFileExtDao {

    @Select("select * from platformFileExt where fileExtID=#{fileExtID}")
    public PlatformFileExt getOneById(long fileExtID);

    @Select("select * from platformFileExt where fileExtGuid=#{fileExtGuid}")
    public PlatformFileExt getOneByGuid(String fileExtGuid);

    @Select("select * from platformFileExt where fileExtName=#{fileExtName}")
    public List<PlatformFileExt> getOneByName(String fileExtName);

    @Select("select * from platformFileExt where fileExtName=#{fileExtName} and fileExtGuid<>#{fileExtGuid}")
    public List<PlatformFileExt> getOneByNameWhenUpdate(String fileExtName,String fileExtGuid);

    @Select("select * from platformFileExt where isActive=1")
    public List<PlatformFileExt> getAll();

    @Update("update platformFileExt set fileExtName=#{fileExtName},isActive=#{isActive} where fileExtGuid=#{fileExtGuid}")
    public int update(PlatformFileExt platformFileExt);

    @Insert({"insert into platformFileExt(fileExtGuid,fileExtName,createTime,isActive)"
            ," values(#{fileExtGuid},#{fileExtName},#{createTime},#{isActive})"})
    public int insert(PlatformFileExt platformFileExt);

    @Delete("delete from platformFileExt where fileExtID=#{fileExtID}")
    public int deleteById(long fileExtID);

    @Delete("delete from platformFileExt where fileExtGuid=#{fileExtGuid}")
    public int deleteByGuid(String fileExtGuid);

    @Update("update platformFileExt set isActive=0 where fileExtID=#{fileExtID}")
    public int setToNotActive(long fileExtID);

    @Update("update platformFileExt set isActive=1 where fileExtID=#{fileExtID}")
    public int setToActive(long fileExtID);

}
