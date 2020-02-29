package cn.Boy.DiskFile.distributeFileEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import cn.Boy.DiskFile.pojo.CustomerDbList;
import cn.Boy.DiskFile.pojo.UnitNodeRelation;
import cn.Boy.DiskFile.pojo.DBUpgradeHistory;

import com.ceph.fs.CephMount;
import com.ceph.fs.CephStat;


@Service("CephFileOperator")
public class CephFileOperator extends AbsCommonFileOperator<CephMount,CephStat> {

    @Autowired
    @Qualifier("DBUpgradeHistoryOps")
    private IPlatformOps<DBUpgradeHistory> PlatformOps_DBUpgradeHistory;

    @Autowired
    @Qualifier("UnitNodeRelationOps") private IPlatformOps<UnitNodeRelation> PlatformOps_UnitNodeRelation;

    @Autowired
    @Qualifier("CustomerDbListOps") private IPlatformOps<CustomerDbList> PlatformOps_CustomerDbList;

    private final Log log = LogFactory.getLog(CephFileOperator.class);

    private static CephFileOperator instance = null;
    public static CephFileOperator getInstance(){
        if(instance==null)
        {
            instance = new CephFileOperator();
        }
        return instance;
    }

    public  CephFileOperator()
    {

    }


    private CephMount mount;
    private String username;
    private String monIp;
    private String userKey;
    private String mountPath;





    //ceph 配置注入(inject ceph config)
    public boolean mount(String _monIp, String _username, String _userKey, String _mountPath)
    {
        try
        {
                this.username = _username;
                this.monIp = _monIp;
                this.userKey = _userKey;
                if (username == "") {
                    this.mount = new CephMount();
                } else {
                    this.mount = new CephMount(username);
                }
                this.mount.conf_set("mon_host", monIp);
                mount.conf_set("key", userKey);
                //log.trace("before monut");
                mount.mount(_mountPath);
                //log.trace("after monut");
                this.mountPath = _mountPath;
            return true;
        }
        catch (Exception e)
        {
            log.trace(e.toString());
            return false;
        }
    }

    //查看目录列表 (check ceph dir)
    public String[] listDir(String path) throws IOException {
        String[] dirs = mount.listdir(path);
        System.out.println("contents of the dir: " + Arrays.asList(dirs));
        //log.trace("contents of the dir: " + Arrays.asList(dirs));
        return dirs;
    }

    //新建目录 (create new ceph dir)
    public String[] mkDir(String path) throws IOException {
        String[] dirList=null;

        if(mount==null){
            return null;
        }
         mount.mkdirs(path, 0755);//0表示十进制
        dirList=mount.listdir(mountPath);
        return dirList;

    }

    //删除目录 (delete ceph dir)
    public String[] delDir(String path) throws IOException {
        String[] dirList=null;

        if(mount==null){
            return  null;
        }
        mount.rmdir(path);
        dirList=mount.listdir(mountPath);
        return dirList;

    }

    //获取文件的状态(Get file status)
    public CephStat getFileStatByPath(String path){
        CephStat stat=new CephStat();
        try{
            if(mount==null){
                return  null;
            }
            mount.lstat(path,stat);
            return stat;

        }catch (Exception e){
            log.trace(e.getMessage());
            return null;
        }

    }

    //重命名目录or文件 (rename ceph dir or file)
    public String[] renameDirOrFile(String oldName, String newName){
        try {
            String[] dirList = null;

            if (mount == null) {
                return null;
            }
            mount.rename(oldName, newName);

            dirList = mount.listdir(mountPath);
            return dirList;

        }catch (Exception e){

            log.trace(e.getMessage());
            return null;
        }
    }
    public String[] renameDirOrFile(String oldName, String newName,String dirPath){
        try {
            String[] dirList = null;

            if (mount == null) {
                return null;
            }
            mount.rename(oldName, newName);

            dirList = mount.listdir(dirPath);
            return dirList;

        }catch (Exception e){

            log.trace(e.getMessage());
            return null;
        }
    }

    //删除文件 (delete file)
    public String[]  delFile(String path) {
        try {
            String[] dirList = null;

            if (mount == null) {
                return null;
            }
            mount.unlink(path);

            dirList = mount.listdir(mountPath);
            return dirList;

        }catch (Exception e){

            log.trace(e.getMessage());
            return null;
        }
    }
    public String[]  delFile(String path,String dirPath) {
        try {
            String[] dirList = null;

            if (mount == null) {
                return null;
            }
            mount.unlink(path);

            dirList = mount.listdir(dirPath);
            return dirList;

        }catch (Exception e){

            log.trace(e.getMessage());
            return null;
        }
    }

    //读文件 (read file)
    public byte[] readFile(String path) {
        CephStat stat=new CephStat();
        //String context=null;
        try{
            if(mount==null){
                return null;
            }
            int fd=mount.open(path, CephMount.O_RDONLY,0);
            mount.fstat(fd,stat);
            byte[] buffer=new byte[(int)stat.size];
            mount.read(fd,buffer,stat.size,0);
            //context=new String(buffer);
            mount.close(fd);
            return buffer;
        }catch (Exception e){
            log.error(e.getStackTrace());
        }
        return  null;


    }

    //复制文件 (copy file)
    public void copyFile(String sourceFile, String targetFile){
        System.out.println("copy:start write file...");
        int readFD = -1, createAA = -1, writeFD = -1;
        try{
            readFD = mount.open(sourceFile, CephMount.O_RDWR, 0755);
            writeFD = mount.open(targetFile, CephMount.O_RDWR | CephMount.O_CREAT, 0644);
//                createAA = mountLucy.open("aa.txt", CephMount.O_RDWR | CephMount.O_CREAT | CephMount.O_EXCL, 0644);//若文件已有， 会异常
            System.out.println("file read fd is : " + readFD);

            byte[] buf = new byte[1024];
            long size = 10;
            long offset = 0;
            long count = 0;
            while((count = mount.read(readFD, buf, size, -1)) > 0){
                mount.write(writeFD, buf, count, -1);//-1指针跟着走，若取值count，指针不动
                System.out.println("offset: " + offset);
                offset += count;
                System.out.println("writeFD position : " + mount.lseek(writeFD, 0, CephMount.SEEK_CUR));
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(readFD > 0){
                mount.close(readFD);
            }
            if(writeFD > 0){
                mount.close(writeFD);
            }
        }
    }

    //写入文件 (write file)
    public void writeFileWithLseek(String path,String fileName, long offset, int type,byte[] buf){
        if(type <= 0){
            type =CephMount.SEEK_CUR;
        }
        System.out.println("write:start write file...");
        int writeFD = -1;
        try{
            //判断文件是否存在，如果不存在，创建一个新的文件
            if(isFileExist(path,fileName)==false){
               mount.open(path+ File.separatorChar+fileName,CephMount.O_CREAT,0);
            }
            writeFD = mount.open(path+ File.separatorChar+fileName, CephMount.O_RDWR | CephMount.O_APPEND, 0644);
            long pos = mount.lseek(writeFD, offset, type);
            System.out.println("pos : " + pos);

            mount.write(writeFD, buf, buf.length, pos);

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(writeFD > 0){
                mount.close(writeFD);
            }
        }
    }

    //Verify dir or file under current path--返回指定目录/文件路径的状态
    public List<String> listFileOrDir(String Path){
        int writeFD = -1;
        List<String> result=new ArrayList<String>();
        try{
            String[] lucyDir = mount.listdir(Path);
            for(int i = 0; i < lucyDir.length; i++){
                CephStat cephStat = new CephStat();
                mount.lstat(lucyDir[i], cephStat);
                System.out.println(lucyDir[i] + " is dir?: " + cephStat.isDir()
                        + " is file?: " + cephStat.isFile()
                        + " size: " + cephStat.size
                        + " blksize: " + cephStat.blksize);//cephStat.size就是文件大小

                result.add(lucyDir[i] + " is dir?: " + cephStat.isDir()
                        + " is file?: " + cephStat.isFile()
                        + " size: " + cephStat.size
                        + " blksize: " + cephStat.blksize);
            }

            return result;

           /* writeFD = mount.open("lucy1.txt", CephMount.O_RDWR | CephMount.O_APPEND, 0644);
            CephFileExtent cephFileExtent = mount.get_file_extent(writeFD, 0);
            System.out.println("lucy1.txt size: " + cephFileExtent.getLength());//4M
            System.out.println("lucy1.txt stripe unit: " + mount.get_file_stripe_unit(writeFD));//4M
            long pos = mount.lseek(writeFD, 0, CephMount.SEEK_END);
            System.out.println("lucy1.txt true size: " + pos);//30Byte*/

        } catch (IOException e){
            log.error(e.getStackTrace());
            return result;
        } finally {
            if(writeFD > 0){
                mount.close(writeFD);
            }
        }
    }

    //Verify dir or file for current path--判断指定路径为目录还是文件
    public String fileOrDir(String Path){

           try{

               if(mount==null){
                   log.trace("error: mount is null");
                   return "error";
               }

               CephStat cephStat = new CephStat();
               mount.lstat(Path, cephStat);
               if(cephStat.isDir())
               {
                   log.trace("dir");
                   return "dir";
               }
               else if(cephStat.isFile())
               {
                   log.trace("file");
                   return "file";
               }else{
                   log.trace("null");
                   return "null";
               }

           }
           catch (FileNotFoundException e)
           {
               log.trace("there is no file on ceph cluster now.");
               return "null";
           }
           catch(IOException e){
               log.trace("error:"+e.toString());
               return "error";

           }catch (Exception e){

               log.trace(e.getMessage()+"mount:"+mount.toString());
               return "error";
           }
    }

    //set current dir (work dir)--移动到指定的工作目录
    public void setWorkDir(String path) throws IOException{
        mount.chdir(path);
    }


    //外部获取mount(get out side mount)
    public CephMount getMount(){
        return this.mount;
    }

    //umount--取消mount挂载
    public void umount(){
        mount.unmount();
        mount=null;
    }

    //判断文件是否存在
    private boolean isFileExist(String userFileFolder,String fileName) throws IOException{
            boolean fileExist = false;
            try {
                String[] dirList = mount.listdir(userFileFolder);
                for (String fileInfo : dirList) {
                    if (fileInfo.equals(fileName)){
                        fileExist = true;
                    }
                }
            }catch (FileNotFoundException e)
            {

                log.trace(e.getMessage()+e.getStackTrace());

            }finally
            {
                return  fileExist;
            }
    }

    //接收上传文件写入ceph集群存储
    public boolean writeToCluster(String userFileFolderPath, String fileName, FileInputStream fis,long userFileLength) throws  IOException{

        if(this.mount==null){
            log.trace("Ceph cluster has not been mount!");
            return false;
        }

        String cephFullFileName=userFileFolderPath + File.separatorChar + fileName;
        //判断文件在集群中是否存在
         if(isFileExist(userFileFolderPath,fileName)==false)
         {
             try {
                 //不存在，创建并写入系统
                 this.mount.open(cephFullFileName, CephMount.O_CREAT, 0);
                 int fd = this.mount.open(cephFullFileName, CephMount.O_RDWR, 0);
                 //begin to push to cluster
                 long uploadLength = 0l;
                 int length = 0;
                 byte[] bytes = new byte[1024];
                 while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                     //write to cluster
                     this.mount.write(fd, bytes, length, uploadLength);
                     //update written length
                     uploadLength += length;

                     //output written rate
                     float rate = (float) uploadLength * 100 / (float) userFileLength;
                     String rateValue = (int) rate + "%";
                     log.trace(rateValue + " has been written to Ceph cluster.");

                     //complete
                     if (uploadLength == userFileLength) {
                         break;
                     }
                 }
                 log.trace("one file named as:" + fileName + " has been written to Ceph cluster.");
                 //chmod
                 this.mount.fchmod(fd, 0666);
                 //close
                 this.mount.close(fd);
                 if (fis != null) {
                     fis.close();
                 }
                 return true;
             }catch (Exception e){

                   log.trace("File write to Ceph cluster failed!");
                   e.printStackTrace();
             }

         }else if(isFileExist(userFileFolderPath,fileName)) {
               //存在则非创建写入
             try {
                 CephStat stat = getFileStatByPath(cephFullFileName);
                 long uploadLength = stat.size;
                 int fd = this.mount.open(cephFullFileName, CephMount.O_RDWR, 0);

                //begin to push to cluster
                 int length=0;
                 byte[] bytes=new byte[1024];
                 fis.skip(uploadLength); //再次上传时，跳过重复部分字节，断点快传实现
                 while ((length=fis.read(bytes,0,bytes.length))!=-1){
                     //write to cluster
                     this.mount.write(fd,bytes,length,uploadLength);

                     //update length
                     uploadLength+=length;

                     //output written rate
                     float rate = (float) uploadLength * 100 / (float) userFileLength;
                     String rateValue = (int) rate + "%";
                     log.trace(rateValue + " has been written to Ceph cluster.");

                     //complete
                     if (uploadLength == userFileLength) {
                         break;
                     }
                     //chmod
                     this.mount.fchmod(fd, 0666);
                     //close
                     this.mount.close(fd);
                     if (fis != null){
                         fis.close();
                     }
                 }
                 log.trace("one file named as:" + fileName + " has been written to Ceph cluster continue with former stream.");
                 return  true;
             }catch (Exception e){
                 log.trace("File write to Ceph cluster failed!");
                 e.printStackTrace();
             }
         }else {
             try{
                 if(fis!=null){
                     fis.close();
                 }

             }catch (Exception e){
                 e.printStackTrace();
             }
             return  false;
         }
          return  false;
    }

    //从ceph集群存储获取一次文件
    public boolean seekFromCluster(String userFileFolderPath, String fileName, FileOutputStream fos) throws IOException{

        if(this.mount==null){
            log.trace("Ceph cluster has not been mount!");
            return false;
        }

        String cephFullFileName=userFileFolderPath + File.separatorChar + fileName;

        //获取文件在集群中的size
        long fileLength= getFileStatByPath(cephFullFileName)!=null? getFileStatByPath(cephFullFileName).size:0l;
        if(fileLength==0l){
            log.trace("Failed to get ceph cluster file size for your current file seek");
            return false;
        }

        //new一个本地缓存文件对象按照fullFileName
        File file=new File(cephFullFileName);
        RandomAccessFile raf=null; //定义一个续载对象
        long downloadLength=0l;
        if(!file.exists()){
            log.trace("point-a");
            int length=10240;
            byte[] bytes=new byte[length];
            try{
                int fd=this.mount.open(cephFullFileName, CephMount.O_RDONLY,0);
                fos=new FileOutputStream(file);
                float rate=0;
                String rateValue="";
                log.trace("point-a-1");
                while((fileLength-downloadLength)>=length &&(this.mount.read(fd,bytes,(long)length,downloadLength))!=-1){
                    fos.write(bytes,0,length);
                    fos.flush();
                    downloadLength+=(long)length;

                    //out put downloaded rate
                    rate=(float)downloadLength*100/(float)fileLength;
                    rateValue=(int)rate+"%";
                    log.trace(rateValue + " has been seeked from Ceph cluster.");

                    if(downloadLength==fileLength){
                        break;
                    }

                }

                //如果在下载的同时，有其它访问操作并发变更了文件，那么补1下子
                log.trace("point-a-2");
                if(downloadLength!=fileLength){
                    this.mount.read(fd,bytes,fileLength-downloadLength,downloadLength);
                    fos.write(bytes,0,(int)(fileLength-downloadLength));
                    fos.flush();
                    downloadLength=fileLength;

                    //out put downloaded rate
                    rate=(float)downloadLength*100/(float)fileLength;
                    rateValue=(int)rate+"%";
                    log.trace(rateValue + " has been seeked from Ceph cluster.");
                }

                log.trace("one file named as:" + fileName + " has been seeked from Ceph cluster once!");
                 fos.close();
                 this.mount.close(fd);
                 return  true;

            }catch (Exception e){
                log.trace("Failed to seek ceph cluster file once!");
                e.printStackTrace();
                return  false;
            }

        }else if(file.exists()){

            log.trace("point-b");
            int length=10240;
            byte[] bytes=new byte[length];
            long filePoint=file.length();//从已存在文件的偏移量，以便续载
            try{
                int fd=this.mount.open(cephFullFileName,CephMount.O_RDONLY,0);
                raf=new RandomAccessFile(file,"rw");
                raf.seek(filePoint);//为续载对象获取偏移量
                downloadLength=filePoint;
                float rate=0;
                String rateValue="";
                log.trace("point-b-1");
                while ((fileLength-downloadLength)>=length &&(this.mount.read(fd,bytes,(long)length,downloadLength))!=-1){
                    raf.write(bytes,0,length);
                    downloadLength+=(long)length;

                    //out put seeked rate
                    rate=(float)downloadLength*100/(float) fileLength;
                    rateValue=(int)rate+"%";
                    log.trace(rateValue + " has been seeked from Ceph cluster.");

                    if(downloadLength==fileLength){
                        break;
                    }
                }
                log.trace("point-b-2");
                if(downloadLength!=fileLength){
                    this.mount.read(fd,bytes,fileLength-downloadLength,downloadLength);
                    raf.write(bytes,0,(int)(fileLength-downloadLength));
                    downloadLength=fileLength;

                    //out put seeked rate
                    rate=(float)downloadLength*100/(float) fileLength;
                    rateValue=(int)rate+"%";
                    log.trace(rateValue + " has been seeked from Ceph cluster.");
                }

                log.trace("one file named as:" + fileName + " has been cut point seeked from Ceph cluster once!");
                raf.close();
                this.mount.close(fd);
                return  true;

            }catch (Exception e){
                log.trace("Failed to continue seek ceph cluster file once!");
                e.printStackTrace();
                return  false;
            }

        }else {

            log.trace("Unknow Error when you seek ceph cluster file! Please check your ceph cluster!");
            return  false;
        }


    }


}
