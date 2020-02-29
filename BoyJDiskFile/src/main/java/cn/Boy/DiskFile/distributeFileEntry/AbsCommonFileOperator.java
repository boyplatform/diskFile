package cn.Boy.DiskFile.distributeFileEntry;

//import com.ceph.fs.CephMount;
//import com.ceph.fs.CephStat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class AbsCommonFileOperator<T,T2> {

    //config setting
    public boolean mount(String username, String monIp, String userKey, String mountPath) {

        return  true;
    }
    //Check Dir list
    public String[] listDir(String path) throws IOException{

        return  null;
    }
    //Make Dir
    public String[] mkDir(String path) throws IOException{
        return  null;
    }
    //Del Dir
    public String[] delDir(String path) throws IOException{


        return  null;
    }

    //Get file status
    public T2 getFileStatByPath(String path){

        return null;
    }
    //Rename Dir or file
    public String[] renameDirOrFile(String oldName, String newName){

        return  null;
    }
    public String[] renameDirOrFile(String oldName, String newName,String dirPath){

        return null;
    }
    //Del file
    public String[]  delFile(String path){

        return  null;
    }
    public String[]  delFile(String path,String dirPath){

        return  null;
    }
    //Read file
    public byte[] readFile(String path){

        return null;
    }
    //copy file
    public void copyFile(String sourceFile, String targetFile){

    }
    //write file
    public void writeFileWithLseek(String path, String fileName, long offset, int type, byte[] buf){

    }

    //Verify dir or file for current path
    public String fileOrDir(String Path){


        return  "";
    }

    //Verify dir or file under current path
    public List<String> listFileOrDir(String Path){

        return  null;
    }

    //set current dir (work dir)
    public void setWorkDir(String path) throws IOException{

    }

    //get mount from outside
    public T getMount(){

        return null;
    }

    //umount
    public void umount(){

    }

    //writeToCluster
    public boolean writeToCluster(String userFileFolderPath, String fileName, FileInputStream fis, long userFileLength) throws IOException{

        return  true;
    }

    //seekFromCluster
    public boolean seekFromCluster(String userFileFolderPath, String fileName, FileOutputStream fos) throws IOException{

        return  true;
    }


}
