package cn.Boy.DiskFile.Scheduler;

import java.util.Timer;
import java.util.TimerTask;

import cn.Boy.DiskFile.ApplicationContextUtil;
import cn.Boy.DiskFile.ThreadLocalContext;
import cn.Boy.DiskFile.common.CommonHelper;
import cn.Boy.DiskFile.distributeFileEntry.PoseidonOptimizeSeek;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;



@Service("PoseidonOptimizeSeekRulesPersist")
public class PoseidonOptimizeSeekRulesPersist extends TimerTask implements IDeamonThreader {



    private final Log log = LogFactory.getLog(PoseidonOptimizeSeekRulesPersist.class);
    @Override
    public void run() {

        ThreadLocalContext.setDbKey("main");
        ((PoseidonOptimizeSeek)ApplicationContextUtil.getBean("PoseidonOptimizeSeek")).scanUpdate();
        log.trace("Poseidong scan was executed once.");
    }
    public boolean setTimerSchedule(long interval){

        try {
            log.trace("Enter the Poseidon time schedule.");
            Timer t = IDeamonThreader.getTimerInstance();
            PoseidonOptimizeSeekRulesPersist task = new PoseidonOptimizeSeekRulesPersist();
            t.schedule(task, CommonHelper.getInstance().getNodeRandomNum(1, 5) * 1000, interval * 1000);
            log.trace("Enter the Poseidon time schedule successfully with scan Rate:"+interval);
            return true;
        }catch (Exception e)
        {
            log.trace(e.getMessage()+e.getStackTrace());
            return false;
        }

    }

}
