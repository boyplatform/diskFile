package cn.Boy.DiskFile.distributeFileEntry;

import cn.Boy.DiskFile.pojo.PlatformFileExt;
import cn.Boy.DiskFile.pojo.PlatformDocType;
import cn.Boy.DiskFile.pojo.PlatformDocTypeFileExtRelation;
import cn.Boy.DiskFile.pojo.AppPlatformDocTypeRelation;
import cn.Boy.DiskFile.pojo.DocumentList;
import cn.Boy.DiskFile.pojo.RequestLog;
import cn.Boy.DiskFile.pojo.OperationLog;
import java.util.List;

public interface ICommonFileMetaInfoRecorder{

    //For platformFileExt
    public PlatformFileExt addPlatformFileExt(PlatformFileExt platformFileExt);
    public PlatformFileExt updatePlatformFileExt(PlatformFileExt platformFileExt);
    public boolean deletePlatformFileExt(int fileExtID);
    public boolean recoverPlatformFileExt(int fileExtID);
    public List<PlatformFileExt> getAllPlatformFileExt();
    public PlatformFileExt getOnePlatformFileExtById(int fileExtID);
    public PlatformFileExt getOnePlatformFileExtByGuid(String fileExtGuid);
    public PlatformFileExt getOnePlatformFileExtByName(String fileExtName);

    //For platformDocType
    public PlatformDocType addPlatformDocType(PlatformDocType platformDocType);
    public PlatformDocType updatePlatformDocType(PlatformDocType platformDocType);
    public boolean deletePlatformDocType(int docTypeId);
    public boolean recoverPlatformDocType(int docTypeId);
    public List<PlatformDocType> getAllPlatformDocType();
    public PlatformDocType getOnePlatformDocTypeById(int docTypeId);
    public PlatformDocType getOnePlatformDocTypeByGuid(String docTypeGuid);
    public PlatformDocType getOnePlatformDocTypeByName(String docTypeName);

    //For platformDocTypeFileExtRelation
    public PlatformDocTypeFileExtRelation addPlatformDocTypeFileExtRelation(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation);
    public PlatformDocTypeFileExtRelation updatePlatformDocTypeFileExtRelation(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation);
    public boolean deletePlatformDocTypeFileExtRelation(int docTypeFileExtRelationID);
    public boolean recoverPlatformDocTypeFileExtRelation(int docTypeFileExtRelationID);
    public List<PlatformDocTypeFileExtRelation> getAllPlatformDocTypeFileExtRelation();
    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationById(int docTypeFileExtRelationID);
    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationByGuid(String docTypeFileExtRelationGuid);
    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationByForeignKeyGroup(long docTypeId,long platformFileExtID);

    //For AppPlatformDocTypeRelation
    public AppPlatformDocTypeRelation addAppPlatformDocTypeRelation(AppPlatformDocTypeRelation appPlatformDocTypeRelation);
    public AppPlatformDocTypeRelation updateAppPlatformDocTypeRelation(AppPlatformDocTypeRelation appPlatformDocTypeRelation);
    public boolean deleteAppPlatformDocTypeRelation(int appdocTypeRelationID);
    public boolean recoverAppPlatformDocTypeRelation(int appdocTypeRelationID);
    public List<AppPlatformDocTypeRelation> getAllAppPlatformDocTypeRelation();
    public AppPlatformDocTypeRelation getOneAppPlatformDocTypeRelationById(int appdocTypeRelationID);

    //For documentList
    public DocumentList addDocumentList(DocumentList documentList);
    public DocumentList updateDocumentList(DocumentList documentList);
    public boolean deleteDocumentList(String docGuid);
    public boolean recoverDocumentList(String docGuid);
    public List<DocumentList> getAllDocumentList();
    public DocumentList getOneDocumentListById(int docId);
    public boolean hardDeleteDocumentList(String filePath,String fileName,long docTypeId,long fileExtID,String fileExtName,String userGuid);
    public DocumentList getOneDocumentListByGuid(String docGuid);
    public List<DocumentList> getAllNotActiveDocumentList();
    public List<DocumentList> getAllActiveDocumentList();
    //RequestLog
    public RequestLog addRequestLog(RequestLog requestLog);
    public RequestLog updateRequestLog(RequestLog requestLog);
    public boolean deleteRequestLog(int reqId);
    public boolean recoverRequestLog(int reqId);
    public List<RequestLog> getRequestLogList();
    public RequestLog getRequestLogById(int reqId);

    //operationLog
    public OperationLog addOperationLog(OperationLog operationLog);
    public OperationLog updateOperationLog(OperationLog operationLog);
    public boolean deleteOperationLog(int operationLogId);
    public boolean recoverOperationLog(int operationLogId);
    public List<OperationLog> getOperationLogList();
    public OperationLog getOperationLogById(int operationLogId);



}
