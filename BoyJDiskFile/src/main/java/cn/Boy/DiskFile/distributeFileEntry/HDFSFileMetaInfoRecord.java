package cn.Boy.DiskFile.distributeFileEntry;

import cn.Boy.DiskFile.pojo.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.stereotype.Service;

@Service("HDFSFileMetaInfoRecord")
public class HDFSFileMetaInfoRecord implements ICommonFileMetaInfoRecorder {

    private final Log log = LogFactory.getLog(HDFSFileMetaInfoRecord.class);
    //For platformFileExt
    public PlatformFileExt addPlatformFileExt(PlatformFileExt platformFileExt)
    {
        return  null;
    }
    public PlatformFileExt updatePlatformFileExt(PlatformFileExt platformFileExt)
    {
        return null;
    }
    public boolean deletePlatformFileExt(int fileExtID){

        return true;
    }
    public boolean recoverPlatformFileExt(int fileExtID){

        return true;
    }
    public List<PlatformFileExt> getAllPlatformFileExt(){

        return  null;
    }
    public PlatformFileExt getOnePlatformFileExtById(int fileExtID){

        return null;
    }
    public PlatformFileExt getOnePlatformFileExtByGuid(String fileExtGuid){

        return  null;
    }
    public PlatformFileExt getOnePlatformFileExtByName(String fileExtName){

        return null;
    }
    //For platformDocType
    public PlatformDocType addPlatformDocType(PlatformDocType platformDocType){

        return null;
    }
    public PlatformDocType updatePlatformDocType(PlatformDocType platformDocType){

        return null;
    }
    public boolean deletePlatformDocType(int docTypeId){

        return true;
    }
    public boolean recoverPlatformDocType(int docTypeId){
        return  true;
    }
    public List<PlatformDocType> getAllPlatformDocType(){

        return  null;
    }
    public PlatformDocType getOnePlatformDocTypeById(int docTypeId){

        return  null;
    }
    public PlatformDocType getOnePlatformDocTypeByGuid(String docTypeGuid){

        return null;
    }
    public PlatformDocType getOnePlatformDocTypeByName(String docTypeName){

        return  null;
    }

    //For platformDocTypeFileExtRelation
    public PlatformDocTypeFileExtRelation addPlatformDocTypeFileExtRelation(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation){

        return null;
    }
    public PlatformDocTypeFileExtRelation updatePlatformDocTypeFileExtRelation(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation){

        return null;
    }
    public boolean deletePlatformDocTypeFileExtRelation(int docTypeFileExtRelationID){

        return  true;
    }
    public boolean recoverPlatformDocTypeFileExtRelation(int docTypeFileExtRelationID){

        return  true;
    }
    public List<PlatformDocTypeFileExtRelation> getAllPlatformDocTypeFileExtRelation(){

        return  null;
    }
    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationById(int docTypeFileExtRelationID){

        return null;
    }

    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationByGuid(String docTypeFileExtRelationGuid){

        return  null;
    }
    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationByForeignKeyGroup(long docTypeId,long platformFileExtID){

        return  null;
    }

    //For AppPlatformDocTypeRelation
    public AppPlatformDocTypeRelation addAppPlatformDocTypeRelation(AppPlatformDocTypeRelation appPlatformDocTypeRelation){

        return  null;
    }
    public AppPlatformDocTypeRelation updateAppPlatformDocTypeRelation(AppPlatformDocTypeRelation appPlatformDocTypeRelation){

        return null;
    }
    public boolean deleteAppPlatformDocTypeRelation(int appdocTypeRelationID){

        return  true;
    }
    public boolean recoverAppPlatformDocTypeRelation(int appdocTypeRelationID){

        return  true;
    }
    public List<AppPlatformDocTypeRelation> getAllAppPlatformDocTypeRelation(){

        return  null;
    }
    public AppPlatformDocTypeRelation getOneAppPlatformDocTypeRelationById(int appdocTypeRelationID){

        return  null;
    }

    //For documentList
    public DocumentList addDocumentList(DocumentList documentList){

        return  null;
    }
    public DocumentList updateDocumentList(DocumentList documentList){

        return  null;
    }
    public boolean deleteDocumentList(String docGuid){

        return  true;
    }

    public boolean hardDeleteDocumentList(String filePath,String fileName,long docTypeId,long fileExtID,String fileExtName,String userGuid) {

        return  true;
    }


    public boolean recoverDocumentList(String docGuid){

        return true;
    }

    @Transactional(readOnly=true)
    public List<DocumentList> getAllDocumentList(){

        return null;
    }

    @Transactional(readOnly=true)
    public List<DocumentList> getAllActiveDocumentList(){
        return null;
    }

    @Transactional(readOnly=true)
    public List<DocumentList> getAllNotActiveDocumentList(){

        return null;
    }

    public DocumentList getOneDocumentListById(int docId){

        return null;
    }


    public DocumentList getOneDocumentListByGuid(String docGuid){

        return  null;
    }

    //RequestLog
    public RequestLog addRequestLog(RequestLog requestLog){

        return  null;
    }
    public RequestLog updateRequestLog(RequestLog requestLog){

        return  null;
    }
    public boolean deleteRequestLog(int reqId){

        return true;
    }
    public boolean recoverRequestLog(int reqId){

        return  true;
    }
    public List<RequestLog> getRequestLogList(){
        return  null;
    }
    public RequestLog getRequestLogById(int reqId){

        return  null;
    }

    //operationLog
    public OperationLog addOperationLog(OperationLog operationLog){

        return null;
    }
    public OperationLog updateOperationLog(OperationLog operationLog){

        return null;
    }
    public boolean deleteOperationLog(int operationLogId){

        return  true;
    }
    public boolean recoverOperationLog(int operationLogId){

        return true;
    }
    public List<OperationLog> getOperationLogList(){

        return  null;
    }
    public OperationLog getOperationLogById(int operationLogId){

        return  null;
    }
}
