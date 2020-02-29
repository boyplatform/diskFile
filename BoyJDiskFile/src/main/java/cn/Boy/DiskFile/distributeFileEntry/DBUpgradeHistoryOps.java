package cn.Boy.DiskFile.distributeFileEntry;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import cn.Boy.DiskFile.pojo.DBUpgradeHistory;
import cn.Boy.DiskFile.dao.DBUpgradeHistoryDao;

@Service("DBUpgradeHistoryOps")
public class DBUpgradeHistoryOps implements IPlatformOps<DBUpgradeHistory>{

    private final Log log = LogFactory.getLog(DBUpgradeHistoryOps.class);
    @Autowired private DBUpgradeHistoryDao dBUpgradeHistoryDao;

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public DBUpgradeHistory add(DBUpgradeHistory dBUpgradeHistory){
        if(log.isTraceEnabled()) {
            log.trace("add started for " + dBUpgradeHistory);
        }

        if(dBUpgradeHistoryDao.insert(dBUpgradeHistory)<=0) {
            throw new RuntimeException("Add Node dBUpgradeHistory failed!");
        }
        return dBUpgradeHistory;
    }

    public DBUpgradeHistory update(DBUpgradeHistory dBUpgradeHistory) {
        if(log.isTraceEnabled()) {
            log.trace("update started for " + dBUpgradeHistory);
        }
        if(dBUpgradeHistoryDao.update(dBUpgradeHistory)<=0){
            throw new RuntimeException("update Node dBUpgradeHistory failed!");
        }
        return dBUpgradeHistory;
    }


    public boolean delete(int nodeDbUpgradeHIstoryId) {
        if(dBUpgradeHistoryDao.setToNotActive(nodeDbUpgradeHIstoryId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recover(int nodeDbUpgradeHIstoryId) {
        if(dBUpgradeHistoryDao.setToActive(nodeDbUpgradeHIstoryId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<DBUpgradeHistory> getAll(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAll started  ");
            }
            List<DBUpgradeHistory> list = dBUpgradeHistoryDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAll failed  ");
            }
            return null;
        }
    }

    public DBUpgradeHistory getOneById(int nodeDbUpgradeHIstoryId){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOneById started  ");
            }
            DBUpgradeHistory dBUpgradeHistory = dBUpgradeHistoryDao.getOneById(nodeDbUpgradeHIstoryId);

            return dBUpgradeHistory;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOneById failed  ");
            }
            return null;
        }
    }

}
