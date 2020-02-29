package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.CustomerDbList;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerDbListDao {

    @Select("select * from customerDbList where id=#{id}")
    public CustomerDbList getOneById(int id);

    @Select("select * from customerDbList where guid=#{guid}")
    public CustomerDbList getOneByGuid(String guid);

    @Select("select * from customerDbList where isActive=1")
    public List<CustomerDbList> getAll();

    @Update("update customerDbList set dataSourceClassName=#{dataSourceClassName},dataSourceUser=#{dataSourceUser},dataSourcePassword=#{dataSourcePassword},dataSourceDataBaseName=#{dataSourceDataBaseName},dataSourcePortNumber=#{dataSourcePortNumber},dataSourceServerName=#{dataSourceServerName},remark=#{remark},dbTypeNum=#{dbTypeNum},isActive=#{isActive} where guid=#{guid}")
    public int update(CustomerDbList customerDbList);

    @Insert({"insert into customerDbList(guid,dataSourceClassName,dataSourceUser,dataSourcePassword,dataSourceDataBaseName,dataSourcePortNumber,dataSourceServerName,remark,dbTypeNum,isActive,createTime)"
            ," values(#{guid},#{dataSourceClassName},#{dataSourceUser},#{dataSourcePassword},#{dataSourceDataBaseName},#{dataSourcePortNumber},#{dataSourceServerName},#{remark},#{dbTypeNum},#{isActive},#{createTime})"})
    public int insert(CustomerDbList customerDbList);

    @Delete("delete from customerDbList where id=#{id}")
    public int deleteById(int id);

    @Delete("delete from customerDbList where guid=#{guid}")
    public int deleteByGuid(String guid);

    @Update("update customerDbList set isActive=0 where id=#{id}")
    public int setToNotActive(int id);

    @Update("update customerDbList set isActive=1 where id=#{id}")
    public int setToActive(int id);

}
