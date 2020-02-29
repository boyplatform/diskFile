package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformOpsDao {

    @Delete("delete from platformDocTypeFileExtRelation where docTypeFileExtRelationID>0")
    public int deletePlatformDocTypeFileExtRelation ();

    @Delete("delete from AppPlatformDocTypeRelation where appdocTypeRelationID>0")
    public int deleteAppPlatformDocTypeRelation ();

    @Delete("delete from documentList where docId>0")
    public int deleteDocumentList ();

    @Delete("delete from platformFileExt where fileExtID>0")
    public int deletePlatformFileExt ();

    @Delete("delete from platformDocType where docTypeId>0")
    public int deletePlatformDocType ();

    @Delete("delete from RequestLog where reqId>0")
    public int deleteRequestLog ();

    @Delete("delete from operationLog where operationLogId>0")
    public int deleteOperationLog ();

    @Delete("delete from DBUpgradeHistory where nodeDbUpgradeHIstoryId>0")
    public int deleteDBUpgradeHistory ();

    @Delete("delete from unitNodeRelation where unitNodeRelationId>0")
    public int deleteUnitNodeRelation ();

    @Delete("delete from customerDbList where id>0")
    public int deleteCustomerDbList ();

}
