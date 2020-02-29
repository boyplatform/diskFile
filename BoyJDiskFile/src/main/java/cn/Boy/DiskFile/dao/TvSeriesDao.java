package cn.Boy.DiskFile.dao;

import java.util.List;

import cn.Boy.DiskFile.pojo.TvSeries;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface TvSeriesDao {
    
    @Select("select * from tv_series where id=#{id}")
    public TvSeries getOneById(int id);
    
    @Select("select * from tv_series where status=0")
    public List<TvSeries> getAll();
    
    public int update(TvSeries tvSeries);
    public int insert(TvSeries tvSeries);
    
    @Delete("delete from tv_series where id=#{id}")
    public int delete(int id);
    
    @Update("update tv_series set status=-1, reason=#{reason} where id=#{id}")
    public int logicDelete(int id, String reason);
}
