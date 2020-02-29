package cn.Boy.DiskFile.distributeFileEntry;

import cn.Boy.DiskFile.ApplicationContextUtil;
import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service("LocalNotExistFileClear")
public class LocalNotExistFileClear extends TimerTask implements IDeamonThreader {

    private final Log log = LogFactory.getLog(LocalNotExistFileClear.class);
    @Override
    public void run() {

        ThreadLocalContext.setDbKey("main");
        ((CephFileNodeMetaInfoSync) ApplicationContextUtil.getBean("CephFileNodeMetaInfoSync")).clearLocalFileNotExistedOnCluster();
        log.trace("LocalNotExistFileClear was executed once.");

    }
    public boolean setTimerSchedule(long interval){

        try {
            log.trace("Enter the LocalNotExistFileClear time schedule.");
            Timer t = IDeamonThreader.getTimerInstance();
            LocalNotExistFileClear task = new LocalNotExistFileClear();
            t.schedule(task, CommonHelper.getInstance().getNodeRandomNum(1, 5) * 1000, interval * 1000);
            log.trace("Enter the LocalNotExistFileClear time schedule successfully with scan Rate:"+interval);
            return true;
        }catch (Exception e)
        {
            log.trace(e.getMessage()+e.getStackTrace());
            return false;
        }

    }
}
