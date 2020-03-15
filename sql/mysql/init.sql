CREATE DATABASE `board_diskfile` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

drop table if exists platformFileExt;

/*==============================================================*/
/* Table: platformFileExt                                       */
/*==============================================================*/
create table platformFileExt
(
   fileExtID            bigint not null auto_increment,
   fileExtGuid          varchar(255) not null,
   fileExtName          varchar(255) not null,
   createTime           datetime not null default '1970-1-1',
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   isActive             bit default 1 comment '0=not active
            1=active',
   isBoardCast          bit default 0,
   boardCastCount       bigint default 0,
   primary key (fileExtID)
);


drop table if exists platformDocType;

/*==============================================================*/
/* Table: platformDocType                                       */
/*==============================================================*/
create table platformDocType
(
   docTypeId            bigint not null auto_increment,
   docTypeGuid          varchar(255) not null,
   docTypeName          varchar(255),
   docTypeDesc          varchar(255),
   maxFileSize          int comment 'MB',
   fileShareFolder      varchar(255),
   comment              varchar(255),
   isActive             bit default 1 comment '0=not active
            1=active',
   createTime           datetime,
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   isBoardCast          bit default 0,
   boardCastCount       bigint default 0,
   primary key (docTypeId)
);

alter table platformDocType comment 'The table is used to descript the self defined platform docu';


drop table if exists documentList;

/*==============================================================*/
/* Table: documentList                                          */
/*==============================================================*/
create table documentList
(
   platformfileExtID    bigint,
   docTypeId            bigint,
   docGuid              varchar(255) not null,
   fileName             varchar(255),
   filePath             text,
   docId                bigint not null auto_increment,
   fileSize             int comment 'MB',
   version              bigint,
   isActive             bit default 1 comment '0=not avtive
            1=active',
   createTime           datetime not null default '1970-1-1',
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   memorySeaLevelTime   datetime comment 'latest time that current file should be searched at first PoseidonOptimizeSeek loop obviously.',
   memoryLiveTimeSec    bigint comment 'The defined duration between memorySealevelTime and the time which the file need to be modified to non-active automatically.',
   memoryHitTimes       bigint comment 'The times that current file was searched during the duration between memorySealevelTime and the time it to be non-active.',
   memoryPerHitComeUpSeconds bigint comment 'define how much seconds will be minused per memoryHitTimes when sys do PoseidonOptimizeSeek',
   storageClusterType   int comment '0=ceph
            1=hdfs',
   uploadedByPlatformUserGuid varchar(255) comment 'first upload user guid',
   lastModifiedByPlatformUserGuid varchar(255),
   deletedByPlatformUserGuid varchar(255),
   clusterStorageStatus int default 0 comment '0=didn''t store to diskFile cluster
            1=stored to diskFile cluster
            2=pending to be stored to diskFile cluster',
   fileExtName          varchar(255),
   viewerCacheFileName  varchar(255),
   viewerCacheTimeLength bigint comment 'unit is Minutes.',
   viewerCacheTime      datetime,
   primary key (docId)
);

alter table documentList comment 'major table to descript documents under entire platform';

alter table documentList add constraint FK_Reference_4 foreign key (platformfileExtID)
      references platformFileExt (fileExtID) on delete restrict on update restrict;

alter table documentList add constraint FK_Reference_5 foreign key (docTypeId)
      references platformDocType (docTypeId) on delete restrict on update restrict;

drop table if exists platformDocTypeFileExtRelation;

/*==============================================================*/
/* Table: platformDocTypeFileExtRelation                        */
/*==============================================================*/
create table platformDocTypeFileExtRelation
(
   docTypeFileExtRelationID bigint not null auto_increment,
   docTypeFileExtRelationGuid varchar(255),
   docTypeId            bigint not null,
   platformFileExtID    bigint,
   isActive             bit default 1 comment '0=not active
            1=active',
   createTime           datetime,
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   isBoardCast          bit default 0,
   boardCastCount       bigint default 0,
   primary key (docTypeFileExtRelationID)
);

alter table platformDocTypeFileExtRelation comment 'The table was used to descipt the relationship between platf';

alter table platformDocTypeFileExtRelation add constraint FK_Reference_2 foreign key (docTypeId)
      references platformDocType (docTypeId) on delete restrict on update restrict;

alter table platformDocTypeFileExtRelation add constraint FK_Reference_3 foreign key (platformFileExtID)
      references platformFileExt (fileExtID) on delete restrict on update restrict;


drop table if exists AppPlatformDocTypeRelation;

/*==============================================================*/
/* Table: AppPlatformDocTypeRelation                            */
/*==============================================================*/
create table AppPlatformDocTypeRelation
(
   appdocTypeRelationID bigint not null auto_increment,
   appdocTypeRelationGuid varchar(255) not null,
   appId                bigint,
   docTypeId            bigint,
   downloadFlag         int default 1 comment '0=not be able to download
            1=be able to download',
   uploadFlag           int default 0 comment '0=not be able to upload
            1=be able to upload',
   deleteFlag           int default 0 comment '0=not be able to delete
            1=be able to delete',
   isActive             bit default 1,
   createTime           datetime,
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   isBoardCast          bit,
   boardCastCount       bigint,
   primary key (appdocTypeRelationID)
);

alter table AppPlatformDocTypeRelation add constraint FK_Reference_1 foreign key (docTypeId)
      references platformDocType (docTypeId) on delete restrict on update restrict;



drop table if exists RequestLog;

/*==============================================================*/
/* Table: RequestLog                                            */
/*==============================================================*/
create table RequestLog
(
   reqId                bigint not null auto_increment,
   appId                bigint,
   appName              varchar(255),
   appGuid              varchar(255),
   userId               bigint,
   url                  text,
   createTime           timestamp default CURRENT_TIMESTAMP,
   reqStorageClusterType int comment '0=ceph
            1=hdfs',
   reqGuid              varchar(255),
   isActive             bit default 1,
   userGuid             varchar(255),
   primary key (reqId)
);

drop table if exists operationLog;

/*==============================================================*/
/* Table: operationLog                                          */
/*==============================================================*/
create table operationLog
(
   operationLogId       bigint not null auto_increment,
   operationStorageClusterType int comment '0=ceph
            1=hdfs',
   operationLogGuid     varchar(255),
   userId               bigint,
   userName             varchar(255),
   operationType        int comment '1=add
            2=update
            3=delete
            4=logicDelete
            5=read',
   operationLogTime     timestamp default CURRENT_TIMESTAMP,
   appId                bigint,
   docId                bigint,
   exfModuleId          bigint,
   viewId               bigint,
   platformControllerId bigint,
   platformActionId     bigint,
   usingObjectId        bigint,
   bizUserRoleId        bigint,
   deviceId             bigint,
   devLangId            bigint,
   workFlowStatusId     bigint,
   isDocExistingCurrently bit default 1 comment '0=not existing
            1=existing',
   isActive             bit default 1 comment '0=not active
            1=active',
   userGuid             varchar(255),
   appGuid              varchar(255),
   primary key (operationLogId)
);

drop table if exists DBUpgradeHistory;

/*==============================================================*/
/* Table: DBUpgradeHistory                                      */
/*==============================================================*/
create table DBUpgradeHistory
(
   nodeDbUpgradeHIstoryId bigint not null auto_increment,
   nodeDbGuid           varchar(255),
   nodeDbName           varchar(255),
   fromPlatformDbVersion bigint,
   toPlatformDbVersion  bigint,
   upgradeTime          timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   currentPlatformUser  bigint,
   platformUserLoginName varchar(255),
   platformUserName     varchar(255),
   platformHostGuid     varchar(255),
   comments             varchar(255),
   nodeDbUpgradeHIstoryGuid varchar(255),
   isActive             bit default 1,
   primary key (nodeDbUpgradeHIstoryId)
);

drop table if exists customerDbList;

/*==============================================================*/
/* Table: customerDbList                                        */
/*==============================================================*/
create table customerDbList
(
   id                   bigint not null auto_increment,
   guid                 varchar(255) not null,
   dataSourceClassName  varchar(255),
   dataSourceUser       varchar(255),
   dataSourcePassword   varchar(255),
   dataSourceDataBaseName varchar(255),
   dataSourcePortNumber bigint default 3306,
   dataSourceServerName varchar(255),
   remark               varchar(255),
   dbTypeNum            bigint default 0,
   isActive             bit default 1,
   createTime           datetime,
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   primary key (id)
);

alter table customerDbList comment 'config except nodeDB, which third party injected datasource ';


drop table if exists unitNodeRelation;

/*==============================================================*/
/* Table: unitNodeRelation                                      */
/*==============================================================*/
create table unitNodeRelation
(
   r_crystalNodeId      bigint,
   appId                bigint,
   r_crystalNodeGuid    varchar(255),
   isActive             bit default 1,
   createTime           datetime,
   updateTime           timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   unitNodeRelationId   bigint not null auto_increment,
   unitNodeRelationGuid varchar(255),
   unitNodeRole         bigint,
   unitNodeSource       int comment '0=managed under devops
            1=network seed',
   unitNodeIp           varchar(66),
   unitNodePort         bigint,
   unitNodeProtocolType int comment '0=http
            1=tcp
            2=udp',
   mem_totalHeap        double,
   mem_heapUsed         double,
   mem_totalForCurrentProcess double,
   mem_totalOnV8EngineUsing double,
   mem_usedMemRate      double,
   cpuArch              varchar(255),
   cpuInfo              varchar(2048),
   freemem              bigint,
   hostName             varchar(255),
   loadAvg              varchar(255),
   networkInterface     varchar(1024),
   platformtype         varchar(255),
   platformVersion      varchar(255),
   osTempDir            varchar(255),
   totalMemory          bigint,
   osType               varchar(255),
   nodeNormalRunedTime  bigint,
   primary key (unitNodeRelationId)
);

alter table unitNodeRelation comment 'define some particular relationship between unitnode and pla';

