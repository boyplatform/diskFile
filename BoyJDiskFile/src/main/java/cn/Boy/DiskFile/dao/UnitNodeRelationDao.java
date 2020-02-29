package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.UnitNodeRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitNodeRelationDao {

    @Select("select * from unitNodeRelation where unitNodeRelationId=#{unitNodeRelationId}")
    public UnitNodeRelation getOneById(int unitNodeRelationId);

    @Select("select * from unitNodeRelation where unitNodeRelationGuid=#{unitNodeRelationGuid}")
    public UnitNodeRelation getOneByGuid(String unitNodeRelationGuid);

    @Select("select * from unitNodeRelation where isActive=1")
    public List<UnitNodeRelation> getAll();

    @Update("update unitNodeRelation set unitNodeId=#{unitNodeId},appId=#{appId},unitNodeGuid=#{unitNodeGuid},isActive=#{isActive},createTime=#{createTime} where unitNodeRelationGuid=#{unitNodeRelationGuid}")
    public int update(UnitNodeRelation unitNodeRelation);

    @Insert({"insert into unitNodeRelation(unitNodeId,appId,unitNodeGuid,isActive,createTime,unitNodeRelationGuid)",
            " values(#{unitNodeId},#{appId},#{unitNodeGuid},#{isActive},#{createTime},#{unitNodeRelationGuid})"})
    public int insert(UnitNodeRelation unitNodeRelation);

    @Delete("delete from unitNodeRelation where unitNodeRelationId=#{unitNodeRelationId}")
    public int deleteById(int unitNodeRelationId);

    @Delete("delete from unitNodeRelation where unitNodeRelationGuid=#{unitNodeRelationGuid}")
    public int deleteByGuid(String unitNodeRelationGuid);

    @Update("update unitNodeRelation set isActive=0 where unitNodeRelationId=#{unitNodeRelationId}")
    public int setToNotActive(int unitNodeRelationId);

    @Update("update unitNodeRelation set isActive=1 where unitNodeRelationId=#{unitNodeRelationId}")
    public int setToActive(int unitNodeRelationId);
}
