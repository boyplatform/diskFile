package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.DBUpgradeHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface DBUpgradeHistoryDao {

    @Select("select * from DBUpgradeHistory where nodeDbUpgradeHIstoryId=#{nodeDbUpgradeHIstoryId}")
    public DBUpgradeHistory getOneById(int nodeDbUpgradeHIstoryId);

    @Select("select * from DBUpgradeHistory where nodeDbUpgradeHIstoryGuid=#{nodeDbUpgradeHIstoryGuid}")
    public DBUpgradeHistory getOneByGuid(String nodeDbUpgradeHIstoryGuid);

    @Select("select * from DBUpgradeHistory where isActive=1")
    public List<DBUpgradeHistory> getAll();

    @Update("update DBUpgradeHistory set nodeDbGuid=#{nodeDbGuid},nodeDbName=#{nodeDbName},fromPlatformDbVersion=#{fromPlatformDbVersion},toPlatformDbVersion=#{toPlatformDbVersion},upgradeTime=#{upgradeTime},currentPlatformUser=#{currentPlatformUser},platformUserLoginName=#{platformUserLoginName},platformUserName=#{platformUserName},platformHostGuid=#{platformHostGuid},comments=#{comments},isActive=#{isActive} where nodeDbUpgradeHIstoryGuid=#{nodeDbUpgradeHIstoryGuid}")
    public int update(DBUpgradeHistory dBUpgradeHistory);

    @Insert({"insert into DBUpgradeHistory(nodeDbGuid,nodeDbName,fromPlatformDbVersion,toPlatformDbVersion,upgradeTime,currentPlatformUser,platformUserLoginName,platformUserName,platformHostGuid,comments,nodeDbUpgradeHIstoryGuid,isActive)",
            " values(#{nodeDbGuid},#{nodeDbName},#{fromPlatformDbVersion},#{toPlatformDbVersion},#{upgradeTime},#{currentPlatformUser},#{platformUserLoginName},#{platformUserName},#{platformHostGuid},#{comments},#{nodeDbUpgradeHIstoryGuid},#{isActive})"})
    public int insert(DBUpgradeHistory dBUpgradeHistory);

    @Delete("delete from DBUpgradeHistory where nodeDbUpgradeHIstoryId=#{nodeDbUpgradeHIstoryId}")
    public int deleteById(int nodeDbUpgradeHIstoryId);

    @Delete("delete from DBUpgradeHistory where nodeDbUpgradeHIstoryGuid=#{nodeDbUpgradeHIstoryGuid}")
    public int deleteByGuid(String nodeDbUpgradeHIstoryGuid);

    @Update("update DBUpgradeHistory set isActive=0 where nodeDbUpgradeHIstoryId=#{nodeDbUpgradeHIstoryId}")
    public int setToNotActive(int nodeDbUpgradeHIstoryId);

    @Update("update DBUpgradeHistory set isActive=1 where nodeDbUpgradeHIstoryId=#{nodeDbUpgradeHIstoryId}")
    public int setToActive(int nodeDbUpgradeHIstoryId);
}
