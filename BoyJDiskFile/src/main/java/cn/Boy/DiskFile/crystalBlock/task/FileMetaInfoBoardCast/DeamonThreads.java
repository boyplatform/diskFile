package cn.Boy.DiskFile.crystalBlock.task.FileMetaInfoBoardCast;


import cn.Boy.DiskFile.distributeFileEntry.ICommonFileMetaInfoRecorder;
import cn.Boy.DiskFile.common.DiskFileHttpHelper;
import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Service("crystalBlock.task.FileMetaInfoBoardCast.DeamonThreads")
public class DeamonThreads {
    @Value("${platformArch.CrystalClusterIps}")  String  crystalClusterIps;
    @Value("${server.port}")  String  serverPort;
    @Value("${DiskFile.httpScheme}")  String  httpScheme;

    @Autowired
    @Qualifier("CephFileMetaInfoRecord")
    private ICommonFileMetaInfoRecorder cephFileMetaInfoRecord;

    @Autowired
    private CommonHelper commonHelper;

    private final Log log = LogFactory.getLog(DeamonThreads.class);
    public void boardCastFileMetaInfoToCrystalCluster(){

          if(crystalClusterIps.isEmpty()==false)
          {
              String[] CrystalClusterIps = crystalClusterIps.split(";");

              //boardcast platformDocType
              //Seek all non-boardcast platformDocType via CephFileMetaInfoRecord
              List<PlatformDocType> PlatformDocTypeList = new ArrayList<PlatformDocType>();
              PlatformDocTypeList = cephFileMetaInfoRecord.getAllPlatformDocType().stream().filter(x -> x.getIsBoardCast() == false || (x.getBoardCastCount()!=((long)(CrystalClusterIps.length-1)))).collect(Collectors.toList());

              log.trace("PlatformDocTypeList Boardcast once,count:"+String.valueOf(PlatformDocTypeList.size()));

              if (PlatformDocTypeList.size() > 0) {
                  for (PlatformDocType nonBoardCastPlatformDocType : PlatformDocTypeList) {
                      long successCount = 0;
                      for (String crystalClusterIp : CrystalClusterIps)
                      {

                          if (crystalClusterIp.equals(commonHelper.getSelfIp())==false)
                          {
                                  log.trace("crystalClusterIp:"+crystalClusterIp+"  currentIP:"+commonHelper.getSelfIp());
                                  String postUrl = httpScheme + "://" + crystalClusterIp + ":" + serverPort + "/DiskFile/CephEntry/" + "addDocType";
                                  Map<String, Object> parameterMap = new Hashtable<String, Object>();
                                  parameterMap.put("docTypeName", nonBoardCastPlatformDocType.getDocTypeName());
                                  parameterMap.put("docTypeDesc", nonBoardCastPlatformDocType.getDocTypeDesc());
                                  parameterMap.put("maxFileSize", nonBoardCastPlatformDocType.getMaxFileSize());
                                  parameterMap.put("fileShareFolder", nonBoardCastPlatformDocType.getFileShareFolder());
                                  parameterMap.put("comment", nonBoardCastPlatformDocType.getComment());
                                  parameterMap.put("isActive", nonBoardCastPlatformDocType.getIsActive());
                                  try {
                                      String result = DiskFileHttpHelper.getInstance().postRequest(postUrl, parameterMap, DiskFileHttpHelper.postQuestMode.json.toString());
                                      JSONObject jsonRs = (JSONObject) (new JSONParser().parse(result));
                                      if (((Boolean) jsonRs.get("result")) == true || jsonRs.get("desc").toString().contains("existed") == true) {
                                          successCount++;
                                      }
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }


                          }
                      }

                      log.trace("successCount:"+successCount+" successCount result:"+(successCount == ((long) (CrystalClusterIps.length - 1))));
                      if (successCount == ((long) (CrystalClusterIps.length - 1))) {
                          cephFileMetaInfoRecord.setPlatformDocTypeBoardCastedFlag(nonBoardCastPlatformDocType.getDocTypeId(), successCount);
                      }

                  }
              }
              //boardcast platformFileExt
              //Seek all non-boardcast platformFileExt via CephFileMetaInfoRecord
              List<PlatformFileExt> PlatformFileExtList = new ArrayList<PlatformFileExt>();
              PlatformFileExtList = cephFileMetaInfoRecord.getAllPlatformFileExt().stream().filter(x -> x.getIsBoardCast() == false || (x.getBoardCastCount()!=((long)(CrystalClusterIps.length-1)))).collect(Collectors.toList());

              log.trace("PlatformFileExtList Boardcast once,count:"+String.valueOf(PlatformFileExtList.size()));
              if (PlatformFileExtList.size() > 0) {
                  for (PlatformFileExt nonBoardCastPlatformFileExt : PlatformFileExtList)
                  {
                      long successCount = 0;
                      for (String crystalClusterIp : CrystalClusterIps)
                      {

                          if (crystalClusterIp.equals(commonHelper.getSelfIp())==false)
                          {
                              log.trace("crystalClusterIp:"+crystalClusterIp+"  currentIP:"+commonHelper.getSelfIp());
                              String postUrl = httpScheme + "://" + crystalClusterIp + ":" + serverPort + "/DiskFile/CephEntry/" + "addFileExt";
                              Map<String, Object> parameterMap = new Hashtable<String, Object>();
                              parameterMap.put("fileExtName", nonBoardCastPlatformFileExt.getFileExtName());

                              try {
                                  String result = DiskFileHttpHelper.getInstance().postRequest(postUrl, parameterMap, DiskFileHttpHelper.postQuestMode.json.toString());
                                  JSONObject jsonRs = (JSONObject) (new JSONParser().parse(result));
                                  if (((Boolean) jsonRs.get("result")) == true || jsonRs.get("desc").toString().contains("existed") == true) {
                                      successCount++;
                                  }
                              } catch (Exception e) {
                                  e.printStackTrace();
                              }


                          }
                      }

                      log.trace("successCount:"+successCount+" successCount result:"+(successCount == ((long) (CrystalClusterIps.length - 1))));
                      if (successCount == ((long) (CrystalClusterIps.length - 1))) {
                          cephFileMetaInfoRecord.setPlatformFileExtBoardCastedFlag(nonBoardCastPlatformFileExt.getFileExtID(), successCount);
                      }

                  }
              }
              //boardcast platformDocTypeFileExtRelation
              //Seek all non-boardcast platformDocTypeFileExtRelation via CephFileMetaInfoRecord
              List<PlatformDocTypeFileExtRelation> PlatformDocTypeFileExtRelationList = new ArrayList<PlatformDocTypeFileExtRelation>();
              PlatformDocTypeFileExtRelationList = cephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation().stream().filter(x -> x.getIsBoardCast() == false || (x.getBoardCastCount()!=((long)(CrystalClusterIps.length-1)))).collect(Collectors.toList());

              log.trace("PlatformDocTypeFileExtRelationList Boardcast once,count:"+String.valueOf(PlatformDocTypeFileExtRelationList.size()));
              if (PlatformDocTypeFileExtRelationList.size() > 0) {
                  for (PlatformDocTypeFileExtRelation nonBoardCastPlatformDocTypeFileExtRelation : PlatformDocTypeFileExtRelationList)
                  {
                      long successCount = 0;
                      for (String crystalClusterIp : CrystalClusterIps)
                      {

                          if (crystalClusterIp.equals(commonHelper.getSelfIp())==false)
                          {
                              log.trace("crystalClusterIp:"+crystalClusterIp+"  currentIP:"+commonHelper.getSelfIp());

                              String postUrl = httpScheme + "://" + crystalClusterIp + ":" + serverPort + "/DiskFile/CephEntry/" + "addDocTypeFileExtRelation";
                              Map<String, Object> parameterMap = new Hashtable<String, Object>();
                              parameterMap.put("docTypeId", nonBoardCastPlatformDocTypeFileExtRelation.getDocTypeId());
                              parameterMap.put("fileExtID", nonBoardCastPlatformDocTypeFileExtRelation.getPlatformFileExtID());

                              try {
                                  String result = DiskFileHttpHelper.getInstance().postRequest(postUrl, parameterMap, DiskFileHttpHelper.postQuestMode.textBody.toString());
                                  JSONObject jsonRs = (JSONObject) (new JSONParser().parse(result));
                                  if (((Boolean) jsonRs.get("result")) == true || jsonRs.get("desc").toString().contains("existed") == true) {
                                      successCount++;
                                  }
                              } catch (Exception e) {
                                  e.printStackTrace();
                              }
                          }
                      }

                      log.trace("successCount:"+successCount+" successCount result:"+(successCount == ((long) (CrystalClusterIps.length - 1))));
                      if (successCount == ((long) (CrystalClusterIps.length - 1))) {
                          cephFileMetaInfoRecord.setPlatformDocTypeFileExtRelationBoardCastedFlag(nonBoardCastPlatformDocTypeFileExtRelation.getDocTypeFileExtRelationID(), successCount);
                      }

                  }
              }
          }
    }
}
