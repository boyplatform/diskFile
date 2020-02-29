package cn.Boy.DiskFile.distributeFileEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.Boy.DiskFile.pojo.CustomerDbList;
import cn.Boy.DiskFile.pojo.UnitNodeRelation;
import cn.Boy.DiskFile.pojo.DBUpgradeHistory;

//之后改成hdfs的jar
import com.ceph.fs.CephFileExtent;
import com.ceph.fs.CephMount;
import com.ceph.fs.CephStat;

import java.io.IOException;

@Service("HDFSFileOperator")
public class HDFSFileOperator extends AbsCommonFileOperator<CephMount,CephStat> {


    public HDFSFileOperator(){

    }

}
