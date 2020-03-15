package cn.Boy.DiskFile;


import cn.Boy.DiskFile.Scheduler.*;
import cn.Boy.DiskFile.distributeFileEntry.*;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import cn.Boy.DiskFile.common.*;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
@MapperScan("cn.Boy.DiskFile.dao")
@EnableConfigurationProperties
public class  Application {

	public static void main(String[] args) {

	    ConfigurableApplicationContext applicationContext= SpringApplication.run(Application.class, args);


		//初始化节点文件目录
		CommonHelper.getInstance().initNodeFilePath();

		//初始化Poseidon Persistent定时任务线程
		long rulePersistentScanRate=Long.parseLong(applicationContext.getEnvironment().getProperty("platformArch.PoSeidong.RulePersistentScanRate"));
		//设定每个几秒进行一次Poseidon扫描更新
		((PoseidonOptimizeSeekRulesPersist)applicationContext.getBean("PoseidonOptimizeSeekRulesPersist")).setTimerSchedule(rulePersistentScanRate);

		//启动后进行一次自动挂载
		String username= applicationContext.getEnvironment().getProperty("DiskFile.cephCluster.username");
		String userKey= applicationContext.getEnvironment().getProperty("DiskFile.cephCluster.userKey");
		String monIpStr= applicationContext.getEnvironment().getProperty("DiskFile.cephCluster.monIpStr");
		String mountPath= applicationContext.getEnvironment().getProperty("DiskFile.cephCluster.mountPath");
		((CephFileOperator)applicationContext.getBean("CephFileOperator")).mount(monIpStr,username,userKey,mountPath);

		//初始化ViewerFileCacheRevoker定时任务线程
		long viewerFileCacheRevokeRate=Long.parseLong(applicationContext.getEnvironment().getProperty("platformArch.ViewerFileCache.ViewerFileCacheRevokeRate"));
		((ViewerFileCacheRevoker)applicationContext.getBean("ViewerFileCacheRevoker")).setTimerSchedule(viewerFileCacheRevokeRate);

		//初始化LocalNotExistFileClear定时任务线程
		long localNotExistFileClearRate=Long.parseLong(applicationContext.getEnvironment().getProperty("platformArch.LocalNotExistFileClearRate"));
		((LocalNotExistFileClear)applicationContext.getBean("LocalNotExistFileClear")).setTimerSchedule(localNotExistFileClearRate);

		//初始化ViewerFileCachePuller定时任务线程
		long viewerFileCachePullRate=Long.parseLong(applicationContext.getEnvironment().getProperty("platformArch.ViewerFileCache.ViewerFileCachePullRate"));
		((ViewerFileCachePuller)applicationContext.getBean("ViewerFileCachePuller")).setTimerSchedule(viewerFileCachePullRate);

		//初始化FileMetaInfoBoardCastSchedule定时任务线程
		long fileMetaInfoBoardCastScheduleRate=Long.parseLong(applicationContext.getEnvironment().getProperty("platformArch.FileMetaInfoBoardCastScheduleRate"));
		((FileMetaInfoBoardCastSchedule)applicationContext.getBean("FileMetaInfoBoardCastSchedule")).setTimerSchedule(fileMetaInfoBoardCastScheduleRate);

	}
}

