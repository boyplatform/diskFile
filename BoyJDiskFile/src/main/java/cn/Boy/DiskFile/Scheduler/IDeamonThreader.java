package cn.Boy.DiskFile.Scheduler;


import java.util.Timer;
import java.util.TimerTask;

public interface IDeamonThreader {

    //定义全局定时器
    public static Timer DeamonThreaderTimer=new Timer();
    public static Timer getTimerInstance(){
        return DeamonThreaderTimer;
    }

    //绑定一个执行计划
    public boolean setTimerSchedule(long interval);


}
