package cn.Boy.DiskFile;


import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

//add proj component into unit test
import cn.Boy.DiskFile.distributeFileEntry.*;
import cn.Boy.DiskFile.common.*;
import cn.Boy.DiskFile.pojo.*;
import cn.Boy.DiskFile.dao.PlatformOpsDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.text.html.parser.Entity;
import java.io.File;
import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTests {

	  @Autowired
	  @Qualifier("CephFileMetaInfoRecord")
	  private ICommonFileMetaInfoRecorder CephFileMetaInfoRecord;

	  @Autowired
	  private PlatformOpsDao platformOpsDao;

	  @Autowired
	  @Qualifier("DBUpgradeHistoryOps")
	  private IPlatformOps platformOps_DBUpgradeHistory;

	  @Autowired
	  @Qualifier("UnitNodeRelationOps")
	  private IPlatformOps platformOps_unitNodeRelation;

	  @Autowired
	  @Qualifier("CustomerDbListOps")
	  private IPlatformOps platformOps_CustomerDbList;

	  @Autowired
	  @Qualifier("CephFileOperator")
	  private AbsCommonFileOperator cephFileOperator;

	  //创建基础数据测试的对象，并在before中初始化测试参数，用于测基础数据表的增删改查。
	private static String UserGuid="";

	private static void setUserGuid(String userGuid)
	{
		UserGuid=userGuid;
	}
	private static String getUserGuid(){
		return UserGuid;
	}

    private static PlatformFileExt platformFileExt=new PlatformFileExt();
	private static PlatformFileExt getplatformFileExt(){
		return platformFileExt;
	}

	private static PlatformDocType platformDocType=new PlatformDocType();
	private static PlatformDocType getPlatformDocType(){
		return platformDocType;
	}

	private static PlatformDocTypeFileExtRelation platformDocTypeFileExtRelation=new PlatformDocTypeFileExtRelation();
	private static PlatformDocTypeFileExtRelation getPlatformDocTypeFileExtRelation(){
		return platformDocTypeFileExtRelation;
	}

	private static AppPlatformDocTypeRelation appPlatformDocTypeRelation=new AppPlatformDocTypeRelation();
	private static AppPlatformDocTypeRelation getAppPlatformDocTypeRelation(){
		return appPlatformDocTypeRelation;
	}

	private static DocumentList documentList=new DocumentList();
	private static DocumentList getDocumentList(){
		return documentList;
	}

	private static RequestLog requestLog=new RequestLog();
	private static RequestLog getRequestLog(){
		return requestLog;
	}

	private static OperationLog operationLog=new OperationLog();
	private static OperationLog getOperationLog(){
		return operationLog;
	}

	private static DBUpgradeHistory dbUpgradeHistory=new DBUpgradeHistory();
	private static DBUpgradeHistory getDBUpgradeHistory(){
		return dbUpgradeHistory;
	}

	private static UnitNodeRelation unitNodeRelation=new UnitNodeRelation();
	private static UnitNodeRelation getUnitNodeRelation(){
		return unitNodeRelation;
	}

	private static CustomerDbList customerDbList=new CustomerDbList();
	private static CustomerDbList getCustomerDbList(){
		return customerDbList;
	}

	private static final Log log = LogFactory.getLog(AppTests.class);



	@BeforeClass
	public static void Prepare(){

        log.trace("Prepare executed once!");

		setUserGuid(UUID.randomUUID().toString().replace("-",""));
		ThreadLocalContext.setDbKey("main");

		//platformFileExt
		getplatformFileExt().setFileExtGuid(UUID.randomUUID().toString().replace("-",""));
		getplatformFileExt().setFileExtName("txt");
		getplatformFileExt().setCreateTime(new Date());

		//platformDocType
		getPlatformDocType().setDocTypeGuid(UUID.randomUUID().toString().replace("-",""));
		getPlatformDocType().setCreateTime(new Date());
		getPlatformDocType().setDocTypeName("testPlatformDocType");
		getPlatformDocType().setComment("testPlatformDocType");
		getPlatformDocType().setDocTypeDesc("testPlatformDocType");
		getPlatformDocType().setFileShareFolder("/mnt");
		getPlatformDocType().setMaxFileSize(10);
		getPlatformDocType().setIsActive(true);


		//platformDocTypeFileExtRelation

		getPlatformDocTypeFileExtRelation().setDocTypeFileExtRelationGuid(UUID.randomUUID().toString().replace("-",""));
		getPlatformDocTypeFileExtRelation().setCreateTime(new Date());
		getPlatformDocTypeFileExtRelation().setIsActive(true);




		//AppPlatformDocTypeRelation
		getAppPlatformDocTypeRelation().setAppdocTypeRelationGuid(UUID.randomUUID().toString().replace("-",""));
		getAppPlatformDocTypeRelation().setCreateTime(new Date());
		getAppPlatformDocTypeRelation().setAppId((long) 1);
		getAppPlatformDocTypeRelation().setIsActive(true);

		//documentList
		getDocumentList().setDocGuid(UUID.randomUUID().toString().replace("-",""));
		getDocumentList().setCreateTime(new Date());
		getDocumentList().setIsActive(true);

		//RequestLog
		getRequestLog().setReqGuid(UUID.randomUUID().toString().replace("-",""));
		getRequestLog().setCreateTime(new Date());
		getRequestLog().setIsActive(true);
		getRequestLog().setUserId((long)0);

		//operationLog
		getOperationLog().setOperationLogGuid(UUID.randomUUID().toString().replace("-",""));

		//DBUpgradeHistory
		getDBUpgradeHistory().setNodeDbUpgradeHIstoryGuid(UUID.randomUUID().toString().replace("-",""));

		//unitNodeRelation
		getUnitNodeRelation().setUnitNodeRelationGuid(UUID.randomUUID().toString().replace("-",""));


		//customerDbList
		getCustomerDbList().setGuid(UUID.randomUUID().toString().replace("-",""));



	}


	@Test
	public void contextLoads() throws Exception {

	}


	@Ignore
	public void UnitTest_00_FormerTestDataClear() throws Exception{

		log.trace("Begin to clean the former test data!");

		//sub tables
		platformOpsDao.deletePlatformDocTypeFileExtRelation();
		platformOpsDao.deleteAppPlatformDocTypeRelation();
		//platformOpsDao.deleteDocumentList();

		//major tables
		platformOpsDao.deletePlatformFileExt();
		platformOpsDao.deletePlatformDocType();
		platformOpsDao.deleteRequestLog();
		platformOpsDao.deleteOperationLog();
		platformOpsDao.deleteDBUpgradeHistory();
		platformOpsDao.deleteUnitNodeRelation();
		platformOpsDao.deleteCustomerDbList();
		log.trace("Former test data has been cleaned!");

	}

	//主表
	//test platformFileExt
	@Ignore
	public void UnitTest_01_platformFileExt() throws Exception {

		try {
			log.trace("UnitTest_01_platformFileExt started for add ");
			getplatformFileExt().setIsActive(true);
			Assert.assertTrue("UnitTest_01_platformFileExt add failed", CephFileMetaInfoRecord.addPlatformFileExt(getplatformFileExt()) != null);
		}catch (Exception e)
		{
			log.trace("platformFileExt duplicate name verification during add has been tested successfully");
		}finally {
			getplatformFileExt().setFileExtID(CephFileMetaInfoRecord.getOnePlatformFileExtByGuid(getplatformFileExt().getFileExtGuid()).getFileExtID());
		}

	/*    try {
			log.trace("UnitTest_01_platformFileExt started for update ");
			getplatformFileExt().setFileExtName(getplatformFileExt().getFileExtName() + "_updated");
			Assert.assertTrue("UnitTest_01_platformFileExt update failed",  CephFileMetaInfoRecord.updatePlatformFileExt(getplatformFileExt()) != null);
		}catch (Exception e)
		{
			log.trace("platformFileExt duplicate name verification during update has been tested successfully");
		}
*/
		log.trace("UnitTest_01_platformFileExt started for select "+"size:"+CephFileMetaInfoRecord.getAllPlatformFileExt().size());

		Assert.assertTrue("UnitTest_01_platformFileExt select failed",CephFileMetaInfoRecord.getAllPlatformFileExt().size()>0);
        //platformFileExtGuid=platformFileExt.getFileExtGuid();
	     CephFileMetaInfoRecord.getAllPlatformFileExt().forEach(x->{
			if(x.getFileExtGuid()==getplatformFileExt().getFileExtGuid()){

				log.trace("UnitTest_01_platformFileExt finished successfully --"+x.getFileExtID());
			}});

	}
	//test platformDocType
	@Ignore
	public void UnitTest_02_platformDocType() throws Exception {
		try {
			log.trace("UnitTest_02_platformDocType started for add ");
			Assert.assertTrue("UnitTest_02_platformDocType add failed", CephFileMetaInfoRecord.addPlatformDocType(getPlatformDocType()) != null);
		}catch (Exception e){
			log.trace("platformDocType duplicated name verification during add has been tested successfully");
		}finally {
		 getPlatformDocType().setDocTypeId(CephFileMetaInfoRecord.getOnePlatformDocTypeByGuid(getPlatformDocType().getDocTypeGuid()).getDocTypeId());
		}
		/*try {
			log.trace("UnitTest_02_platformDocType started for update ");
			getPlatformDocType().setDocTypeName(getPlatformDocType().getDocTypeName() + "_updated");
			Assert.assertTrue("UnitTest_02_platformDocType update failed", CephFileMetaInfoRecord.updatePlatformDocType(getPlatformDocType()) != null);
		}catch (Exception e)
		{
			log.trace("platformDocType duplicated name verification during update has been tested successfully");
		}*/

		log.trace("UnitTest_02_platformDocType started for select ");
		Assert.assertTrue("UnitTest_02_platformDocType select failed",CephFileMetaInfoRecord.getAllPlatformDocType().size()>0);
        //dDocTypeGuid=platformDocType.getDocTypeGuid();

		CephFileMetaInfoRecord.getAllPlatformDocType().forEach(x->{
			if(x.getDocTypeGuid()==getPlatformDocType().getDocTypeGuid()){

				log.trace("UnitTest_02_platformDocType finished successfully --"+x.getDocTypeId());
			}});


	}

	//从表
	//test platformDocTypeFileExtRelation
	@Ignore
	public void UnitTest_03_platformDocTypeFileExtRelation() throws Exception {

		log.trace("platformDocTypeFileExtRelation_UnitTest started for add ");

		getplatformFileExt().setFileExtID(CephFileMetaInfoRecord.getOnePlatformFileExtByGuid(getplatformFileExt().getFileExtGuid()).getFileExtID());
		getPlatformDocType().setDocTypeId(CephFileMetaInfoRecord.getOnePlatformDocTypeByGuid(getPlatformDocType().getDocTypeGuid()).getDocTypeId());


		getPlatformDocTypeFileExtRelation().setPlatformFileExtID(getplatformFileExt().getFileExtID());
		getPlatformDocTypeFileExtRelation().setDocTypeId(getPlatformDocType().getDocTypeId());
		Assert.assertTrue("platformDocTypeFileExtRelation_UnitTest add failed",CephFileMetaInfoRecord.addPlatformDocTypeFileExtRelation(getPlatformDocTypeFileExtRelation())!=null);

		log.trace("platformDocTypeFileExtRelation_UnitTest started for update ");
		platformDocTypeFileExtRelation.setIsActive(true);
		Assert.assertTrue("platformDocTypeFileExtRelation_UnitTest update failed",CephFileMetaInfoRecord.updatePlatformDocTypeFileExtRelation(getPlatformDocTypeFileExtRelation())!=null);

		log.trace("platformDocTypeFileExtRelation_UnitTest started for select ");
		Assert.assertTrue("platformDocTypeFileExtRelation_UnitTest select failed",CephFileMetaInfoRecord.getAllPlatformDocTypeFileExtRelation().size()>0);


		getPlatformDocTypeFileExtRelation().setPlatformFileExtID((long)0);
		getPlatformDocTypeFileExtRelation().setDocTypeId((long)0);
		try{
			CephFileMetaInfoRecord.addPlatformDocTypeFileExtRelation(getPlatformDocTypeFileExtRelation());
		}catch (Exception e){

			log.trace("Data perfact verification during add for platformDocTypeFileExtRelation has been tested successfully");
		}
		try{
			CephFileMetaInfoRecord.updatePlatformDocTypeFileExtRelation(getPlatformDocTypeFileExtRelation());
		}catch (Exception e){

			log.trace("Data perfact verification during add for platformDocTypeFileExtRelation has been tested successfully");
		}

		log.trace("platformDocTypeFileExtRelation_UnitTest finished successfully ");
	}

	//test AppPlatformDocTypeRelation
	@Ignore
	public void UnitTest_04_AppPlatformDocTypeRelation() throws Exception{

		//add
		log.trace("addAppPlatformDocTypeRelation test started");
		getAppPlatformDocTypeRelation().setDocTypeId(getPlatformDocType().getDocTypeId());
		getAppPlatformDocTypeRelation().setDownloadFlag(1);
		getAppPlatformDocTypeRelation().setUploadFlag(1);
		getAppPlatformDocTypeRelation().setDeleteFlag(1);
		Assert.assertTrue("addAppPlatformDocTypeRelation test Failed",CephFileMetaInfoRecord.addAppPlatformDocTypeRelation(getAppPlatformDocTypeRelation())!=null);

		//update
		log.trace("updateAppPlatformDocTypeRelation test started");
		getAppPlatformDocTypeRelation().setDownloadFlag(0);
		getAppPlatformDocTypeRelation().setUploadFlag(0);
		getAppPlatformDocTypeRelation().setDeleteFlag(0);
		Assert.assertTrue("updateAppPlatformDocTypeRelation test Failed",CephFileMetaInfoRecord.updateAppPlatformDocTypeRelation(getAppPlatformDocTypeRelation())!=null);

		//select
		log.trace("select AppPlatformDocTypeRelation test started");
		Assert.assertTrue("select AppPlatformDocTypeRelation test Failed",CephFileMetaInfoRecord.getAllAppPlatformDocTypeRelation().size()>0);

	}
	//test documentList
	@Ignore
	public void UnitTest_05_documentList() throws Exception{

		//add
		log.trace("addDocumentList test started");
		getDocumentList().setUploadedByPlatformUserGuid(getUserGuid());
		getDocumentList().setLastModifiedByPlatformUserGuid(getUserGuid());
		getDocumentList().setDocTypeId(getPlatformDocType().getDocTypeId());
		getDocumentList().setPlatformfileExtID(getplatformFileExt().getFileExtID());
		getDocumentList().setFileName("first test file");
		getDocumentList().setFilePath("/mnt");
		getDocumentList().setFileSize(10);
		getDocumentList().setMemorySeaLevelTime(new Date());
		getDocumentList().setMemoryHitTimes((long)2);
		getDocumentList().setMemoryLiveTimeSec((long)100);
		getDocumentList().setMemoryPerHitComeUpSeconds((long)15);
		getDocumentList().setStorageClusterType(0);
		getDocumentList().setVersion(CommonHelper.getInstance().getNodeHashCode(getDocumentList().getFileName()));

		Assert.assertTrue("addDocumentList test failed",CephFileMetaInfoRecord.addDocumentList(getDocumentList())!=null);

		//duplicate add test
		log.trace("duplicate add DocumentList test started");
		Thread.sleep(10);
		getDocumentList().setDocGuid(CommonHelper.getInstance().getNodeUUID());//模拟其它流量发送新建文件请求时遇到重名
		getDocumentList().setDocTypeId(getPlatformDocType().getDocTypeId());
		getDocumentList().setPlatformfileExtID(getplatformFileExt().getFileExtID());
		Assert.assertTrue("duplicate add DocumentList test failed",CephFileMetaInfoRecord.addDocumentList(getDocumentList())!=null);

       //update
		log.trace("updateDocumentList test started");
		getDocumentList().setLastModifiedByPlatformUserGuid(UUID.randomUUID().toString().replace("-",""));
		getDocumentList().setFileSize(15);
		Assert.assertTrue("updateDocumentList test failed",CephFileMetaInfoRecord.updateDocumentList(getDocumentList())!=null);


		//duplicate update test
		log.trace("duplicate update DocumentList test started");
		getDocumentList().setLastModifiedByPlatformUserGuid(UUID.randomUUID().toString().replace("-",""));
		getDocumentList().setFileSize(16);
		getDocumentList().setDocTypeId(getDocumentList().getDocTypeId());
		getDocumentList().setPlatformfileExtID(getDocumentList().getPlatformfileExtID());
		Assert.assertTrue("duplicate update DocumentList  test failed",CephFileMetaInfoRecord.updateDocumentList(getDocumentList())!=null);
		//select
		log.trace("select DocumentList  test started");
		Assert.assertTrue("select DocumentList test failed",CephFileMetaInfoRecord.getAllDocumentList().size()%2==0);


	}

	//test RequestLog
	@Ignore
	public void UnitTest_06_RequestLog() throws Exception{
		//add
		log.trace("addRequestLog test started");
		getRequestLog().setUserGuid(getUserGuid());
		getRequestLog().setAppGuid(CommonHelper.getInstance().getNodeUUID());
		getRequestLog().setAppName("testBoyApp1");
		getRequestLog().setReqStorageClusterType(0);
		getRequestLog().setUrl("/testUrl/FileEntry");

		Assert.assertTrue("addRequestLog test failed",CephFileMetaInfoRecord.addRequestLog(getRequestLog())!=null);

		log.trace("addRequestLog test successfully");
		//update
		log.trace("updateRequestLog test started");
		getRequestLog().setAppName("testBoyApp2");
		Assert.assertTrue("updateRequestLog test failed",CephFileMetaInfoRecord.updateRequestLog(getRequestLog())!=null);

		log.trace("updateRequestLog test successfully");
		//select
		log.trace("select RequestLog test started");
		Assert.assertTrue("select RequestLog test failed",CephFileMetaInfoRecord.getRequestLogList().size()>0);
		log.trace("select RequestLog test successfully");
	}
	//test operationLog
	@Ignore
	public void UnitTest_07_OperationLog() throws Exception{
		//add
		log.trace("addOperationLog test started");
		getOperationLog().setAppId((long)CommonHelper.getInstance().getNodeRandomNum(1,13579));
		getOperationLog().setBizUserRoleId((long)1);
        getOperationLog().setDeviceId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setDevLangId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setDocId(getDocumentList().getDocId());
		getOperationLog().setExfModuleId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setIsActive(true);
		getOperationLog().setIsDocExistingCurrently(true);
		getOperationLog().setOperationLogTime(new Date());
		getOperationLog().setAppGuid(CommonHelper.getInstance().getNodeUUID());
		getOperationLog().setOperationStorageClusterType(0);
		getOperationLog().setOperationType(0);
		getOperationLog().setPlatformActionId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setPlatformControllerId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setUserGuid(getUserGuid());
		getOperationLog().setUserId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setUserName("boytester1");
		getOperationLog().setUsingObjectId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setViewId((long)CommonHelper.getInstance().getNodeRandomNum(1,100));
		getOperationLog().setWorkFlowStatusId((long)CommonHelper.getInstance().getNodeRandomNum(1,200));

		Assert.assertTrue("addOperationLog test failed",CephFileMetaInfoRecord.addOperationLog(getOperationLog())!=null);
		log.trace("addOperationLog test finished");
		//update
		log.trace("updateOperationLog test started");
		getOperationLog().setUserName("boytester2");
		Assert.assertTrue("updateOperationLog test failed",CephFileMetaInfoRecord.updateOperationLog(getOperationLog())!=null);
		log.trace("updateOperationLog test finished");
		//select
		log.trace("select OperationLog test started");
		Assert.assertTrue("select OperationLog  test failed",CephFileMetaInfoRecord.getOperationLogList().size()>0);
		log.trace("select OperationLog test finished");
	}

	//test DBUpgradeHistory
	@Ignore
	public void UnitTest_08_DBUpgradeHistory() throws Exception{
		//add
		log.trace("add DBUpgradeHistory test started");
		getDBUpgradeHistory().setComments("test db upgrade Histroy");
		getDBUpgradeHistory().setCurrentPlatformUser((long)CommonHelper.getInstance().getNodeRandomNum(1,20488));
		getDBUpgradeHistory().setFromPlatformDbVersion((long)CommonHelper.getInstance().getNodeRandomNum(1,10488));
		getDBUpgradeHistory().setIsActive(true);
		getDBUpgradeHistory().setNodeDbGuid(CommonHelper.getInstance().getNodeUUID());
		getDBUpgradeHistory().setNodeDbName("BoyNodeTest");
		getDBUpgradeHistory().setPlatformHostGuid(CommonHelper.getInstance().getNodeUUID());
		getDBUpgradeHistory().setPlatformUserLoginName("boy");
		getDBUpgradeHistory().setPlatformUserName("boyboyboy");
		getDBUpgradeHistory().setToPlatformDbVersion((long)CommonHelper.getInstance().getNodeRandomNum(1,7248));
		getDBUpgradeHistory().setUpgradeTime(new Date());

		Assert.assertTrue("add DBUpgradeHistory test failed",platformOps_DBUpgradeHistory.add(getDBUpgradeHistory())!=null);
		log.trace("add DBUpgradeHistory test successfully");
		//update
		log.trace("update DBUpgradeHistory test started");
		getDBUpgradeHistory().setNodeDbName("BoyNodeTest321");
		Assert.assertTrue("add DBUpgradeHistory test failed",platformOps_DBUpgradeHistory.update(getDBUpgradeHistory())!=null);
		log.trace("update DBUpgradeHistory test successfully");

		//select
		log.trace("select DBUpgradeHistory test started");
		Assert.assertTrue("select DBUpgradeHistory test failed",platformOps_DBUpgradeHistory.getAll().size()>0);
		log.trace("select DBUpgradeHistory test successfully");
	}

	//test unitNodeRelation
	@Ignore
	public void UnitTest_09_unitNodeRelation() throws Exception{
		//add
		log.trace("add unitNodeRelation test started");
		getUnitNodeRelation().setUnitNodeRelationGuid(CommonHelper.getInstance().getNodeUUID());
		getUnitNodeRelation().setAppId((long) CommonHelper.getInstance().getNodeRandomNum(1,2304));
		getUnitNodeRelation().setCreateTime(new Date());
		getUnitNodeRelation().setIsActive(true);
		getUnitNodeRelation().setUnitNodeRelationId((long) CommonHelper.getInstance().getNodeRandomNum(1,23450));
		Assert.assertTrue("add unitNodeRelation test failed",platformOps_unitNodeRelation.add(getUnitNodeRelation())!=null);
		log.trace("add unitNodeRelation test successfully");
		//update
		log.trace("update unitNodeRelation test started");
		getUnitNodeRelation().setUnitNodeRelationId((long) CommonHelper.getInstance().getNodeRandomNum(1,100));
		Assert.assertTrue("update unitNodeRelation test failed",platformOps_unitNodeRelation.update(getUnitNodeRelation())!=null);
		log.trace("update unitNodeRelation test successfully");

		//select
		log.trace("select unitNodeRelation test started");
		Assert.assertTrue("select unitNodeRelation test failed",platformOps_unitNodeRelation.getAll().size()>0);
		log.trace("select unitNodeRelation test successfully");
	}

	//test customerDbList
	@Ignore
	public void UnitTest_10_customerDbList() throws Exception{
		//add
		log.trace("add customerDbList test started");
        getCustomerDbList().setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		getCustomerDbList().setDataSourceDataBaseName("board_diskFile");
		getCustomerDbList().setDataSourcePassword("whoisboy");
		getCustomerDbList().setDataSourcePortNumber((long)3306);
		getCustomerDbList().setDataSourceServerName("127.0.0.1");
		getCustomerDbList().setDataSourceUser("root");
		getCustomerDbList().setDbTypeNum((long)1);
		getCustomerDbList().setIsActive(true);
		getCustomerDbList().setRemark("remark from third party db platform");
		getCustomerDbList().setCreateTime(new Date());
        Assert.assertTrue("add customerDbList test failed!",platformOps_CustomerDbList.add(getCustomerDbList())!=null);
		log.trace("add customerDbList test successfully");

		//update
		/*log.trace("update customerDbList test started");
		getCustomerDbList().setDataSourceServerName("127.0.0.2");
		Assert.assertTrue("update customerDbList test failed!",platformOps_CustomerDbList.update(getCustomerDbList())!=null);
		log.trace("update customerDbList test successfully");
		//select
		log.trace("select customerDbList test started");
		Assert.assertTrue("select customerDbList test failed!",platformOps_CustomerDbList.getAll().size()>0);
		log.trace("select customerDbList test successfully");*/
	}


	//test ceph interface General basic function
	@Ignore
	public void UnitTest_11_CephInterfaceGeneral() throws  Exception{
      //初始化API测试
		File f1 = new File("D:\\WorkSpace\\The5\\UT\\Boy1.0\\Board\\diskFile\\uploadTestFile1.txt");
		File f2 = new File("D:\\WorkSpace\\The5\\UT\\Boy1.0\\Board\\diskFile\\uploadTestFile2.txt");
		String testUrl="http://192.168.125.129:8080";
		String currentAPIRoute="/DiskFile/CephEntry/";
		String cmd="";

		//配置注入(inject ceph config)
		log.trace("inject ceph config test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			JSONObject jsob=new JSONObject();
			jsob.put("username","admin"); //
			jsob.put("monIp","192.168.125.128:6789;192.168.125.129:6789;192.168.125.130:6789");
			jsob.put("userKey","AQDyuF1dpxsnExAAAIkIlT3m7gcuhgvW+aPmiw==");
			jsob.put("mountPath","/");


			StringEntity data=new StringEntity(jsob.toString(),"utf-8");
			data.setContentEncoding("utf-8");
			data.setContentType("application/json");



           cmd="mount";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status+response.toString());
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}
		log.trace("inject ceph config test sucessfully");


		//查看目录列表 (check ceph dir)
		log.trace("check ceph dir test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


  		    HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("dirPath","/")
					.build();

			cmd="listdir";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}




		log.trace("check ceph dir test sucessfully");

		//新建目录 (create new ceph dir)
		log.trace("create new ceph dir test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


			HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("dirPath","/newDirTest1")
					.build();

			cmd="mkdir";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


			HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("dirPath","/newDirTest2")
					.build();

			cmd="mkdir";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}

		log.trace("create new ceph dir test sucessfully");

		//删除目录 (delete ceph dir)
		log.trace("delete ceph dir test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


			HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("dirPath","/newDirTest2")
					.build();

			cmd="deldir";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}

		log.trace("delete ceph dir test sucessfully");

		//获取文件的状态(Get file status)
		log.trace("Get file status test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


			HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("dirPath","/newDirTest1")
					.build();

			cmd="stat";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}

		log.trace("Get file status test sucessfully");

		//重命名目录or文件 (rename ceph dir or file)
		log.trace("rename ceph dir or file test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


			HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("oldPath","/newDirTest1")
					.addTextBody("newPath","/newDirTest2")
					.build();

			cmd="rename";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}
		log.trace("rename ceph dir or file test sucessfully");

		//set current dir (work dir)
		log.trace("set current dir test started");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


			HttpEntity data= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody("dirPath","/newDirTest2")
					.build();

			cmd="cd";

			HttpUriRequest request = RequestBuilder
					.post(testUrl+currentAPIRoute+cmd)
					.setEntity(data)
					.build();

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					System.out.println(status);
					if (status == 200) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpClient.execute(request, responseHandler);
			System.out.println(responseBody);
		}

		log.trace("set current dir test  sucessfully");


		//Verify dir or file under current path

		//Verify dir or file for current path


		//外部获取mount(get out side mount)

		//umount
		log.trace("umount test  start");
		Map<String,Object> parameterMap=new Hashtable<String,Object>();
		cmd="umount";
		String postUrl=testUrl+currentAPIRoute+cmd;
		String result=DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.json.toString());
		System.out.println(result);
		parameterMap.clear();
		log.trace("umount test  sucessfully");
	}

	//test ceph interface file write(upload) function
	@Ignore
	public void UnitTest_12_CephInterfaceWrite() throws  Exception{

		String testUrl="http://192.168.125.129:8080";
		String currentAPIRoute="/DiskFile/CephEntry/";
		String cmd="";

		Map<String,Object> parameterMap=new Hashtable<String,Object>();
		parameterMap.put("username","admin");
		parameterMap.put("monIp","192.168.125.128:6789;192.168.125.129:6789;192.168.125.130:6789");
		parameterMap.put("userKey","AQDyuF1dpxsnExAAAIkIlT3m7gcuhgvW+aPmiw==");
		parameterMap.put("mountPath","/");
		cmd="mount";
		String postUrl=testUrl+currentAPIRoute+cmd;
		String result1=DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.json.toString());
		parameterMap.clear();

		log.trace(result1);


		List<File> uploadFileList=new ArrayList<File>();
		uploadFileList.add(new File("D:\\WorkSpace\\The5\\UT\\Boy1.0\\Board\\diskFile\\uploadTestFile1.txt"));
		uploadFileList.add(new File("D:\\WorkSpace\\The5\\UT\\Boy1.0\\Board\\diskFile\\uploadTestFile2.txt"));
	    parameterMap=new Hashtable<String,Object>();

		parameterMap.put("userGuid",getUserGuid());
		parameterMap.put("docTypeId",getPlatformDocType().getDocTypeId());
		parameterMap.put("platformfileExtID",getplatformFileExt().getFileExtID());
		parameterMap.put("isBig",0);//数字0，1直接可转boolean，告诉接口是否为大文件传输缓存
		parameterMap.put("inputBufferFiles",uploadFileList);

		cmd="vim";
		postUrl=testUrl+currentAPIRoute+cmd;
		String result2=DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.binaryBody.toString());
		log.trace(result2);

	}
    //test ceph interface file ls function
	@Ignore
	public void UnitTest_13_CephInterfaceLs() throws  Exception{
		String testUrl="http://192.168.125.129:8080";
		String currentAPIRoute="/DiskFile/CephEntry/";
		String cmd="";

		Map<String,Object> parameterMap=new Hashtable<String,Object>();
		parameterMap.put("username","admin");
		parameterMap.put("monIp","192.168.125.128:6789;192.168.125.129:6789;192.168.125.130:6789");
		parameterMap.put("userKey","AQDyuF1dpxsnExAAAIkIlT3m7gcuhgvW+aPmiw==");
		parameterMap.put("mountPath","/");
		cmd="mount";
		String postUrl=testUrl+currentAPIRoute+cmd;
		String result=DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.json.toString());
		parameterMap.clear();
		log.trace(result);

		cmd="ls";
		parameterMap.put("userGuid",getUserGuid());

		postUrl=testUrl+currentAPIRoute+cmd;
		result=DiskFileHttpHelper.getInstance().postRequest(postUrl,parameterMap,DiskFileHttpHelper.postQuestMode.textBody.toString());
		parameterMap.clear();
		log.trace(result);


	}
	//test ceph interface dir/file copy function

	//test ceph interface dir/file rename function


	//test ceph interface dir/file delete function

	//test ceph restful api
	@Test
	public void UnitTest_14_mgr_dump() throws  Exception{

		String url="http://192.168.125.130:5000/api/v0.1/mgr/dump";
		Map<String,Object> parameterMap=new Hashtable<>();
		String result=DiskFileHttpHelper.getInstance().getRequest(url,parameterMap,"queryCephApiUrl");

		log.trace(result);

	}

	@AfterClass
	public static void End(){

	}

}
