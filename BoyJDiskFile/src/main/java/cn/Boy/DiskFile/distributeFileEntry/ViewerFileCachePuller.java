package cn.Boy.DiskFile.distributeFileEntry;

import cn.Boy.DiskFile.ApplicationContextUtil;
import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service("ViewerFileCachePuller")
public class ViewerFileCachePuller extends TimerTask implements IDeamonThreader{

    private final Log log = LogFactory.getLog(ViewerFileCachePuller.class);
    @Override
    public void run() {

        ThreadLocalContext.setDbKey("main");
        ((CephViewerFileCacheManager) ApplicationContextUtil.getBean("CephViewerFileCacheManager")).PullNonExpiredViewerFileCacheFromClusterToLocal();
        log.trace("ViewerFileCachePuller scan was executed once.");

    }
    public boolean setTimerSchedule(long interval){

        try {
            log.trace("Enter the ViewerFileCachePuller time schedule.");
            Timer t = IDeamonThreader.getTimerInstance();
            ViewerFileCachePuller task = new ViewerFileCachePuller();
            t.schedule(task, CommonHelper.getInstance().getNodeRandomNum(1, 5) * 1000, interval * 1000);
            log.trace("Enter the ViewerFileCachePuller time schedule successfully with scan Rate:"+interval);
            return true;
        }catch (Exception e)
        {
            log.trace(e.getMessage()+e.getStackTrace());
            return false;
        }

    }
}
