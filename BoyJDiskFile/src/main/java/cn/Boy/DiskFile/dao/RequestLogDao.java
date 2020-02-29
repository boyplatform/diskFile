package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.RequestLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;


@Repository
public interface RequestLogDao {

    @Select("select * from RequestLog where reqId=#{reqId}")
    public RequestLog getOneById(int reqId);

    @Select("select * from RequestLog where reqGuid=#{reqGuid}")
    public RequestLog getOneByGuid(String reqGuid);

    @Select("select * from RequestLog where isActive=1")
    public List<RequestLog> getAll();

    @Update("update RequestLog set appId=#{appId},appName=#{appName},appGuid=#{appGuid},userId=#{userId},url=#{url},createTime=#{createTime},reqStorageClusterType=#{reqStorageClusterType},isActive=#{isActive} where reqGuid=#{reqGuid}")
    public int update(RequestLog requestLog);

    @Insert({"insert into RequestLog(appId,appName,appGuid,userId,url,createTime,reqStorageClusterType,reqGuid,isActive,userGuid)",
            " values(#{appId},#{appName},#{appGuid},#{userId},#{url},#{createTime},#{reqStorageClusterType},#{reqGuid},#{isActive},#{userGuid})"})
    public int insert(RequestLog requestLog);

    @Delete("delete from RequestLog where reqId=#{reqId}")
    public int deleteById(int reqId);

    @Delete("delete from RequestLog where reqGuid=#{reqGuid}")
    public int deleteByGuid(String reqGuid);

    @Update("update RequestLog set isActive=0 where reqId=#{reqId}")
    public int setToNotActive(int reqId);

    @Update("update RequestLog set isActive=1 where reqId=#{reqId}")
    public int setToActive(int reqId);

}
