package cn.Boy.DiskFile.distributeFileEntry;

import java.util.List;
import java.util.Map;


import cn.Boy.DiskFile.pojo.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.dao.PlatformFileExtDao;
import cn.Boy.DiskFile.dao.PlatformDocTypeDao;
import cn.Boy.DiskFile.dao.PlatformDocTypeFileExtRelationDao;
import cn.Boy.DiskFile.dao.AppPlatformDocTypeRelationDao;
import cn.Boy.DiskFile.dao.DocumentListDao;
import cn.Boy.DiskFile.dao.RequestLogDao;
import cn.Boy.DiskFile.dao.OperationLogDao;

@Service("CephFileMetaInfoRecord")
@Scope("prototype")
public class CephFileMetaInfoRecord implements ICommonFileMetaInfoRecorder{

    private final Log log = LogFactory.getLog(CephFileMetaInfoRecord.class);
    @Autowired private PlatformFileExtDao platformFileExtDao;
    @Autowired private PlatformDocTypeDao platformDocTypeDao;
    @Autowired private PlatformDocTypeFileExtRelationDao platformDocTypeFileExtRelationDao;
    @Autowired private AppPlatformDocTypeRelationDao appPlatformDocTypeRelationDao;
    @Autowired private DocumentListDao documentListDao;
    @Autowired private RequestLogDao requestLogDao;
    @Autowired private OperationLogDao operationLogDao;
    //Operation on file Basic Meta data tables
   //For platformFileExt
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public PlatformFileExt addPlatformFileExt(PlatformFileExt platformFileExt){
        if(log.isTraceEnabled()) {
            log.trace("addPlatformFileExt started for " + platformFileExt);
        }

        if(platformFileExtDao.getOneByName(platformFileExt.getFileExtName()).size()>0){
            throw new RuntimeException("The PlatformFileExt has been existed under platform");
        }

        if(platformFileExtDao.insert(platformFileExt)<= 0) {
            throw new RuntimeException("addPlatformFileExt failed");
        }
        return platformFileExt;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public PlatformFileExt updatePlatformFileExt(PlatformFileExt platformFileExt) {
        if(log.isTraceEnabled()) {
            log.trace("updatePlatformFileExt started for " + platformFileExt);
        }

        if(platformFileExtDao.getOneByNameWhenUpdate(platformFileExt.getFileExtName(),platformFileExt.getFileExtGuid().trim()).size()>0){
            throw new RuntimeException("The PlatformFileExt has been existed under platform");
        }

        if(platformFileExtDao.update(platformFileExt)<=0){
            throw new RuntimeException("updatePlatformFileExt failed");
        }
        return platformFileExt;
    }

    public boolean deletePlatformFileExt(long fileExtID) {
        if(platformFileExtDao.setToNotActive(fileExtID)>0)
        {
             return true;
        }else{
             return false;
        }
    }

    public boolean recoverPlatformFileExt(long fileExtID) {
        if(platformFileExtDao.setToActive(fileExtID)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean setPlatformFileExtBoardCastedFlag(long fileExtID,long boardCastCount) {
        if(platformFileExtDao.setToBoardCasted(fileExtID,boardCastCount)>0)
        {
            return true;
        }else{
            return false;
        }
    }
    @Transactional(readOnly=true)
    public List<PlatformFileExt> getAllPlatformFileExt(){
        try {
             Thread.sleep(10);
             if(log.isTraceEnabled()) {
                log.trace("getAllPlatformFileExt started  ");
             }
             List<PlatformFileExt> list = platformFileExtDao.getAll();

             return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllPlatformFileExt failed  ");
            }
            return null;
        }
    }

    public PlatformFileExt getOnePlatformFileExtById(int fileExtID){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformFileById started  ");
            }
            PlatformFileExt platformFileExt = platformFileExtDao.getOneById(fileExtID);

            return platformFileExt;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformFileById failed  ");
            }
            return null;
        }
    }
    public PlatformFileExt getOnePlatformFileExtByGuid(String fileExtGuid){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformFileExtByGuid started  :"+fileExtGuid);
            }
            PlatformFileExt platformFileExt = platformFileExtDao.getOneByGuid(fileExtGuid);

            return platformFileExt;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformFileExtByGuid failed  ");
            }
            return null;
        }
    }
    public PlatformFileExt getOnePlatformFileExtByName(String fileExtName){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformFileExtByName started  ");
            }
            PlatformFileExt platformFileExt = platformFileExtDao.getOneByName(fileExtName).stream().findFirst().get();

            return platformFileExt;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformFileExtByName failed  "+e.getMessage());
            }
            return null;
        }
    }




    //For platformDocType
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public PlatformDocType addPlatformDocType(PlatformDocType platformDocType){

        if(log.isTraceEnabled()) {
            log.trace("addPlatformDocType started for " + platformDocType);
        }

        if(platformDocTypeDao.getOneByName(platformDocType.getDocTypeName()).size()>0){
            throw new RuntimeException("The PlatformDocType has been existed under platform");
        }

        if(platformDocTypeDao.insert(platformDocType)<=0) {
            throw new RuntimeException("addPlatformDocType failed");
        }
        return platformDocType;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public PlatformDocType updatePlatformDocType(PlatformDocType platformDocType) {
        if(log.isTraceEnabled()) {
            log.trace("updatePlatformDocType started for " + platformDocType);
        }

        if(platformDocTypeDao.getOneByNameWhenUpdate(platformDocType.getDocTypeName(),platformDocType.getDocTypeGuid().trim()).size()>0){
            throw new RuntimeException("The PlatformDocType has been existed under platform");
        }

        if(platformDocTypeDao.update(platformDocType)<=0){

            throw new RuntimeException("updatePlatformDocType failed");
        }
        return platformDocType;
    }

    public boolean deletePlatformDocType(long docTypeId) {
        if(platformDocTypeDao.setToNotActive(docTypeId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recoverPlatformDocType(long docTypeId) {
        if(platformDocTypeDao.setToActive(docTypeId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean setPlatformDocTypeBoardCastedFlag(long docTypeId,long boardCastCount){
        if(platformDocTypeDao.setToBoardCasted(docTypeId,boardCastCount)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<PlatformDocType> getAllPlatformDocType(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAllPlatformDocType started  ");
            }
            List<PlatformDocType> list = platformDocTypeDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllPlatformDocType failed  ");
            }
            return null;
        }
    }

    public PlatformDocType getOnePlatformDocTypeById(int docTypeId){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeById started  ");
            }
            PlatformDocType platformDocType = platformDocTypeDao.getOneById(docTypeId);

            return platformDocType;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeById failed  ");
            }
            return null;
        }
    }

    public PlatformDocType getOnePlatformDocTypeByGuid(String docTypeGuid){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeById started  ");
            }
            PlatformDocType platformDocType = platformDocTypeDao.getOneByGuid(docTypeGuid);

            return platformDocType;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeById failed  ");
            }
            return null;
        }
    }

    public PlatformDocType getOnePlatformDocTypeByName(String docTypeName){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeByName started  ");
            }
            PlatformDocType platformDocType = platformDocTypeDao.getOneByName(docTypeName).stream().findFirst().get();

            return platformDocType;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeByName failed  ");
            }
            return null;
        }
    }

    //For platformDocTypeFileExtRelation
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public PlatformDocTypeFileExtRelation addPlatformDocTypeFileExtRelation(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation){
        if(log.isTraceEnabled()) {
            log.trace("addPlatformDocTypeFileExtRelation started for " + platformDocTypeFileExtRelation);
        }
        //verify fileExt existing
        log.trace("current relation File Ext ID:"+platformDocTypeFileExtRelation.getPlatformFileExtID()+" | current relation platformDocType ID:"+platformDocTypeFileExtRelation.getDocTypeId());
        if(platformFileExtDao.getOneById(platformDocTypeFileExtRelation.getPlatformFileExtID())==null){
            throw new RuntimeException("The file extension under your defined relationship is not existing currently");
        }
        //verify DocType existing
        if(platformDocTypeDao.getOneById(platformDocTypeFileExtRelation.getDocTypeId())==null){
            throw new RuntimeException("The DocType under your defined relationship is not existing currently");
        }
        //verify PlatformDocTypeFileExtRelation  existing
        if(platformDocTypeFileExtRelationDao.getOneByForeignKeysGroup(platformDocTypeFileExtRelation.getDocTypeId(),platformDocTypeFileExtRelation.getPlatformFileExtID()).size()>0)
        {
            throw new RuntimeException("The platformDocType and file extension's relationship which you try to define has been existed under the platform.");
        }


        if(platformDocTypeFileExtRelationDao.insert(platformDocTypeFileExtRelation)<=0) {
            throw new RuntimeException("addPlatformDocTypeFileExtRelation failed");
        }
        return platformDocTypeFileExtRelation;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public PlatformDocTypeFileExtRelation updatePlatformDocTypeFileExtRelation(PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation) {
        if(log.isTraceEnabled()) {
            log.trace("updatePlatformDocTypeFileExtRelation started for " + platformDocTypeFileExtRelation);
        }

        //verify fileExt existing
        if(platformFileExtDao.getOneById(platformDocTypeFileExtRelation.getPlatformFileExtID().intValue())==null){
            throw new RuntimeException("The file extend under the relation is not existing currently");
        }
        //verify DocType existing
        if(platformDocTypeDao.getOneById(platformDocTypeFileExtRelation.getDocTypeId().intValue())==null){
            throw new RuntimeException("The DocType under the relation is not existing currently");
        }

        //verify PlatformDocTypeFileExtRelation  existing
        if(platformDocTypeFileExtRelationDao.getOneByForeignKeysGroupWhenUpdate(platformDocTypeFileExtRelation.getDocTypeId().intValue(),platformDocTypeFileExtRelation.getPlatformFileExtID().intValue(),platformDocTypeFileExtRelation.getDocTypeFileExtRelationGuid().trim()).size()>0)
        {
            throw new RuntimeException("The platformDocType and file extension's relationship which you try to define has been existed under the platform.");
        }


        if(platformDocTypeFileExtRelationDao.update(platformDocTypeFileExtRelation)<=0)
        {
            throw new RuntimeException("updatePlatformDocTypeFileExtRelation failed");
        }
        return platformDocTypeFileExtRelation;
    }

    public boolean deletePlatformDocTypeFileExtRelation(long docTypeFileExtRelationID) {
        if(platformDocTypeFileExtRelationDao.setToNotActive(docTypeFileExtRelationID)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recoverPlatformDocTypeFileExtRelation(long docTypeFileExtRelationID) {
        if(platformDocTypeFileExtRelationDao.setToActive(docTypeFileExtRelationID)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean setPlatformDocTypeFileExtRelationBoardCastedFlag(long docTypeFileExtRelationID,long boardCastCount){

        if(platformDocTypeFileExtRelationDao.setToBoardCasted(docTypeFileExtRelationID,boardCastCount)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<PlatformDocTypeFileExtRelation> getPlatformDocTypeFileExtRelationListByFileExtName(String fileExtName){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getPlatformDocTypeFileExtRelationListByFileExtName started  ");
            }
            List<PlatformDocTypeFileExtRelation> list = platformDocTypeFileExtRelationDao.getListByFileExtName(fileExtName);

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getPlatformDocTypeFileExtRelationListByFileExtName failed  ");
            }
            return null;
        }
    }
    @Transactional(readOnly=true)
    public List<PlatformDocTypeFileExtRelation> getAllPlatformDocTypeFileExtRelation(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAllPlatformDocTypeFileExtRelation started  ");
            }
            List<PlatformDocTypeFileExtRelation> list = platformDocTypeFileExtRelationDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllPlatformDocTypeFileExtRelation failed  ");
            }
            return null;
        }
    }

    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationById(int docTypeFileExtRelationID){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeFileExtRelationById started  ");
            }
            PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation = platformDocTypeFileExtRelationDao.getOneById(docTypeFileExtRelationID);

            return platformDocTypeFileExtRelation;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeFileExtRelationById failed  ");
            }
            return null;
        }
    }

    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationByGuid(String docTypeFileExtRelationGuid){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeFileExtRelationByGuid started  ");
            }
            PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation = platformDocTypeFileExtRelationDao.getOneByGuid(docTypeFileExtRelationGuid);

            return platformDocTypeFileExtRelation;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeFileExtRelationByGuid failed  ");
            }
            return null;
        }
    }

    public PlatformDocTypeFileExtRelation getOnePlatformDocTypeFileExtRelationByForeignKeyGroup(long docTypeId,long platformFileExtID){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeFileExtRelationByForeignKeyGroup started  ");
            }
            PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation = platformDocTypeFileExtRelationDao.getOneByForeignKeysGroup(docTypeId,platformFileExtID).stream().findFirst().get();

            return platformDocTypeFileExtRelation;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOnePlatformDocTypeFileExtRelationByForeignKeyGroup failed  ");
            }
            return null;
        }
    }

    //For AppPlatformDocTypeRelation
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public AppPlatformDocTypeRelation addAppPlatformDocTypeRelation(AppPlatformDocTypeRelation appPlatformDocTypeRelation)
    {
        if(log.isTraceEnabled()) {
            log.trace("AppPlatformDocTypeRelation started for " + appPlatformDocTypeRelation);
        }

        //verify DocType existing
        if(platformDocTypeDao.getOneById(appPlatformDocTypeRelation.getDocTypeId().intValue())==null){
            throw new RuntimeException("The DocType under the relation is not existing currently");
        }
        //verify AppPlatform DocType Relation duplicate
        if(appPlatformDocTypeRelationDao.getOneByForeignKeysGroup(appPlatformDocTypeRelation.getAppId(),appPlatformDocTypeRelation.getDocTypeId()).size()>0){

            throw new RuntimeException("The Relationship between App and platform docType has been existed under current platform");
        }

        if(appPlatformDocTypeRelationDao.insert(appPlatformDocTypeRelation)<=0) {
            throw new RuntimeException("AppPlatformDocTypeRelation failed");
        }
        return appPlatformDocTypeRelation;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public AppPlatformDocTypeRelation updateAppPlatformDocTypeRelation(AppPlatformDocTypeRelation appPlatformDocTypeRelation)
    {
        if(log.isTraceEnabled()) {
            log.trace("updateAppPlatformDocTypeRelation started for " + appPlatformDocTypeRelation);
        }
        //verify DocType existing
        if(platformDocTypeDao.getOneById(appPlatformDocTypeRelation.getDocTypeId().intValue())==null){
            throw new RuntimeException("The DocType under the relation is not existing currently");
        }

        //verify AppPlatform DocType Relation duplicate
        if(appPlatformDocTypeRelationDao.getOneByForeignKeysGroupWhenUpdate(appPlatformDocTypeRelation.getAppId(),appPlatformDocTypeRelation.getDocTypeId(),appPlatformDocTypeRelation.getAppdocTypeRelationGuid().trim()).size()>0)
        {
            throw new RuntimeException("The Relationship between App and platform docType has been existed under current platform");
        }

        if(appPlatformDocTypeRelationDao.update(appPlatformDocTypeRelation)<=0){
            throw new RuntimeException("updateAppPlatformDocTypeRelation failed");
        }
        return appPlatformDocTypeRelation;
    }

    public boolean deleteAppPlatformDocTypeRelation(long appdocTypeRelationID) {
        if(appPlatformDocTypeRelationDao.setToNotActive(appdocTypeRelationID)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recoverAppPlatformDocTypeRelation(long appdocTypeRelationID) {
        if(appPlatformDocTypeRelationDao.setToActive(appdocTypeRelationID)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<AppPlatformDocTypeRelation> getAllAppPlatformDocTypeRelation(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAllAppPlatformDocTypeRelation started  ");
            }
            List<AppPlatformDocTypeRelation> list = appPlatformDocTypeRelationDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllAppPlatformDocTypeRelation failed  ");
            }
            return null;
        }
    }

    public AppPlatformDocTypeRelation getOneAppPlatformDocTypeRelationById(int appdocTypeRelationID){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOneAppPlatformDocTypeRelationById started  ");
            }
            AppPlatformDocTypeRelation appPlatformDocTypeRelation = appPlatformDocTypeRelationDao.getOneById(appdocTypeRelationID);

            return appPlatformDocTypeRelation;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOneAppPlatformDocTypeRelationById failed  ");
            }
            return null;
        }
    }

    //For documentList
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public DocumentList addDocumentList(DocumentList documentList)
    {
        if(log.isTraceEnabled()) {
            log.trace("addDocumentList started for " + documentList);
        }

        //verify fileExt existing
        if(platformFileExtDao.getOneById(documentList.getPlatformfileExtID().intValue())==null){
            throw new RuntimeException("The file extend under the relation is not existing currently");
        }
        //verify DocType existing
        if(platformDocTypeDao.getOneById(documentList.getDocTypeId().intValue())==null){
            throw new RuntimeException("The DocType under the relation is not existing currently");
        }

        //verify documentList duplicated,if duplicated,then rename by hash(uuid & timespan string)
        if(documentListDao.getOneByGroupConditionColumn(documentList.getFilePath(),documentList.getFileName(),documentList.getPlatformfileExtID(),documentList.getDocTypeId(),documentList.getUploadedByPlatformUserGuid().trim()).size()>0){

            String tempFileName= documentList.getFileName();
            Map<String,Object> fileHashFreshNameMap=CommonHelper.getInstance().getFileHashFreshName(tempFileName);
            documentList.setFileName(fileHashFreshNameMap.get("renewFileName")+"."+tempFileName.split("\\.")[1]);
            documentList.setVersion((long)fileHashFreshNameMap.get("renewFileVersion"));
            log.trace("Uploaded file was renamed to:"+documentList.getFileName()+" since name duplicate.");
        }

        if(documentListDao.insert(documentList)<=0) {
            throw new RuntimeException("addDocumentList failed");
        }
        return documentList;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public DocumentList updateDocumentList(DocumentList documentList)
    {
        if(log.isTraceEnabled()) {
            log.trace("updateDocumentList started for " + documentList);
        }
        //verify fileExt existing
        if(platformFileExtDao.getOneById(documentList.getPlatformfileExtID().intValue())==null){
            throw new RuntimeException("The file extend under the relation is not existing currently");
        }
        //verify DocType existing
        if(platformDocTypeDao.getOneById(documentList.getDocTypeId().intValue())==null){
            throw new RuntimeException("The DocType under the relation is not existing currently");
        }

        //verify documentList duplicated,if duplicated,then rename by hash(uuid & timespan string)
        if(documentListDao.getOneByGroupConditionColumnWhenUpdate(documentList.getFilePath(),documentList.getFileName(),documentList.getPlatformfileExtID(),documentList.getDocTypeId(),documentList.getLastModifiedByPlatformUserGuid().trim(),documentList.getDocGuid().trim()).size()>0){
            String tempFileName= documentList.getFileName();
            Map<String,Object> fileHashFreshNameMap=CommonHelper.getInstance().getFileHashFreshName(tempFileName);
            documentList.setFileName(fileHashFreshNameMap.get("renewFileName")+"."+tempFileName.split("\\.")[1]);
            documentList.setVersion((long)fileHashFreshNameMap.get("renewFileVersion"));
            log.trace("Uploaded file was renamed to:"+documentList.getFileName()+" since name duplicate.");
        }

        if(documentListDao.update(documentList)<=0){
            throw new RuntimeException("updateDocumentList failed");
        }
        return documentList;
    }

    public boolean deleteDocumentList(String docGuid) {
        if(documentListDao.setToNotActive(docGuid)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean hardDeleteDocumentList(String filePath,String fileName,long docTypeId,long fileExtID,String fileExtName,String userGuid) {

        if(documentListDao.deleteByGroupConditionColumn(filePath,fileName,docTypeId,fileExtID,fileExtName,userGuid)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recoverDocumentList(String docGuid) {
        if(documentListDao.setToActive(docGuid)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<DocumentList> getAllDocumentList(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAllDocumentList started  ");
            }
            List<DocumentList> list = documentListDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllDocumentList failed  ");
            }
            return null;
        }
    }


    @Transactional(readOnly=true)
    public List<DocumentList> getAllActiveDocumentList(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAllActiveDocumentList started  ");
            }
            List<DocumentList> list = documentListDao.getAllActive();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllActiveDocumentList failed  ");
            }
            return null;
        }
    }


    @Transactional(readOnly=true)
    public List<DocumentList> getAllNotActiveDocumentList(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAllNotActiveDocumentList started  ");
            }
            List<DocumentList> list = documentListDao.getAllNotActive();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAllNotActiveDocumentList failed  ");
            }
            return null;
        }
    }

    public DocumentList getOneDocumentListById(int docId){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOneDocumentListById started  ");
            }
            DocumentList documentList = documentListDao.getOneById(docId);

            return documentList;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOneDocumentListById failed  ");
            }
            return null;
        }
    }

    public DocumentList getOneDocumentListByGuid(String docGuid)
    {
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOneDocumentListByGuid started  ");
            }
            DocumentList documentList = documentListDao.getOneByGuid(docGuid);

            return documentList;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOneDocumentListByGuid failed  ");
            }
            return null;
        }
    }

    //RequestLog
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public RequestLog addRequestLog(RequestLog requestLog)
    {
        if(log.isTraceEnabled()) {
            log.trace("addRequestLog started for " + requestLog);
        }
        //verify app existing in the future

        if(requestLogDao.insert(requestLog)<=0) {
            throw new RuntimeException("addRequestLog failed");
        }
        return requestLog;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public RequestLog updateRequestLog(RequestLog requestLog)
    {
        if(log.isTraceEnabled()) {
            log.trace("updateRequestLog started for " + requestLog);
        }
        //verify app existing
        if(requestLogDao.update(requestLog)<=0){
            throw new RuntimeException("updateRequestLog failed");
        }
        return requestLog;
    }

    public boolean deleteRequestLog(int reqId) {
        if(requestLogDao.setToNotActive(reqId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recoverRequestLog(int reqId) {
        if(requestLogDao.setToActive(reqId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<RequestLog> getRequestLogList(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getRequestLogList started  ");
            }
            List<RequestLog> list = requestLogDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getRequestLogList failed  ");
            }
            return null;
        }
    }

    public RequestLog getRequestLogById(int reqId){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getRequestLogById started  ");
            }
            RequestLog requestLog = requestLogDao.getOneById(reqId);

            return requestLog;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getRequestLogById failed  ");
            }
            return null;
        }
    }

    //operationLog
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public OperationLog addOperationLog(OperationLog operationLog)
    {
        if(log.isTraceEnabled()) {
            log.trace("addOperationLog started for " + operationLog);
        }
        //verify app existing in the future

        if(operationLogDao.insert(operationLog)<=0) {
            throw new RuntimeException("addOperationLog failed");
        }
        return operationLog;
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public OperationLog updateOperationLog(OperationLog operationLog)
    {
        if(log.isTraceEnabled()) {
            log.trace("updateOperationLog started for " + operationLog);
        }
        //verify app existing
        if(operationLogDao.update(operationLog)<=0){
            throw new RuntimeException("updateOperationLog failed");
        }
        return operationLog;
    }

    public boolean deleteOperationLog(int operationLogId){
        if(operationLogDao.setToNotActive(operationLogId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recoverOperationLog(int operationLogId) {
        if(operationLogDao.setToActive(operationLogId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<OperationLog> getOperationLogList(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOperationLogList started  ");
            }
            List<OperationLog> list = operationLogDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOperationLogList failed  ");
            }
            return null;
        }
    }

    public OperationLog getOperationLogById(int operationLogId){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOperationLogById started  ");
            }
            OperationLog operationLog = operationLogDao.getOneById(operationLogId);

            return operationLog;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOperationLogById failed  ");
            }
            return null;
        }
    }

}
