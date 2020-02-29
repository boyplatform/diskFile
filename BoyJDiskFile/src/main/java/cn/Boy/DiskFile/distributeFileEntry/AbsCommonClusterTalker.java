package cn.Boy.DiskFile.distributeFileEntry;

import java.util.Map;

public abstract class AbsCommonClusterTalker {
    //mgr
    public Map<String,Object> getActiveMgr(){

        return null;
    }

    public Map<String,Object> getTotalMgrNum(){
        return null;
    }
    //common Shell execute
    public Map<String,Object> runShellAndGetResult(String cmd){


         return null;
    }
    public Map<String,Object> runShellAndGetResult(String cmd[]){


        return null;
    }
}
