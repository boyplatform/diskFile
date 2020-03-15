package cn.Boy.DiskFile.controller;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonEnums;
import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.common.DiskFileHttpHelper;
import cn.Boy.DiskFile.distributeFileEntry.*;
import cn.Boy.DiskFile.dto.CephFsConfigInfo;

import cn.Boy.DiskFile.pojo.DocumentList;
import cn.Boy.DiskFile.pojo.PlatformDocType;
import cn.Boy.DiskFile.pojo.PlatformDocTypeFileExtRelation;
import cn.Boy.DiskFile.pojo.PlatformFileExt;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanProperty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.ceph.fs.CephStat;


/**
 * RestController的一个例子，展示了各种基本操作在RestController的实现方式。
 *
 */
@RestController
@RequestMapping("/DiskFile/CephEntry")
public class CephInterfaceTestController {

    private final Log log = LogFactory.getLog(CephInterfaceTestController.class);

    @Value("${spring.mvc.static-path-pattern:/nodeFile/}") String staticFilePath;
    @Value("${DiskFile.streamFileCacheFolder:target/uploadFileCache}") String streamFileCacheFolder;
    @Value("${DiskFile.viewerCacheFileFolder:target/uploadFileCache/viewerCache}") String viewerCacheFileFolder;
    @Value("${platformArch.ViewerFileCache.defaultViewerCacheLength}")  String  defaultViewerCacheLength;
    @Value("${platformArch.ViewerFileCache.ViewerFileCachePullRate}")  long  viewerFileCachePullRate;
    @Value("${platformArch.CrystalClusterIps}")  String  crystalClusterIps;

    @Autowired
    @Qualifier("CephFileOperator")
    private AbsCommonFileOperator cephFileOperator;

    @Autowired
    @Qualifier("CephFileNodeMetaInfoSync")
    private IFileNodeMetaInfoRecorderSync cephFileNodeMetaInfoSync;

    @Autowired
    @Qualifier("CephFileMetaInfoRecord")
    private ICommonFileMetaInfoRecorder cephFileMetaInfoRecord;

    @Autowired
    @Qualifier("CephClusterTalk")
    private AbsCommonClusterTalker cephClusterTalk;

    @Autowired
    @Qualifier("CephViewerFileCacheManager")
    private IViewerFileCacheManager cephViewerFileCacheManager;

    @Autowired
    private CommonHelper commonHelper;

    //ceph 配置注入(inject ceph config)
    @RequestMapping(value = "/mount",method = RequestMethod.POST)
    public  boolean mountCephFsCommonRoot(@RequestBody CephFsConfigInfo cephFsConfigInfo){

        //log.trace("received Info:"+cephFsConfigInfo.getUsername()+"|"+cephFsConfigInfo.getMonIp()+"|"+cephFsConfigInfo.getUserKey()+"|"+cephFsConfigInfo.getMountPath());
        String username = cephFsConfigInfo.getUsername()==""? "admin":cephFsConfigInfo.getUsername();
        String monIp = cephFsConfigInfo.getMonIp()==""? "192.168.125.128:6789;192.168.125.129:6789;192.168.125.130:6789":cephFsConfigInfo.getMonIp();
        String userKey = cephFsConfigInfo.getUserKey()==""? "AQDyuF1dpxsnExAAAIkIlT3m7gcuhgvW+aPmiw==":cephFsConfigInfo.getUserKey();
        String mountPath = cephFsConfigInfo.getMountPath()==""? "/":cephFsConfigInfo.getMountPath();

        return  cephFileOperator.mount(monIp,username,userKey,mountPath);
    }

    //查看目录列表 (check ceph dir)
    @RequestMapping(value = "/listdir",method = RequestMethod.POST)
    @ResponseBody
    public String[] listDirByGivenPath(@RequestParam(value = "dirPath") String path){

        log.trace("received Info:"+path);
        try{
           return  cephFileOperator.listDir(path);
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //新建目录 (create new ceph dir)
    @RequestMapping(value = "/mkdir",method = RequestMethod.POST)
    @ResponseBody
    public String[] mkDirByGivenPath(@RequestParam(value = "dirPath") String path){

        try{
            return  cephFileOperator.mkDir(path);
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //删除目录 (delete ceph dir)
    @RequestMapping(value = "/deldir",method = RequestMethod.POST)
    @ResponseBody
    public String[] delDirByGivenPath(@RequestParam(value = "dirPath") String path){

        try{
            return  cephFileOperator.delDir(path);
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //获取文件的状态(Get file status)
    @RequestMapping(value = "/stat",method = RequestMethod.POST)
    @ResponseBody
    public CephStat getStatByGivenPath(@RequestParam(value = "dirPath") String path){

        try{
            return  (CephStat)cephFileOperator.getFileStatByPath(path);
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //重命名目录or文件 (rename ceph dir or file)
    @RequestMapping(value = "/rename",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Map<String, Object>  rename(@RequestParam(value = "userGuid") String userGuid,@RequestParam(value = "oldFileName") String oldFileName,@RequestParam(value = "newFileName") String newFileName,@RequestParam(value = "platformfileExtID") long fileExtID,@RequestParam(value = "docTypeId") long docTypeId){
        try
        {
            ThreadLocalContext.setDbKey("main");
            Map<String, Object> result = new HashMap<>();
            Map<String,Object> renameLazySyncResult=new Hashtable<String,Object>();
            //创建当前的userFileFolderStr
            String userFileFolderStr=streamFileCacheFolder+File.separatorChar+userGuid;
            renameLazySyncResult=cephFileNodeMetaInfoSync.renameLazySync_checkAndSave(userGuid,userFileFolderStr,oldFileName,newFileName,docTypeId,fileExtID);
            if(renameLazySyncResult.containsKey("result") && renameLazySyncResult.containsKey("desc")&&renameLazySyncResult.containsKey("dirList")
               && renameLazySyncResult.get("result").equals(true))
            {
                result.put("result",true);
                result.put("desc",renameLazySyncResult.get("desc"));
                result.put("dirList",renameLazySyncResult.get("dirList"));
            }else {
                result.put("result",false);
                result.put("desc",renameLazySyncResult.get("desc"));
            }

            return result;
        }
        catch (Exception e)
        {
            log.error(e.getStackTrace());
            return  null;
        }
    }

    //删除文件 (delete file)
    @RequestMapping(value = "/rm",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Map<String, Object> removeFile(@RequestParam(value = "userGuid") String userGuid,@RequestParam(value = "fileName") String fileName,@RequestParam(value = "platformfileExtID") long fileExtID,@RequestParam(value = "docTypeId") long docTypeId){
        try
        {
            ThreadLocalContext.setDbKey("main");
            Map<String, Object> result = new HashMap<>();
            Map<String,Object> deleteLazySyncResult=new Hashtable<String,Object>();
            //创建当前的userFileFolderStr
            String userFileFolderStr=streamFileCacheFolder+File.separatorChar+userGuid;
            deleteLazySyncResult=cephFileNodeMetaInfoSync.deleteLazySync_checkAndSave(userGuid,userFileFolderStr,fileName,docTypeId,fileExtID);
            if(deleteLazySyncResult.containsKey("result")&& deleteLazySyncResult.containsKey("desc")&&deleteLazySyncResult.containsKey("delFileDirList")
                    &&deleteLazySyncResult.get("result").equals(true)){

                 result.put("result",true);
                 result.put("delFileDirList",deleteLazySyncResult.get("delFileDirList"));
                 result.put("desc",deleteLazySyncResult.get("desc"));

            }else{

                result.put("result",false);
                result.put("desc",deleteLazySyncResult.get("desc"));

            }

            return result;
        }
        catch (Exception e)
        {

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //读目录文件列表 (seek file list by path for the user)
    @RequestMapping(value = "/ls",method = RequestMethod.POST)
    @ResponseBody
    public String[] lsFiles(@RequestParam(value = "userGuid") String userGuid){
        try
        {
            String userFileFolderStr=streamFileCacheFolder+File.separatorChar+userGuid.trim();
            log.trace(userFileFolderStr);
            return  cephFileOperator.listDir(userFileFolderStr);
        }
        catch (Exception e)
        {

            log.trace("ls error:"+e.toString());
            return  null;
        }
    }

    //复制文件 (copy file)
    @RequestMapping(value = "/cp",method = RequestMethod.POST)
    @ResponseBody
    public boolean copyFile(@RequestParam(value = "sourceFile") String sourceFilePath,@RequestParam(value = "targetFile") String targetFilePath){
        try
        {
            cephFileOperator.copyFile(sourceFilePath,targetFilePath);
            return  true;
        }
        catch (Exception e)
        {
            log.error(e.getStackTrace());
            return  false;
        }
    }

    //写入文件 (write file)
    @PostMapping(value = "/vim",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Object> vimWriteFile(@RequestParam(value = "userGuid") String userGuid,@RequestParam(value = "platformfileExtID") long fileExtID,@RequestParam(value = "docTypeId") long docTypeId,@RequestParam(value = "isBig") boolean isBig,@RequestParam(value = "inputBufferFiles")  MultipartFile[] inputBufferFiles)
    {
        ThreadLocalContext.setDbKey("main");
        if(log.isTraceEnabled()) {
            log.trace("platform current received file number is:" + (inputBufferFiles == null ? 0 : inputBufferFiles.length));
        }

        Map<String, Object> result = new HashMap<>();
        Map<String,Object> uploadLazySyncResult=new Hashtable<String,Object>();
        List<String> files = new ArrayList<>();
        try
        {
            //创建当前的userFileFolderStr
            String userFileFolderStr=streamFileCacheFolder+File.separatorChar+userGuid;

            //遍历接收到的文件流
            for(MultipartFile inputBufferFile : inputBufferFiles)
            {
                String fileName = inputBufferFile.getOriginalFilename();
                uploadLazySyncResult= cephFileNodeMetaInfoSync.uploadLazySync_checkAndSave(userGuid,userFileFolderStr,fileName,docTypeId,fileExtID,inputBufferFile,isBig);

                if(uploadLazySyncResult.get("isGoOn").equals(true)
                        &&uploadLazySyncResult.containsKey("File")
                        &&uploadLazySyncResult.containsKey("DocumentList")) {

                    FileInputStream fis = new FileInputStream((File) uploadLazySyncResult.get("File"));
                    log.trace("Begin push to ceph cluster.");
                    //获取当前文件的流信息传给ceph处理类(base on fixed freshest filename)
                    if (cephFileOperator.writeToCluster(userFileFolderStr, ((DocumentList) uploadLazySyncResult.get("DocumentList")).getFileName(), fis, ((File) uploadLazySyncResult.get("File")).length())) {
                        //更新数据库中文件的传输处理状态-1
                        DocumentList dcList=(DocumentList) uploadLazySyncResult.get("DocumentList");
                        //dcList.setClusterStorageStatus(CommonEnums.ClusterStorageStatus.saved.getClusterStorageStatus());
                        //cephFileMetaInfoRecord.updateDocumentList(dcList);
                        cephFileNodeMetaInfoSync.uploadLazySync_StatusUpdate(dcList.getDocGuid(),CommonEnums.ClusterStorageStatus.saved.getClusterStorageStatus());
                        files.add(((DocumentList) uploadLazySyncResult.get("DocumentList")).getFileName());//put proccessed fileName into result (base on fixed freshest filename)
                        result.put("desc","Your vim uploaded file was saved on boy platform diskFile cluster successfully!");
                    } else {
                        //更新数据库中文件的传输处理状态-2
                        DocumentList dcList=(DocumentList) uploadLazySyncResult.get("DocumentList");
                        //dcList.setClusterStorageStatus(CommonEnums.ClusterStorageStatus.pendingSaved.getClusterStorageStatus());
                        //cephFileMetaInfoRecord.updateDocumentList(dcList);
                        cephFileNodeMetaInfoSync.uploadLazySync_StatusUpdate(dcList.getDocGuid(),CommonEnums.ClusterStorageStatus.pendingSaved.getClusterStorageStatus());
                        result.put("writtenFiles", files);
                        result.put("result", false);
                        result.put("desc", "Your vim uploaded file was cached on one node successfully,but failed to be written into Ceph cluster,Please try again later!");
                        return result;
                    }
                }else {
                    result.put("writtenFiles", files);
                    result.put("result", false);
                    result.put("desc", "Your uploaded files was failed during lazy sync because of--"+uploadLazySyncResult.get("desc"));
                    return result;
                }
            }

            result.put("writtenFiles",files);
            result.put("result",true);
            return  result;
        }
        catch (Exception e)
        {
            log.trace(e.getStackTrace()+e.getMessage()+e.getCause());
            result.put("writtenFiles",files);
            result.put("result",false);
            result.put("desc", uploadLazySyncResult.get("desc"));
            return  result;

        }finally {
            ThreadLocalContext.setDbKey(null);
        }
    }


    //Verify dir or file under current path--返回指定目录/文件路径的状态
    @RequestMapping(value = "/statDirOrFile",method = RequestMethod.POST)
    @ResponseBody
    public List<String> getDirOrFileByGivenPath(@RequestParam(value = "dirOrFilePath") String path){

        try{
            return cephFileOperator.listFileOrDir(path);
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //Verify dir or file for current path--判断指定路径为目录还是文件
    @RequestMapping(value = "/isDirOrFile",method = RequestMethod.POST)
    @ResponseBody
    public String verifyIsDirOrFileByGivenPath(@RequestParam(value = "dirOrFilePath") String path){

        try{
            return cephFileOperator.fileOrDir(path);
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  null;
        }
    }

    //set current dir (work dir)--移动到指定的工作目录
    @RequestMapping(value = "/cd",method = RequestMethod.POST)
    @ResponseBody
    public boolean moveToGivenWorkDirPath(@RequestParam(value = "dirPath") String path)
    {

        try{
              cephFileOperator.setWorkDir(path);
              return  true;
        }
        catch (Exception e){

            log.error(e.getStackTrace());
            return  false;
        }
    }

   //umount--取消mount挂载
    @RequestMapping(value = "/umount",method = RequestMethod.POST)
    @ResponseBody
    public boolean umountCephFsCommonRoot(){
        try
        {
            cephFileOperator.umount();
            return  true;
        }
        catch (Exception e)
        {
            log.error(e.getStackTrace());
            return  false;
        }
    }



     //预览静态资源/下载文件(cat by download/ cat by getStaticUrl)
     @RequestMapping(value = "/cat",method = {RequestMethod.POST,RequestMethod.GET})
     public Map<String, Object> catFile(@RequestParam(value = "userGuid") String userGuid, @RequestParam(value = "platformfileExtID") long fileExtID,@RequestParam(value = "docTypeId") long docTypeId,@RequestParam(value = "fileName") String fileName,  @RequestParam(value = "catType") String type,  @RequestParam(value = "httpType",required = false,defaultValue = "http") String httpType,@RequestParam(value = "isBig") boolean isBig,@RequestParam(value = "viewerCacheLength",required = false,defaultValue = "1") long viewerCacheTimeLength){

         //log.trace(userGuid+"|"+fileExtID+"|"+docTypeId+"|"+fileName+"|"+type+"|"+httpType);
         //get viewerCacheTimeLength from config at first
         if(viewerCacheTimeLength==1){
             viewerCacheTimeLength=Long.parseLong(defaultViewerCacheLength);
         }
         ThreadLocalContext.setDbKey("main");
         //创建当前的userFileFolderStr
         String userFileFolderStr=streamFileCacheFolder+File.separatorChar+userGuid;

         Map<String,Object> result=new Hashtable<String,Object>();

         HttpServletRequest request= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
         HttpServletResponse response=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

         log.trace("point1");
         //Invoke the downloadLazySync_checkAndSave.
         Map<String, Object>  downloadLazySyncResult=cephFileNodeMetaInfoSync.downloadLazySync_checkAndSave(userGuid,userFileFolderStr,fileName,docTypeId,fileExtID);

         //If downloadLazySync_checkAndSave was return back go on processing, then invoke ceph cluster download via CephFileOperator;
          if(downloadLazySyncResult.get("isGoOn").equals(true)&&downloadLazySyncResult.containsKey("File")==true){
              try {
                  //If cat type is seekCached, then if the file was existed under local cached folder,return it directly to end user.
                   if(type.equals("seekCached"))
                   {
                       log.trace("point2");
                        if(((File)downloadLazySyncResult.get("File")).exists()
                            &&((File)downloadLazySyncResult.get("File")).length()==((CephStat)cephFileOperator.getFileStatByPath(userFileFolderStr+File.separatorChar+fileName)).size
                        ){

                            response.setHeader("content-type","application/octet-stream");
                            response.setContentType("application/octet-stream");
                            //make it can be chinese file name
                            response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName,"UTF-8"));

                            //implement download
                            byte[] buffer=new byte[1024];
                            FileInputStream fis=null;
                            BufferedInputStream bis=null;

                            try{
                                fis= new FileInputStream(((File)downloadLazySyncResult.get("File")));
                                bis= new BufferedInputStream(fis);
                                OutputStream os=response.getOutputStream();
                                int i=bis.read(buffer);
                                while (i!=-1){
                                    os.write(buffer,0,i);
                                    i=bis.read(buffer);
                                }
                                log.trace("cat download file successfully!");
                            }catch (Exception e){

                                log.trace("cat download file failed!");
                            }finally {
                                if(bis!=null){
                                    try{
                                        bis.close();
                                    }catch (IOException e){
                                        log.trace(e.getMessage());
                                    }
                                }
                                if(fis!=null){
                                    try{
                                        fis.close();
                                    }catch (IOException e){
                                        log.trace(e.getMessage());
                                    }
                                }
                                ThreadLocalContext.setDbKey(null);
                                return null;
                            }

                        }else {
                            //if there is no cached file on current node,then shift the type into download and go on processing
                            type="download";
                        }
                   }

                   if(cephFileOperator.seekFromCluster(userFileFolderStr, fileName, new FileOutputStream(((File) downloadLazySyncResult.get("File"))))){

                       //then If user request download,write the local cached file stream to the response;
                        if(type.equals("download"))
                        {
                            log.trace("point3");
                            response.setHeader("content-type","application/octet-stream");
                            response.setContentType("application/octet-stream");
                            //make it can be chinese file name
                            response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName,"UTF-8"));

                            //implement download
                            byte[] buffer=new byte[1024];
                            FileInputStream fis=null;
                            BufferedInputStream bis=null;

                            try{
                                fis= new FileInputStream(new File(userFileFolderStr,fileName));
                                bis= new BufferedInputStream(fis);
                                OutputStream os=response.getOutputStream();
                                int i=bis.read(buffer);
                                while (i!=-1){
                                    os.write(buffer,0,i);
                                    i=bis.read(buffer);
                                }
                                log.trace("cat download file successfully!");
                            }catch (Exception e){

                                log.trace("cat download file failed!");
                            }finally {
                                   if(bis!=null){
                                       try{
                                           bis.close();
                                       }catch (IOException e){
                                           log.trace(e.getMessage());
                                       }
                                   }
                                   if(fis!=null){
                                       try{
                                           fis.close();
                                       }catch (IOException e){
                                           log.trace(e.getMessage());
                                       }
                                   }
                                   ThreadLocalContext.setDbKey(null);
                                   return null;
                            }

                        }else if(type.equals("getStaticUrl")) {
                            log.trace("point4");
                            //verify whether request file has viewer-cache under nodeDb,if existed,take viewerCacheFileName from it,return response per it.
                            String viewerCacheFileName=cephViewerFileCacheManager.getViewerFileCache((DocumentList)downloadLazySyncResult.get("DocumentList"),viewerCacheTimeLength);

                            if(viewerCacheFileName=="") {
                                //if no viewer-cache,copy the user cat file into interact file folder with sha256(filename+userGuid+timeSpan)+fileExtsion to view catche folder
                                //String tempFileName = fileName + userGuid + CommonHelper.getInstance().getNodeTimeSpanString();
                                //viewerCacheFileName = CommonHelper.getInstance().getSHA256Str(tempFileName).substring(CommonHelper.getInstance().getNodeRandomNum(1, 10), CommonHelper.getInstance().getNodeRandomNum(10, CommonHelper.getInstance().getSHA256Str(tempFileName).length())) + "." + fileName.split("\\.")[1];
                                viewerCacheFileName=commonHelper.getHashViewerCacheFileName(userFileFolderStr,fileName,userGuid);
                                File cachedFile = new File(userFileFolderStr, fileName);
                                File viewerCacheFile = new File(viewerCacheFileFolder, viewerCacheFileName);

                                if (isBig) {
                                    IOUtils.copyLarge(new FileInputStream(cachedFile), new FileOutputStream(viewerCacheFile));
                                } else {
                                    IOUtils.copy(new FileInputStream(cachedFile), new FileOutputStream(viewerCacheFile));
                                }

                                //set viewer-cache to the document on nodeDb.
                                cephViewerFileCacheManager.setViewerFileCache((DocumentList)downloadLazySyncResult.get("DocumentList"),viewerCacheFileName,new Date(),viewerCacheTimeLength,viewerCacheFile);
                                Thread.sleep((viewerFileCachePullRate-commonHelper.getNodeRandomNum(1,Integer.parseInt(Long.toString(viewerFileCachePullRate-1))))*1000);
                            }

                            result.put("result",true);
                            result.put("resourceUrl",request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+staticFilePath.replace("*","")+viewerCacheFileName);
                            /*result.put("resourceUrl",request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/nodeFileTemp/"+viewerCacheFileName+"?fextId="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getPlatformfileExtID()
                                      +"&docTid="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getDocTypeId()
                                      +"&fName="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getFileName()
                                      +"&fPath="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getFilePath()
                                      +"&V="+(((DocumentList) downloadLazySyncResult.get("DocumentList")).getVersion()==null? "0":((DocumentList) downloadLazySyncResult.get("DocumentList")).getVersion())
                                      +"&mslt="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemorySeaLevelTime()
                                      +"&mlts="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemoryLiveTimeSec()
                                      +"&mht="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemoryHitTimes()
                                      +"&mphcus="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemoryPerHitComeUpSeconds()
                                      +"&sct="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getStorageClusterType()
                                      +"&ugid="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getUploadedByPlatformUserGuid()
                                      +"&clss="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getClusterStorageStatus()
                                      +"&vctl="+String.valueOf(viewerCacheTimeLength));*/
                            result.put("desc","cat the resource Url successfully.");

                            return result;

                        }else if(type.equals("redirectToStaticUrl")){
                            //verify whether request file has viewer-cache under nodeDb,if existed,take viewerCacheFileName from it,return response per it.
                            String viewerCacheFileName=cephViewerFileCacheManager.getViewerFileCache((DocumentList)downloadLazySyncResult.get("DocumentList"),viewerCacheTimeLength);

                            if(viewerCacheFileName=="") {
                                //if no viewer-cache,copy the user cat file into interact file folder with sha256(filename+userGuid+timeSpan)+fileExtsion to view catche folder
                                //String tempFileName = fileName + userGuid + CommonHelper.getInstance().getNodeTimeSpanString();
                                //viewerCacheFileName = CommonHelper.getInstance().getSHA256Str(tempFileName).substring(CommonHelper.getInstance().getNodeRandomNum(1, 10), CommonHelper.getInstance().getNodeRandomNum(10, CommonHelper.getInstance().getSHA256Str(tempFileName).length())) + "." + fileName.split("\\.")[1];
                                viewerCacheFileName=commonHelper.getHashViewerCacheFileName(userFileFolderStr,fileName,userGuid);
                                File cachedFile = new File(userFileFolderStr, fileName);
                                File viewerCacheFile = new File(viewerCacheFileFolder, viewerCacheFileName);

                                if (isBig) {
                                    IOUtils.copyLarge(new FileInputStream(cachedFile), new FileOutputStream(viewerCacheFile));
                                } else {
                                    IOUtils.copy(new FileInputStream(cachedFile), new FileOutputStream(viewerCacheFile));
                                }
                                //set viewer-cache to the document on nodeDb.
                                cephViewerFileCacheManager.setViewerFileCache((DocumentList)downloadLazySyncResult.get("DocumentList"),viewerCacheFileName,new Date(),viewerCacheTimeLength,viewerCacheFile);
                                Thread.sleep(viewerFileCachePullRate*1000);
                            }

                            response.sendRedirect(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+staticFilePath.replace("*","")+viewerCacheFileName);
                            /*response.sendRedirect(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/nodeFileTemp/"+viewerCacheFileName+"?fextId="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getPlatformfileExtID()
                                    +"&docTid="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getDocTypeId()
                                    +"&fName="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getFileName()
                                    +"&fPath="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getFilePath()
                                    +"&V="+(((DocumentList) downloadLazySyncResult.get("DocumentList")).getVersion()==null? "0":((DocumentList) downloadLazySyncResult.get("DocumentList")).getVersion())
                                    +"&mslt="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemorySeaLevelTime()
                                    +"&mlts="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemoryLiveTimeSec()
                                    +"&mht="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemoryHitTimes()
                                    +"&mphcus="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getMemoryPerHitComeUpSeconds()
                                    +"&sct="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getStorageClusterType()
                                    +"&ugid="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getUploadedByPlatformUserGuid()
                                    +"&clss="+((DocumentList) downloadLazySyncResult.get("DocumentList")).getClusterStorageStatus()
                                    +"&vctl="+String.valueOf(viewerCacheTimeLength));*/
                            return null;
                        }
                        else {

                            result.put("result",false);
                            result.put("desc","cat file from boy platform failed.");
                            return result;
                        }

                   }else {

                       result.put("result",false);
                       result.put("desc","cat file from boy platform failed.");
                       return result;
                   }


              }
              catch (InterruptedException e){
                  log.trace(e.getMessage());
                  result.put("result",false);
                  result.put("desc","cat file from boy platform failed caused by an interruptedException.");
                  return result;
              }
              catch (IOException e){
                  log.trace(e.getMessage());
                  result.put("result",false);
                  result.put("desc","cat file from boy platform failed.");
                  return result;

              }finally {
                  ThreadLocalContext.setDbKey(null);
              }
          }else {
              result.put("result",false);
              result.put("desc","cat file from boy platform failed.");
              ThreadLocalContext.setDbKey(null);
              return result;
          }
     }


     /*
        Below is the platform meta data config API |:)<|

    */

    //创建平台文件类型
    @RequestMapping(value = "/addDocType",method =RequestMethod.POST)
    public Map<String,Object> addDocType(@RequestBody PlatformDocType platformDocType){

            Map<String,Object> result=new Hashtable<String,Object>();

            //doctype name can not be null
           if(platformDocType.getDocTypeName()==null||platformDocType.getDocTypeName().isEmpty())
           {
               result.put("result",false);
               result.put("desc","Platform doctype name can not be empty or null.");
               return  result;
           }

           //verify duplicate name
            ThreadLocalContext.setDbKey("main");
           if(cephFileMetaInfoRecord.getOnePlatformDocTypeByName(platformDocType.getDocTypeName())!=null){

               result.put("result",false);
               result.put("desc","The platform doctype name which u want to add has been existed under platform,kindly pls reform your platform Ops plan.");
               return  result;
           }


            platformDocType.setDocTypeGuid(CommonHelper.getInstance().getNodeUUID());
            platformDocType.setCreateTime(new Date());
            if(platformDocType.getIsActive()==null) {
                platformDocType.setIsActive(true);
            }

            if(cephFileMetaInfoRecord.addPlatformDocType(platformDocType)!=null){

               //boardcast the platformDocType to diskFile cluster
               HttpServletRequest request= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
               String[] CrystalClusterIps=crystalClusterIps.split(";");
               for(String crystalClusterIp: CrystalClusterIps){
                   if(crystalClusterIp.equals(commonHelper.getRequestIP(request))==false) {
                       String postUrl = request.getScheme() + "://" + crystalClusterIp + ":" + request.getServerPort()+"/DiskFile/CephEntry/"+"addDocType";
                       Map<String,Object> parameterMap=new Hashtable<String,Object>();
                       parameterMap.put("docTypeName",platformDocType.getDocTypeName());
                       parameterMap.put("docTypeDesc",platformDocType.getDocTypeDesc());
                       parameterMap.put("maxFileSize",platformDocType.getMaxFileSize());
                       parameterMap.put("fileShareFolder",platformDocType.getFileShareFolder());
                       parameterMap.put("comment",platformDocType.getComment());
                       parameterMap.put("isActive",platformDocType.getIsActive());
                       try {
                           DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.json.toString());
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   }
               }
                result.put("result",true);
                result.put("platformDocType",cephFileMetaInfoRecord.getOnePlatformDocTypeByGuid(platformDocType.getDocTypeGuid()));
                result.put("desc","One platform doctype has been added into current node successfully.");
            }else
            {
                result.put("result",false);
                result.put("desc","One platform doctype has been added into current node failed.");
            }
            ThreadLocalContext.setDbKey(null);
            return result;
    }


    //创建平台平台支持的文件扩展名
    @RequestMapping(value = "/addFileExt",method =RequestMethod.POST)
    public Map<String, Object> addFileExtension(@RequestBody PlatformFileExt platformFileExt){

        Map<String,Object> result=new Hashtable<String,Object>();
        //fileExt name can not be null
        if(platformFileExt.getFileExtName()==null||platformFileExt.getFileExtName().isEmpty())
        {
            result.put("result",false);
            result.put("desc","Platform fileExt name can not be empty or null.");
            return  result;
        }

        //verify duplicate name
        ThreadLocalContext.setDbKey("main");
        if(cephFileMetaInfoRecord.getOnePlatformFileExtByName(platformFileExt.getFileExtName())!=null){
            result.put("result",false);
            result.put("desc","The platform fileExt name which you want to add has been existed under platform, Kindly pls reform your platform Ops plan.");
            return result;
        }

        platformFileExt.setFileExtGuid(CommonHelper.getInstance().getNodeUUID());
        platformFileExt.setCreateTime(new Date());
        if(platformFileExt.getIsActive()==null){
            platformFileExt.setIsActive(true);
        }

        if(cephFileMetaInfoRecord.addPlatformFileExt(platformFileExt)!=null){

            //boardcast the platformFileExt to diskFile cluster
            HttpServletRequest request= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String[] CrystalClusterIps=crystalClusterIps.split(";");
            for(String crystalClusterIp: CrystalClusterIps){
                if(crystalClusterIp.equals(commonHelper.getRequestIP(request))==false) {
                    String postUrl = request.getScheme() + "://" + crystalClusterIp + ":" + request.getServerPort()+"/DiskFile/CephEntry/"+"addFileExt";
                    Map<String,Object> parameterMap=new Hashtable<String,Object>();
                    parameterMap.put("fileExtName",platformFileExt.getFileExtName());

                    try {
                        DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.json.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            result.put("result",true);
            result.put("platformFileExt",cephFileMetaInfoRecord.getOnePlatformFileExtByGuid(platformFileExt.getFileExtGuid()));
            result.put("desc","Platform fileExt name has been added into platform successfully.");
        }else {
            result.put("result",false);
            result.put("desc","Platform fileExt name has been added into platform failed.");
        }
        ThreadLocalContext.setDbKey(null);
        return result;
    }

    //创建平台文件类型-文件扩展名关系
    @RequestMapping(value = "/addDocTypeFileExtRelation",method =RequestMethod.POST)
    public Map<String,Object> inputDocTypeFileExtRelation(@RequestParam (value = "docTypeId") long docTypeId,@RequestParam(value = "fileExtID") long platformFileExtID){

        Map<String,Object> result=new Hashtable<String, Object>();

        //verify duplicate DocytypeFileExt relationship
        ThreadLocalContext.setDbKey("main");
        if(cephFileMetaInfoRecord.getOnePlatformDocTypeFileExtRelationByForeignKeyGroup(docTypeId,platformFileExtID)!=null){

            result.put("result",false);
            result.put("desc","The platform doctype & fileExt relationship which u want to add has been existed under platform.Kindly pls reform your platform Ops plan.");
            return  result;
        }

        PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation=new PlatformDocTypeFileExtRelation();
        platformDocTypeFileExtRelation.setDocTypeId(docTypeId);
        platformDocTypeFileExtRelation.setPlatformFileExtID(platformFileExtID);
        platformDocTypeFileExtRelation.setDocTypeFileExtRelationGuid(CommonHelper.getInstance().getNodeUUID());
        platformDocTypeFileExtRelation.setCreateTime(new Date());

        if(cephFileMetaInfoRecord.addPlatformDocTypeFileExtRelation(platformDocTypeFileExtRelation)!=null){

            //boardcast the platformDocTypeFileExtRelation to diskFile cluster
            HttpServletRequest request= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String[] CrystalClusterIps=crystalClusterIps.split(";");
            for(String crystalClusterIp: CrystalClusterIps){
                if(crystalClusterIp.equals(commonHelper.getRequestIP(request))==false) {
                    String postUrl = request.getScheme() + "://" + crystalClusterIp + ":" + request.getServerPort()+"/DiskFile/CephEntry/"+"addDocTypeFileExtRelation";
                    Map<String,Object> parameterMap=new Hashtable<String,Object>();
                    parameterMap.put("docTypeId",platformDocTypeFileExtRelation.getDocTypeId());
                    parameterMap.put("fileExtID",platformDocTypeFileExtRelation.getPlatformFileExtID());

                    try {
                        DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.textBody.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            result.put("result",true);
            result.put("platformDocTypeFileExtRelation",cephFileMetaInfoRecord.getOnePlatformDocTypeFileExtRelationByGuid(platformDocTypeFileExtRelation.getDocTypeFileExtRelationGuid()));
            result.put("desc","The input docType & File Extension relationship has been created successfully.");
        }else {
            result.put("result",false);
            result.put("desc","The input docType & File Extension relationship has been created failed.");
        }
        ThreadLocalContext.setDbKey(null);
        return  result;
    }

    //获取平台文件类型列表
    @RequestMapping(value = "/seekDocType",method =RequestMethod.POST)
    @ResponseBody
    public List<PlatformDocType> getDocTypeList(){

         List<PlatformDocType> pdctList=new ArrayList<PlatformDocType>();
         ThreadLocalContext.setDbKey("main");
         pdctList=cephFileMetaInfoRecord.getAllPlatformDocType();
        ThreadLocalContext.setDbKey(null);
        return  pdctList;
    }

    //获取平台支持的文件扩展名列表
    @RequestMapping(value = "/seekFileExt",method =RequestMethod.POST)
    @ResponseBody
    public List<PlatformFileExt> getFileExtList(){

        List<PlatformFileExt> pfExt=new ArrayList<PlatformFileExt>();
        ThreadLocalContext.setDbKey("main");
        pfExt=cephFileMetaInfoRecord.getAllPlatformFileExt();
        ThreadLocalContext.setDbKey(null);
        return  pfExt;
    }

    //获取平台文件类型-文件扩展名关系列表
    @RequestMapping(value = "/seekDocTypeFileExtRelation",method =RequestMethod.POST)
    @ResponseBody
    public List<PlatformDocTypeFileExtRelation> getDocTypeFileExtRelationList(){

        List<PlatformDocTypeFileExtRelation> pfDcExt=new ArrayList<PlatformDocTypeFileExtRelation>();
        ThreadLocalContext.setDbKey("main");
        pfDcExt=cephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation();
        ThreadLocalContext.setDbKey(null);
        return  pfDcExt;
    }

    //按文件扩展名获取平台文件类型-文件扩展名关系列表
    @RequestMapping(value = "/seekDocTypeFileExtRelationByFileExtName",method =RequestMethod.POST)
    @ResponseBody
    public List<PlatformDocTypeFileExtRelation> getDocTypeFileExtRelationListByFileExtName(@RequestParam (value = "fileExtName") String fileExtName){

        List<PlatformDocTypeFileExtRelation> pfDcExt=new ArrayList<PlatformDocTypeFileExtRelation>();
        ThreadLocalContext.setDbKey("main");
        pfDcExt=cephFileMetaInfoRecord.getPlatformDocTypeFileExtRelationListByFileExtName(fileExtName);
        ThreadLocalContext.setDbKey(null);
        return  pfDcExt;
    }

      /*
        Below is the CephClusterTalk API |:)<|

    */
     /*Basic*/


    //修改当前节点的主机名
    @RequestMapping(value = "/setupNodeHostName",method = {RequestMethod.POST})
    public Map<String,Object> setupNodeHostName(@RequestParam(value = "targetHostName") String targetHostName){

          String cmd="hostnamectl set-hostname "+targetHostName;
          Map<String,Object> execResult= cephClusterTalk.runShellAndGetResult(cmd);
          return execResult;
    }
    //关闭selinux
    @RequestMapping(value = "/closeSeLinux",method = {RequestMethod.POST})
    public Map<String,Object> closeSeLinux(){

        String[] cmd={"sed -i 's/SELINUX=.*/SELINUX=disabled/' /etc/selinux/config","setenforce 0"};
        Map<String,Object> execResult= cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
    //配置无密码访问
    @RequestMapping(value = "/configNodeNonPwdAccess",method = {RequestMethod.POST})
    public Map<String,Object> configNodeNonPwdAccess(@RequestParam(value = "hostNameListStr") String hostNameListStr){

        String[] cmd1={"ssh-keygen -t rsa","\n","\n"};
        Map<String,Object> execResult1=cephClusterTalk.runShellAndGetResult(cmd1);

        if(execResult1.containsKey("result")&&execResult1.get("result").equals(true)){
            String[] hostNameList=hostNameListStr.split(";");
            if(hostNameList.length>0){
                String[] cmd2=new String[hostNameList.length];
                for(int i=0;i<hostNameList.length;i++){
                    cmd2[i]="ssh-copy-id root@"+hostNameList[i];
                }
                Map<String,Object> execResult2=cephClusterTalk.runShellAndGetResult(cmd2);
                if(execResult2.containsKey("result")&&execResult2.get("result").equals(true)){

                    return execResult2;
                }else {

                    return execResult2;
                }
            }else {
                return execResult1;
            }
        }else {

            return execResult1;
        }
    }

    //安装wget
    @RequestMapping(value = "/wgetInstall",method = {RequestMethod.POST})
    public Map<String,Object> wgetInstall(){

        String[] cmd={"yum -y install wget","yum -y install setup","yum -y install perl"};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;

    }

    //备份你的原yum镜像文件
    @RequestMapping(value = "/yumRepoBackup",method = {RequestMethod.POST})
    public Map<String,Object> yumRepoBackup(){

        String cmd="mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup";
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
    //下载新的CentOS-Base.repo
    @RequestMapping(value = "/downLoadYumRepo",method = {RequestMethod.POST})
    public Map<String,Object> downloadYumRepo(@RequestParam(value = "OsVersion") String OsVersionStr){

        String cmd="";
        Map<String, Object> execResult=new HashMap<>();
        switch (OsVersionStr){
            case "CentOS5":
                cmd="wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.163.com/.help/CentOS5-Base-163.repo";
            case "CentOS6":
                cmd="wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.163.com/.help/CentOS6-Base-163.repo";
            case "CentOS7":
                cmd="wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.163.com/.help/CentOS7-Base-163.repo";
        }
        if(cmd.isEmpty()&&cmd!="") {
            execResult = cephClusterTalk.runShellAndGetResult(cmd);
            return execResult;
        }else {

            execResult.put("result",false);
            execResult.put("desc","Your input OS-version was not supported on current node to downLoad Yum Repo");
            return execResult;
        }
    }

    //安装epel
    @RequestMapping(value = "/installEpel",method = {RequestMethod.POST})
    public Map<String,Object> installEpel(@RequestParam(value = "OsVersion") String OsVersionStr){

        String cmd="";
        Map<String, Object> execResult=new HashMap<>();
        switch (OsVersionStr){
            case "CentOS5":
                cmd="rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-5.noarch.rpm";
            case "CentOS6":
                cmd="rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-6.noarch.rpm";
            case "CentOS7":
                cmd="rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm";
            default:
                cmd="rpm -ivh http://mirrors.sohu.com/fedora-epel/epel-release-latest-7.noarch.rpm";
        }
        if(cmd.isEmpty()&&cmd!="") {
            execResult = cephClusterTalk.runShellAndGetResult(cmd);
            return execResult;
        }else {

            execResult.put("result",false);
            execResult.put("desc","Your input OS-version was not supported on current node to install Epel");
            return execResult;
        }

    }

    //创建主机ceph本地source
    @RequestMapping(value = "/createCephLocalRepo",method = {RequestMethod.POST})
    public Map<String,Object> createCephLocalRepo(){
        String cmd=CommonHelper.getInstance().getBaseJarPath().getPath()+File.separatorChar+"target/cephLocalSourceInstallMini.sh";
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
    //缓存yum包信息
    @RequestMapping(value = "/makeYumCache",method = {RequestMethod.POST})
    public Map<String,Object> makeYumCache(){
        String cmd="yum makecache";
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
     //安装ntp时间同步服务
    @RequestMapping(value = "/installNtp",method = {RequestMethod.POST})
    public Map<String,Object> installNtp(){
        String cmd="yum install ntp -y";
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }

    //重启ntp时间同步服务
    @RequestMapping(value = "/restartNtp",method = {RequestMethod.POST})
    public Map<String,Object> restartNtp(){
        String cmd[]={"systemctl enable ntpd","systemctl restart ntpd"};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }

     //安装ceph
    @RequestMapping(value = "/installCeph",method = {RequestMethod.POST})
     public Map<String,Object> installCeph(){
        String cmd[]={"yum install ceph -y","ceph -v"};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
     //安装ceph-deploy
     @RequestMapping(value = "/installCephDeploy",method = {RequestMethod.POST})
    public Map<String,Object> installCephDeploy(){
         String cmd[]={"yum install ceph-deploy -y","ceph-deploy --version"};
         Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
         return execResult;
     }

    //创建mon部署目录,并初始化一个ceph集群
    @RequestMapping(value = "/initCephCluster",method = {RequestMethod.POST})
    public Map<String,Object> initCephCluster(@RequestParam(value = "cephClusterHostNameStr") String cephClusterHostNameStr){
        String cmd[]={"mkdir cluster","cd cluster/","ceph-deploy new "+cephClusterHostNameStr};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
    //节点关闭防火墙
    @RequestMapping(value = "/closeNodeFireWall",method = {RequestMethod.POST})
    public Map<String,Object> closeNodeFireWall(){
        String cmd[]={"systemctl stop firewalld","systemctl disable firewalld"};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
    //mon create-initial,ceph-deploy admin xx xx xx
    @RequestMapping(value = "/initCephClusterMon",method = {RequestMethod.POST})
    public Map<String,Object> initCephClusterMon(@RequestParam(value = "cephClusterHostNameStr") String cephClusterHostNameStr) {

        String cmd[]={"ceph-deploy mon create-initial","ceph-deploy admin "+cephClusterHostNameStr};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }


    //创建osd
    @RequestMapping(value = "/createOsd",method = {RequestMethod.POST})
    public Map<String,Object> createOsd(@RequestBody Map<String,String> OsdInfo) {

         String osdHostName=OsdInfo.get("hostName");
         String osdDataDevice=OsdInfo.get("dataDevName");
         String osdBlockDbDevice=OsdInfo.get("blockDbDevName");
         String osdBlockWalDevice=OsdInfo.get("blockWalDevName");
         String cmd="ceph-deploy osd create "+osdHostName+" --bluestore --data "+osdDataDevice+" --block-db "+osdBlockDbDevice+" --block-wal "+osdBlockWalDevice;
         Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
         return execResult;
    }

    //Deploy mgr,初始化部署ceph集群mgr
    @RequestMapping(value = "/createCephClusterMgr",method = {RequestMethod.POST})
    public Map<String,Object> createCephClusterMgr(@RequestParam(value = "cephClusterHostNameStr") String cephClusterHostNameStr)
    {
        String cmd="ceph-deploy mgr create "+cephClusterHostNameStr;
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }

    //启动dashboard
    @RequestMapping(value = "/startCephClusterDashboard",method = {RequestMethod.POST})
    public Map<String,Object> startCephClusterDashboard(){
        String cmd="ceph mgr module enable dashboard";
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }

    //寻找到当前ceph集群的主mgr，并打开ceph dashboard页面
    @RequestMapping(value = "/seekCephDashBoard",method = {RequestMethod.POST,RequestMethod.GET})
    public void seekCephDashBoard(){
        HttpServletRequest request= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        Map<String,Object> activeMgrResult=cephClusterTalk.getActiveMgr();
        try {
            if (activeMgrResult.get("result").equals(true)
                    && activeMgrResult.get("activeMgrUrl") != "") {
                response.sendRedirect(activeMgrResult.get("activeMgrUrl").toString());
            }
        }catch (Exception e){

            log.trace(e.getMessage());
        }
    }

    /*CephFS*/

    //部署mds守护进程--（ceph-mgr）
    @RequestMapping(value = "/createCephClusterMds",method = {RequestMethod.POST})
    public Map<String,Object> createCephClusterMds(@RequestParam(value = "cephClusterHostNameStr") String cephClusterHostNameStr){

        String cmd="ceph-deploy mds create "+cephClusterHostNameStr;
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }
    //新建pool
    @RequestMapping(value = "/createCephFsPool",method = {RequestMethod.POST})
    public Map<String,Object> createCephFsPool(@RequestParam(value = "pgNumData") int pgNumData,@RequestParam(value = "pgNumMetadata") int pgNumMetadata){

        String[] cmd={"ceph osd pool create cephfs_data "+Integer.toString(pgNumData),"ceph osd pool create cephfs_metadata "+Integer.toString(pgNumMetadata)};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;

    }
    //设置pool副本数
    @RequestMapping(value = "/setCephFsPoolDuplicatesNum",method = {RequestMethod.POST})
    public Map<String,Object> setCephFsPoolDuplicatesNum(@RequestParam(value = "poolName") String poolName){

        Map<String,Object> totalMgrNumRs= cephClusterTalk.getTotalMgrNum();
        Map<String, Object> execResult=new HashMap<>();

        log.trace("Current platform file disk total Mgr count number is:"+totalMgrNumRs.get("totalMgrNum"));

        if(totalMgrNumRs.containsKey("result")&&totalMgrNumRs.get("result").equals(true)){
            String cmd="ceph osd pool set "+poolName+" size "+totalMgrNumRs.get("totalMgrNum");
            execResult=cephClusterTalk.runShellAndGetResult(cmd);
            return execResult;
        }else {

            execResult.put("result",false);
            execResult.put("desc","get platform file disk total Mgr count number failed.");
            return execResult;
        }

    }

    //新建cephfs
    @RequestMapping(value = "/createCephFs",method = {RequestMethod.POST})
    public Map<String,Object> createCephFs(@RequestParam(value = "cephFsName") String cephFsName){

        String cmd="ceph fs new "+cephFsName+" cephfs_metadata cephfs_data";
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;

    }
    //Java evn installation(not include config)
    @RequestMapping(value = "/installJavaEvn",method = {RequestMethod.POST})
    public Map<String,Object> installJavaEvn(){
        String[] cmd={"cd /usr","mkdir /usr/local/java/","tar -zxvf jdk-8u171-linux-x64.tar.gz -C /usr/local/java/","ln -s /usr/local/java/jdk1.8.0_171/bin/java /usr/bin/java"};
        Map<String, Object> execResult=cephClusterTalk.runShellAndGetResult(cmd);
        return execResult;
    }

    //安装libcephfs相关开发包
    @RequestMapping(value = "/installLibCephfs",method = {RequestMethod.POST})
    public Map<String,Object> installLibCephfs(){
        String[] cmd1={"yum -y install libcephfs2","yum -y install libcephfs_jni1"};
        String[] cmd2={"ln -s /usr/lib64/libcephfs_jni.so.1.0.0 /usr/lib/libcephfs_jni.so.1","ln -s /usr/lib64/libcephfs_jni.so.1.0.0 /usr/lib/libcephfs_jni.so"};
        String cmd3="yum -y install ceph-common";
        Map<String, Object> execResult1=cephClusterTalk.runShellAndGetResult(cmd1);
        if(execResult1.containsKey("result")&&execResult1.get("result").equals(true)){
            Map<String, Object> execResult2=cephClusterTalk.runShellAndGetResult(cmd2);
            if(execResult2.containsKey("result")&&execResult2.get("result").equals(true)){
                Map<String, Object> execResult3=cephClusterTalk.runShellAndGetResult(cmd3);
                return execResult3;
            }else {
                return execResult2;
            }
        }else {
            return execResult1;
        }
    }

}
