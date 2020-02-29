package cn.Boy.DiskFile.distributeFileEntry;

import cn.Boy.DiskFile.pojo.DocumentList;

import java.io.File;
import java.util.Date;

public interface IViewerFileCacheManager {

    //set viewerCache by viewerCacheFileName , viewerCacheTime and viewerCacheTimeLength
     public boolean setViewerFileCache(DocumentList documentList, String viewerCacheFileName, Date viewerCacheTime, long viewerCacheTimeLength, File viewerCacheFile);

     //get viewerCache by document
    public String getViewerFileCache(DocumentList documentList,long viewerCacheTimeLength);

    //Revoke viewerFileCache
    public void revokeExpiredViewerFileCache();

}
