package cn.Boy.DiskFile.distributeFileEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import cn.Boy.DiskFile.pojo.CustomerDbList;
import cn.Boy.DiskFile.dao.CustomerDbListDao;

@Service("CustomerDbListOps")
public class CustomerDbListOps implements IPlatformOps<CustomerDbList> {

    private final Log log = LogFactory.getLog(CustomerDbListOps.class);
    @Autowired private CustomerDbListDao customerDbListDao;

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public CustomerDbList add(CustomerDbList customerDbList){
        if(log.isTraceEnabled()) {
            log.trace("add started for " + customerDbList);
        }

        if(customerDbListDao.insert(customerDbList)<=0) {
            throw new RuntimeException("Add customerDbList failed");
        }
        return customerDbList;
    }

    public CustomerDbList update(CustomerDbList customerDbList) {
        if(log.isTraceEnabled()) {
            log.trace("update started for " + customerDbList);
        }
        if(customerDbListDao.update(customerDbList)<=0){
            throw new RuntimeException("update customerDbList failed");
        }
        return customerDbList;
    }


    public boolean delete(int id) {
        if(customerDbListDao.setToNotActive(id)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    public boolean recover(int id) {
        if(customerDbListDao.setToActive(id)>0)
        {
            return true;
        }else{
            return false;
        }
    }

    @Transactional(readOnly=true)
    public List<CustomerDbList> getAll(){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getAll started  ");
            }
            List<CustomerDbList> list = customerDbListDao.getAll();

            return list;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getAll failed  ");
            }
            return null;
        }
    }

    public CustomerDbList getOneById(int id){
        try {
            Thread.sleep(10);
            if(log.isTraceEnabled()) {
                log.trace("getOneById started  ");
            }
            CustomerDbList customerDbList = customerDbListDao.getOneById(id);

            return customerDbList;
        }catch(Exception e) {

            if(log.isTraceEnabled()) {
                log.trace("getOneById failed  ");
            }
            return null;
        }
    }



}
