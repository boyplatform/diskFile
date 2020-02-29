package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.OperationLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;


@Repository
public interface OperationLogDao {

    @Select("select * from operationLog where operationLogId=#{operationLogId}")
    public OperationLog getOneById(int operationLogId);

    @Select("select * from operationLog where operationLogGuid=#{operationLogGuid}")
    public OperationLog getOneByGuid(String operationLogGuid);

    @Select("select * from operationLog where isActive=1")
    public List<OperationLog> getAll();

    @Update("update operationLog set operationStorageClusterType=#{operationStorageClusterType},userId=#{userId},userName=#{userName},operationType=#{operationType},operationLogTime=#{operationLogTime},appId=#{appId},docId=#{docId},exfModuleId=#{exfModuleId},viewId=#{viewId},platformControllerId=#{platformControllerId},platformActionId=#{platformActionId},usingObjectId=#{usingObjectId},bizUserRoleId=#{bizUserRoleId},deviceId=#{deviceId},devLangId=#{devLangId},workFlowStatusId=#{workFlowStatusId},isDocExistingCurrently=#{isDocExistingCurrently},isActive=#{isActive},userGuid=#{userGuid},appGuid=#{appGuid} where operationLogGuid=#{operationLogGuid}")
    public int update(OperationLog operationLog);

    @Insert({"insert into operationLog(operationStorageClusterType,operationLogGuid,userId,userName,operationType,operationLogTime,appId,docId,exfModuleId,viewId,platformControllerId,platformActionId,usingObjectId,bizUserRoleId,deviceId,devLangId,workFlowStatusId,isDocExistingCurrently,isActive,userGuid,appGuid)",
            " values(#{operationStorageClusterType},#{operationLogGuid},#{userId},#{userName},#{operationType},#{operationLogTime},#{appId},#{docId},#{exfModuleId},#{viewId},#{platformControllerId},#{platformActionId},#{usingObjectId},#{bizUserRoleId},#{deviceId},#{devLangId},#{workFlowStatusId},#{isDocExistingCurrently},#{isActive},#{userGuid},#{appGuid})"})
    public int insert(OperationLog operationLog);

    @Delete("delete from operationLog where operationLogId=#{operationLogId}")
    public int deleteById(int operationLogId);

    @Delete("delete from operationLog where operationLogGuid=#{operationLogGuid}")
    public int deleteByGuid(String operationLogGuid);

    @Update("update operationLog set isActive=0 where operationLogId=#{operationLogId}")
    public int setToNotActive(int operationLogId);

    @Update("update operationLog set isActive=1 where operationLogId=#{operationLogId}")
    public int setToActive(int operationLogId);
}
