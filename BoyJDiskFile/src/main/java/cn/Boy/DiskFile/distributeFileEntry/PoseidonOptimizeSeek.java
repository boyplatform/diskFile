package cn.Boy.DiskFile.distributeFileEntry;

import cn.Boy.DiskFile.dao.DocumentListDao;
import cn.Boy.DiskFile.pojo.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("PoseidonOptimizeSeek")
public class PoseidonOptimizeSeek {

    @Autowired
    @Qualifier("CephFileMetaInfoRecord")
    private ICommonFileMetaInfoRecorder cephFileMetaInfoRecord;

    @Autowired
    private DocumentListDao documentListDao;


    private final Log log = LogFactory.getLog(PoseidonOptimizeSeek.class);

    //seek
    //used for *Sync_checkAndSave
    public List<DocumentList> seek(String userGuid, String filePath, String fileName, long docTypeId, long fileExtID){

        List<DocumentList> resultLs=new ArrayList<DocumentList>();
        //Seek all active documents via CephFileMetaInfoRecord
         resultLs=cephFileMetaInfoRecord.getAllActiveDocumentList().stream().filter(x->(x.getUploadedByPlatformUserGuid().trim().equals(userGuid)||x.getLastModifiedByPlatformUserGuid().trim().equals(userGuid)) && x.getFilePath().equals(filePath) &&x.getFileName().equals(fileName) &&x.getDocTypeId().equals(docTypeId) && x.getPlatformfileExtID().equals(fileExtID)).collect(Collectors.toList());
        //filter it out by input condition
           //if existed under active result,then update its poseidon parameter and return back the result.
         if(resultLs.size()>0&&resultLs.size()==1)
         {
               DocumentList dcList=resultLs.get(0);
               dcList.setMemoryHitTimes(dcList.getMemoryHitTimes()+1); //add up storage doc live time base on existing Poseidong calculation parameter.
               cephFileMetaInfoRecord.updateDocumentList(dcList);
               return  resultLs;

         }//if not existed under active result,then seek it from non-active result via CephFileMetaInfoRecord by input condition
         else if(resultLs.size()<=0){

             resultLs=  documentListDao.getOneNotActiveByGroupConditionColumn(filePath,fileName,fileExtID,docTypeId,userGuid);
             if(resultLs.size()>0&&resultLs.size()==1) {
                 //if existed under non-active result,then update its poseidon parameter& make it be active and return back the result.
                 DocumentList dcList = documentListDao.getOneNotActiveByGroupConditionColumn(filePath, fileName, fileExtID, docTypeId, userGuid).get(0);
                 dcList.setMemoryHitTimes(dcList.getMemoryHitTimes()+1); //add up storage doc live time base on existing Poseidong calculation parameter.
                 dcList.setMemorySeaLevelTime(new Date());//set new memorySeaLevelTime to current dateTime
                 dcList.setIsActive(true); //update active status into true.
                 cephFileMetaInfoRecord.updateDocumentList(dcList);
             }else if(resultLs.size()<=0){
                 //if not existed under non-active result, then return empty list to let upstream invoker know empty.
                 return resultLs;
             }

         }
        return resultLs;
    }

    //used for ls api
    public List<DocumentList> seek(String filePath,String fileName){

        List<DocumentList> resultLs=new ArrayList<DocumentList>();
        //Seek all active documents via CephFileMetaInfoRecord
        List<DocumentList> dcAllList=cephFileMetaInfoRecord.getAllActiveDocumentList();
        //filter it out by input condition-if fileName=*, then just filter by filePath
         if(fileName.trim().equals("*")){

               resultLs=dcAllList.stream().filter(x->x.getFilePath().equals(filePath)).collect(Collectors.toList());

         }else {
               resultLs=dcAllList.stream().filter(x->x.getFilePath().equals(filePath) && x.getFileName().equals(fileName)).collect(Collectors.toList());
         }
        //if existed under active result,then update its poseidon parameter and return back the result.
        if(resultLs.size()>0)
        {
            for(DocumentList dcLs: resultLs){
                dcLs.setMemoryHitTimes(dcLs.getMemoryHitTimes()+1); //add up storage doc live time base on existing Poseidong calculation parameter.
                cephFileMetaInfoRecord.updateDocumentList(dcLs);
            }
            return resultLs;
        }else {
            //if not existed under active result,then seek it from non-active result via CephFileMetaInfoRecord by input condition
                dcAllList=cephFileMetaInfoRecord.getAllNotActiveDocumentList();

                if(fileName.trim().equals("*")){
                    resultLs=dcAllList.stream().filter(x->x.getFilePath().equals(filePath)).collect(Collectors.toList());
                }else {
                    resultLs=dcAllList.stream().filter(x->x.getFilePath().equals(filePath) && x.getFileName().equals(fileName)).collect(Collectors.toList());
                }

               if(resultLs.size()>0) {
                   //if existed under non-active result,then update its poseidon parameter& make it be active and return back the result.
                   for(DocumentList dcLs: resultLs){
                       dcLs.setMemoryHitTimes(dcLs.getMemoryHitTimes()+1); //add up storage doc live time base on existing Poseidong calculation parameter.
                       dcLs.setMemorySeaLevelTime(new Date());//set new memorySeaLevelTime to current dateTime
                       dcLs.setIsActive(true);
                       cephFileMetaInfoRecord.updateDocumentList(dcLs);
                   }
                   return resultLs;

               }else {
                   //if not existed under non-active result, then return empty list to let upstream inoker know empty.
                   return resultLs;
               }
        }
    }
    //scanUpdate
    public void scanUpdate(){

        //seek all active documents via CephFileMetaInfoRecord
        List<DocumentList> dcList=cephFileMetaInfoRecord.getAllActiveDocumentList();
        //verify each one base on its Poseidon parameter
        if(dcList.size()>0){
            Date now=new Date();
            for(DocumentList dc:dcList){
                Date compareDeadLineTime=new Date(dc.getMemorySeaLevelTime().getTime()+(dc.getMemoryLiveTimeSec()+dc.getMemoryPerHitComeUpSeconds()*dc.getMemoryHitTimes())*1000);//get Poseidong dead line base on Poseidong setting
                if(now.after(compareDeadLineTime)){ //if expired the setting deadline,then update the one into non-active status timely.
                    dc.setIsActive(false);
                    dc.setMemoryHitTimes(0l);//memoryHitTimes update to 0
                    cephFileMetaInfoRecord.updateDocumentList(dc);
                    log.trace("One file named as "+dc.getFileName()+" was set to non-active by Poseidong calculation.");
                }

            }
        }

    }

}
