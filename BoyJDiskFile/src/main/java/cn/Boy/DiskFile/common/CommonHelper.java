package cn.Boy.DiskFile.common;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import cn.Boy.DiskFile.common.DiskFileHttpHelper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component("CommonHelper")
@Scope("singleton")
public class CommonHelper {


    private static CommonHelper instance = null;
    public static CommonHelper getInstance(){

            if (instance == null) {
                instance = new CommonHelper();
            }

        return instance;
    }

    private final Log log = LogFactory.getLog(CommonHelper.class);

    private  String cachedNodePubNetWorkIp="";
    private  Date cachedNodePubNetWorkIpDate=null;

    @Value("${platformArch.PoSeidong.MemoryLiveTimeSec:100}") int memoryLiveTimeSec;
    @Value("${platformArch.PoSeidong.MemoryPerHitComeUpSeconds:15}") int memoryPerHitComeUpSeconds;
    @Value("${platformArch.ViewerFileCache.ViewerFileNameHashTimes}") int viewerFileNameHashTimes;
    @Value("${platformArch.ViewerFileCache.ViewerCacheFileNameLenth}") int viewerCacheFileNameLenth;
    @Value("${platformArch.SelfPubNetworkIpCacheTime}") int selfPubNetworkIpCacheTime;
    @Value("${platformArch.CheckSelfPubNetworkIpUrl}") String checkSelfPubNetworkIpUrl;
    @Value("${platformArch.CrystalClusterNetworkMode}") String crystalClusterNetworkMode;

    public String getNodeUUID(){

        return UUID.randomUUID().toString().replace("-","");
    }

    public String getPlatFormUUID(){

        return "";
    }

    public String getNodeTimeSpanString(){
        Date now=new Date();
        SimpleDateFormat f=new SimpleDateFormat("yyyyMMddhhmmss");

       return f.format(now);
    }

    public String getPlatFormTimeSpanString(){

        return "";
    }

    public long getNodeHashCode(String str)
    {
        long h=0;
        if(h==0)
        {
            int off=0;
            char val[]=str.toCharArray();
            long len=str.length();
            for(long i=0;i<len;i++)
            {
                h=31*h+val[off++];
            }
        }
        return  h;
    }

    public long getPlatFormHashCode(String str){

        long h=0;
        if(h==0)
        {
            int off=0;
            char val[]=str.toCharArray();
            long len=str.length();
            for(long i=0;i<len;i++)
            {
                h=31*h+val[off++];
            }
        }
        return  h;
    }

    public int getNodeRandomNum(int min,int max){

        return (int)(min+Math.random()*(max-min+1));
    }

    public  int getPlatFormRandomNum(int min,int max){

        return (int)(min+Math.random()*(max-min+1));
    }


    public File getBaseJarPath(){
        ApplicationHome home=new ApplicationHome(getClass());
        File jarFile=home.getSource();
        return jarFile.getParentFile();
    }

    public void initNodeFilePath(){

        //判断当前节点是否存在target，无创建
        File targetFolder=  new File(getBaseJarPath().getPath()+File.separatorChar+"target");
        targetFolder.mkdirs();

        while(targetFolder.exists()==false)
        {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
         //判断当前节点是否存在target/uploadFileCache，无创建
        File uploadFileCacheFolder=  new File(targetFolder.getPath()+File.separatorChar+"uploadFileCache");
        uploadFileCacheFolder.mkdirs();
         //判断当前节点是否存在target/static,interact,js,css,viewerCache,无创建
        File staticFolder=  new File(uploadFileCacheFolder.getPath()+File.separatorChar+"static");
        staticFolder.mkdirs();

        File interactFolder=  new File(uploadFileCacheFolder.getPath()+File.separatorChar+"interact");
        interactFolder.mkdirs();

        File jsFolder=  new File(uploadFileCacheFolder.getPath()+File.separatorChar+"js");
        jsFolder.mkdirs();

        File cssFolder=  new File(uploadFileCacheFolder.getPath()+File.separatorChar+"css");
        cssFolder.mkdirs();

        File seedFolder=  new File(uploadFileCacheFolder.getPath()+File.separatorChar+"seed");
        seedFolder.mkdirs();

        File viewerCacheFolder=  new File(uploadFileCacheFolder.getPath()+File.separatorChar+"viewerCache");
        viewerCacheFolder.mkdirs();


    }


    //Get File hash rename
    public Map<String,Object> getFileHashFreshName(String currentFileName){
        Map<String,Object> result=new HashMap<String,Object>();
        String timeSpan=this.getNodeTimeSpanString();
        String uuid=this.getNodeUUID();
        if(currentFileName.contains("_")&&currentFileName.split("_").length==3)
        {
            result.put("renewFileName", currentFileName.split("_")[0] + "_" + timeSpan + "_" + this.getNodeHashCode(uuid + timeSpan));

        }else {
            result.put("renewFileName", currentFileName + "_" + timeSpan + "_" + this.getNodeHashCode(uuid + timeSpan));
        }
        result.put("renewFileVersion",this.getNodeHashCode(uuid + timeSpan));
        return result;
    }

    //Get hash viewerCacheFileName by input condition
    public String getHashViewerCacheFileName(String filePath,String fileName,String userGuid){

        String viewerCacheFileName="";
        String tempFileName = filePath + fileName + userGuid;
        for(int i=0;i<viewerFileNameHashTimes;i++){
            if(i==0) {
                viewerCacheFileName = getSHA256Str(tempFileName);
            }else {
                viewerCacheFileName=getSHA256Str(viewerCacheFileName);
            }
        }
        return viewerCacheFileName.substring(0,viewerCacheFileNameLenth-1)+ "." + fileName.split("\\.")[1];
    }

    //Get MB from Byte
    public int getMbFromByte(long byteNum){

        return (int)byteNum/1024/1024;
    }


    //get Poseidong parameter
    public Map<String,Integer> getPoseidongParameter(){

        Map<String,Integer> resultMap=new Hashtable<String,Integer>();
        resultMap.put("memoryLiveTimeSec",memoryLiveTimeSec);
        resultMap.put("memoryPerHitComeUpSeconds",memoryPerHitComeUpSeconds);
        log.trace("memoryLiveTimeSec:"+memoryLiveTimeSec+"|"+"memoryPerHitComeUpSeconds:"+memoryPerHitComeUpSeconds);
        return  resultMap;
    }

    //get SHA256
    public String getSHA256Str(String str){
        MessageDigest messageDigest;
        String encodeStr="";
        try{
            messageDigest=MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr=byteToHex(messageDigest.digest());

        }catch (NoSuchAlgorithmException e){

            log.trace(e.getStackTrace());

        }catch (UnsupportedEncodingException e){

            log.trace(e.getStackTrace());
        }

        return encodeStr;
    }
    private String byteToHex(byte[] bytes){
        StringBuffer stringBuffer=new StringBuffer();
        String temp=null;
        for(int i=0;i<bytes.length;i++){
            temp=Integer.toHexString(bytes[i] & 0xFF);
            if(temp.length()==1){
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public  String getRequestIP(HttpServletRequest request){

        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public  String getSelfIp() {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;// 是否找到外网IP


            if(crystalClusterNetworkMode.equals("internet")){
                netip=getSelfPubNetworkIp();
                if (netip != null && !"".equals(netip)) {
                    finded = true;
                }
            }

            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = (InetAddress) address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                    } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
                        localip = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    private String getSelfPubNetworkIp(){

        try
        {
            if (cachedNodePubNetWorkIp.equals("") || cachedNodePubNetWorkIpDate == null || (new Date()).after(new Date(cachedNodePubNetWorkIpDate.getTime() + selfPubNetworkIpCacheTime * 1000))) {
                String getUrl =checkSelfPubNetworkIpUrl;
                Map<String, Object> parameterMap = new Hashtable<String, Object>();
                JSONObject jsonRs = (JSONObject) (new JSONParser().parse(DiskFileHttpHelper.getInstance().getRequest(getUrl,parameterMap,DiskFileHttpHelper.getQuestMode.json.toString())));
                cachedNodePubNetWorkIp=jsonRs.get("ip").toString();
                cachedNodePubNetWorkIpDate=new Date();
            }
        }catch (Exception e){
            cachedNodePubNetWorkIp="";
            cachedNodePubNetWorkIpDate=null;
            e.printStackTrace();
        }

        return  cachedNodePubNetWorkIp;
    }
}
