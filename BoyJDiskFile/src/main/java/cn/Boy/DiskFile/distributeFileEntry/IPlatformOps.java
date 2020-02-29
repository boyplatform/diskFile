package cn.Boy.DiskFile.distributeFileEntry;

import java.util.List;

public interface IPlatformOps<T> {

    //For DBUpgradeHistory //For unitNodeRelation //For unitNodeRelation
    public T add(T typeObj);
    public T update(T typeObj);
    public boolean delete(int id);
    public boolean recover(int id);
    public List<T> getAll();
    public T getOneById(int id);


}
