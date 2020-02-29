package cn.Boy.DiskFile.common;

public class CommonEnums {

    private static CommonEnums instance = new CommonEnums();
    public static CommonEnums getInstance(){
        return instance;
    }

    public enum StorageClusterType{

        ceph(0), hdfs(1);
        private int clusterType;
        StorageClusterType(int typeNum){
            this.clusterType=typeNum;
        }
        public int getClusterType(){
            return  clusterType;
        }
    }

    public  enum  ClusterStorageStatus{
        didnotsaved(0), saved(1),pendingSaved(2);
        private int clusterStorageStatus;
        ClusterStorageStatus(int StatusNum){
            this.clusterStorageStatus=StatusNum;
        }
        public int getClusterStorageStatus(){
            return  clusterStorageStatus;
        }
    }

}
