package cn.Boy.DiskFile.Scheduler;

import cn.Boy.DiskFile.ApplicationContextUtil;
import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.distributeFileEntry.CephViewerFileCacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service("ViewerFileCacheRevoker")
public class ViewerFileCacheRevoker extends TimerTask implements IDeamonThreader {

    private final Log log = LogFactory.getLog(ViewerFileCacheRevoker.class);
    @Override
    public void run() {

        ThreadLocalContext.setDbKey("main");
        ((CephViewerFileCacheManager) ApplicationContextUtil.getBean("CephViewerFileCacheManager")).revokeExpiredViewerFileCache();
        log.trace("ViewerFileCacheRevoker scan was executed once.");

    }
    public boolean setTimerSchedule(long interval){

        try {
            log.trace("Enter the ViewerFileCacheRevoker time schedule.");
            Timer t = IDeamonThreader.getTimerInstance();
            ViewerFileCacheRevoker task = new ViewerFileCacheRevoker();
            t.schedule(task, CommonHelper.getInstance().getNodeRandomNum(1, 5) * 1000, interval * 1000);
            log.trace("Enter the ViewerFileCacheRevoker time schedule successfully with scan Rate:"+interval);
            return true;
        }catch (Exception e)
        {
            log.trace(e.getMessage()+e.getStackTrace());
            return false;
        }

    }
}
