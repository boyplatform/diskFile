package cn.Boy.DiskFile.Scheduler;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import cn.Boy.DiskFile.ApplicationContextUtil;
import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.crystalBlock.task.FileMetaInfoBoardCast.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service("FileMetaInfoBoardCastSchedule")
public class FileMetaInfoBoardCastSchedule extends TimerTask implements IDeamonThreader  {
    private final Log log = LogFactory.getLog(FileMetaInfoBoardCastSchedule.class);
    @Override
    public void run() {

        ThreadLocalContext.setDbKey("main");
        ((DeamonThreads) ApplicationContextUtil.getBean("crystalBlock.task.FileMetaInfoBoardCast.DeamonThreads")).boardCastFileMetaInfoToCrystalCluster();
        log.trace("FileMetaInfoBoardCastSchedule was executed once.");

    }
    public boolean setTimerSchedule(long interval){

        try {
            log.trace("Enter the FileMetaInfoBoardCastSchedule time schedule.");
            Timer t = IDeamonThreader.getTimerInstance();
            FileMetaInfoBoardCastSchedule task = new FileMetaInfoBoardCastSchedule();
            t.schedule(task, CommonHelper.getInstance().getNodeRandomNum(1, 5) * 1000, interval * 1000);
            log.trace("Enter the FileMetaInfoBoardCastSchedule time schedule successfully with scan Rate:"+interval);
            return true;
        }catch (Exception e)
        {
            log.trace(e.getMessage()+e.getStackTrace());
            return false;
        }

    }
}
