package cn.Boy.DiskFile.distributeFileEntry;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import cn.Boy.DiskFile.pojo.UnitNodeRelation;
import cn.Boy.DiskFile.dao.UnitNodeRelationDao;

@Service("UnitNodeRelationOps")
public class UnitNodeRelationOps implements IPlatformOps<UnitNodeRelation> {

    private final Log log = LogFactory.getLog(UnitNodeRelationOps.class);
    @Autowired private UnitNodeRelationDao unitNodeRelationDao;

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public UnitNodeRelation add(UnitNodeRelation unitNodeRelation){
        if(log.isTraceEnabled()) {
            log.trace("add started for " + unitNodeRelation);
        }

        if( unitNodeRelationDao.insert(unitNodeRelation)<=0) {
            throw new RuntimeException("Add unitNodeRelation failed!");
        }
        return unitNodeRelation;
    }

    public UnitNodeRelation update(UnitNodeRelation unitNodeRelation) {
        if(log.isTraceEnabled()) {
            log.trace("update started for " + unitNodeRelation);
        }
        if(unitNodeRelationDao.update(unitNodeRelation)<=0){
            throw new RuntimeException("Update unitNodeRelation failed!");
        }
        return unitNodeRelation;
    }


    public boolean delete(int unitNodeId) {
        if(unitNodeRelationDao.setToNotActive(unitNodeId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recover(int unitNodeId) {
        if(unitNodeRelationDao.setToActive(unitNodeId)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<UnitNodeRelation> getAll(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAll started  ");
            }
            List<UnitNodeRelation> list = unitNodeRelationDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAll failed  ");
            }
            return null;
        }
    }

    public UnitNodeRelation getOneById(int unitNodeId){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOneById started  ");
            }
            UnitNodeRelation unitNodeRelation = unitNodeRelationDao.getOneById(unitNodeId);

            return unitNodeRelation;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOneById failed  ");
            }
            return null;
        }
    }
}
