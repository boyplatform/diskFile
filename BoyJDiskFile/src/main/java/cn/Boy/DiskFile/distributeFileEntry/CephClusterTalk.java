package cn.Boy.DiskFile.distributeFileEntry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.Boy.DiskFile.common.DiskFileHttpHelper;
import cn.Boy.DiskFile.common.Dom4jHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("CephClusterTalk")
public class CephClusterTalk extends AbsCommonClusterTalker {

    @Autowired
    @Qualifier("Dom4jHelper")
    private Dom4jHelper dom4jHelper;

    @Value("${DiskFile.cephCluster.apiUrl}") String apiUrl;

    private final Log log = LogFactory.getLog(CephClusterTalk.class);

    @Override
    public Map<String,Object> getActiveMgr(){

        Map<String,Object> returnRs=new HashMap<>();
        try {

            //调ceph原生接口得到返回值
            String url = apiUrl + "mgr/dump";
            Map<String, Object> parameterMap = new Hashtable<>();
            String responseRs = DiskFileHttpHelper.getInstance().getRequest(url, parameterMap, "queryCephApiUrl");

            //解析返回的xml得到active_addr
            dom4jHelper.parseXML(responseRs);
            String active_addr= dom4jHelper.findSingleElementText("//mgrmap/active_addr");
            //正则得到IP，拼接出7000端口url返回结果
            String regEx="((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(active_addr);

            String activeIp="";
            while (m.find()) {
                activeIp=m.group();
                log.trace("Current active mgr IP is:"+activeIp);
                break;
            }

            returnRs.put("result",true);
            returnRs.put("activeIp",activeIp);
            returnRs.put("desc","get active mgr url successfully.");
            returnRs.put("activeMgrUrl","http://"+activeIp+":7000");
            return returnRs;
        }catch (Exception e){

            returnRs.put("result",false);
            returnRs.put("desc","met error during get active mgr process");
            returnRs.put("error",e.getMessage());
            return returnRs;
        }
    }

    @Override
    public Map<String,Object> getTotalMgrNum(){
        Map<String,Object> returnRs=new HashMap<>();
        try {
            //调ceph原生接口得到返回值
            String url = apiUrl + "mgr/count-metadata?property=service";
            Map<String, Object> parameterMap = new Hashtable<>();
            String responseRs = DiskFileHttpHelper.getInstance().getRequest(url, parameterMap, "queryCephApiUrl");

            //解析返回的xml得到active_addr
            dom4jHelper.parseXML(responseRs);
            String totalMgrNum= dom4jHelper.findSingleElementText("//service/unknown");

            returnRs.put("result",true);
            returnRs.put("totalMgrNum",totalMgrNum);
            returnRs.put("desc","get totalMgrNum successfully.");
            return returnRs;
        }catch (Exception e){
            returnRs.put("result",false);
            returnRs.put("desc","met error during get totalMgrNum process.");
            returnRs.put("error",e.getMessage());
            return returnRs;
        }
    }

    @Override
    public Map<String,Object> runShellAndGetResult(String cmd){

        Map<String,Object> result=new HashMap<>();
        try {
            if(cmd.isEmpty()==false) {
                Process p = Runtime.getRuntime().exec(cmd);
                InputStream Is=p.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(Is));
                String line;
                StringBuffer totalExecuteResult=new StringBuffer();
                while((line=reader.readLine())!=null){

                    totalExecuteResult.append(line);
                }
                result.put("result",true);
                result.put("desc","your input cmd has been executed successfully.");
                result.put("cmdResult",totalExecuteResult.toString());
            }
        }catch (Exception e){

            result.put("result",false);
            result.put("desc","met error exception during running your input cmd.");
            result.put("error",e.getMessage());
            return result;
        }

        return result;
    }

    @Override
    public Map<String,Object> runShellAndGetResult(String cmd[]){

        Map<String,Object> result=new HashMap<>();
        try {
            if(cmd.length>0) {
                Process p = Runtime.getRuntime().exec(cmd);
                InputStream Is=p.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(Is));
                String line;
                StringBuffer totalExecuteResult=new StringBuffer();
                while((line=reader.readLine())!=null){

                    totalExecuteResult.append(line);
                }
                result.put("result",true);
                result.put("desc","your input cmd has been executed successfully.");
                result.put("cmdResult",totalExecuteResult.toString());
            }
        }catch (Exception e){

            result.put("result",false);
            result.put("desc","met error exception during running your input cmd.");
            result.put("error",e.getMessage());
            return result;
        }

        return result;
    }
}
