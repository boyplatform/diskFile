# diskFile
DiskFile,One repo of boy distribute modality platform. Support Ceph and will support HDFS distribute storage tech, and also will provide a group of restful API for boy sharing modality platform's file storage.

#license policy
"license": "MPL(Mozilla Public License2.0)"
"comments": "Any unauthoritied using risk won't be charged to current platform developper-boybian. Meanwhile,thanks each every person who pushed this platform to be built."

#Why DiskFile
1.If you want save your platform's files/attachments on distributed file nodes,you can use DiskFile of boyplatform. 


#如何使用[how to use]
1.首先请在你将部署本微服务的容器/虚拟机/IOT下位机中配置安装java1.8环境以及mysql，mysql将作为该微服务的localDB用于微服务运行数据与逻辑数据的存储与处理。
1.First of all, Please install java1.8 environment and mysql into the container/virtual machine/IOT lower computer which you want to deploy this micro-service to, the mysql will be used as current micro-service's localDB to store and process operation data & logical data of current micro-service node.

2.接下来请在部署目标容器/虚拟机/IOT下位机中的mysql上运行repo中mysql localDB的初始化脚本。
2.Then, Please run the sql script under current github repo to init mysql localDB's structure on your deployment-target service container/virtual machine/ IOT lower computer .


3.如果你想在开发环境中调试本微服务,根据你项目的实际情况配置resources文件夹中的application.yml文件,你可手动复制编译后的JAR包到测试使用的容器/虚拟机/IOT下位机中,使用启动脚本文件或者java -jar命令运行这个微服务JAR包。
  如果你想在生产环境中使用本微服务并且你拥有一个devops团队，你可将预部署的容器/虚拟机/IOT下位机 IP及端口配置到ansible的主机清单中，将编译后的JAR包通过jenkins pipeline发布到Jfrog-Artifactory中，然后为你的部署目标服务器写一个playbook，在playbook中首先将发布在artifactory中的jar包下载到部署目标服务器，然后在playbook最后的task中执行Jar包启动命令，最后把发布、部署动作整合到你jenkins的publish、deployment pipeline中。
  如果你需要负载均衡以及更安全的内外网服务器分离，你可以将ansible主机清单中的终端IP配置给一台nginx服务作为集群的反向代理服务出口。
  application.yml文件ViewerFileCachePullRate可理解为整个diskFile集群节点刷新用户访问分布式存储文件时产生的静态文件资源之频率，可以理解为一种虚拟CDN机制的刷新频率。
3.If you want to debug this micro-service under your dev environment,please config application.yml under resources folder at first, then you can manually copy the compiled JAR to your deployment-target container/virtual machine/IOT lower computer, then use java -jar command or any lauch command batch file to lauch up this micro-service.
  If you want to use this micro-service under your prod environment and you have a devops team, you can put your deployment-target container/virtual machine/IOT lower computer IP into ansible inventory, then publish the JAR to Jfrog-Artifactory via jenkins pipeline, then write a playbook for your deployment-target service nodes, under playbook you can ask deployment-target node download the JAR from Jfrog-Artifactory at first, then execute Jar lauch command at last task of your playbook. In the end, integrate the publish,deployment action into your Jenkins pipeline.
  If you need load-balance and isolate interal and external network for your security, you can config the terminal IPs of your ansible inventory into a nginx service and let it be exit of your messageQueue cluster.
  ViewerFileCachePullRate of application.yml file can be identified as the rate of entire diskFile cluster which was used to refresh the static resource files which was generated once users visit distribute-storage files, it's quite similar to a kind of virtual CDN's refresh rate.


4.如果你想调试这个微服务的整套restful API，可使用fiddler尝试运行如下报文。
4.If you want to debug this micro-service's restful API,you can use fiddler to run below post message to related API.


--ceph集群挂载(ceph mount)

post url:  http://www.boydiskfile.com/DiskFile/CephEntry/mount

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 135
content-type: application/json

body message:
{
 "username":"admin",
 "monIp":"192.168.125.128:6789;192.168.125.129:6789;192.168.125.130:6789",
 "userKey":"AQDyuF1dpxsnExAAAIkIlT3m7gcuhgvW+aPmiw==",
 "mountPath":"/"

}

--创建平台文件类型(create platform's document type)

post url:  http://www.boydiskfile.com/DiskFile/CephEntry/addDocType

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 135
content-type: application/json

body message:
{

"docTypeName":"For test APP groups2",
"docTypeDesc":"test2",
"maxFileSize":10 ,
"fileShareFolder":"target/uploadFileCache/",
"comment":"test comment2",
"isActive":true

}

--获取平台文件类型列表(seek platform's document type list)
post url: http://www.boydiskfile.com/DiskFile/CephEntry/seekDocType

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 135
content-type: application/json

body message:
{}


--创建平台平台支持的文件扩展名(create the file extension name which should be supported by the platform)
post url:  http://www.boydiskfile.com/DiskFile/CephEntry/addFileExt  
  
User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 135
content-type: application/json

body message:
{
"fileExtName":"xml"
}

--获取平台支持的文件扩展名列表(get the file extension name which has been supported by the platform)
post url: http://www.boydiskfile.com/DiskFile/CephEntry/seekFileExt

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 135
content-type: application/json
body message:
{}

--创建平台文件类型-文件扩展名映射关系(create the platform document type and supported file extension name mapping relationship)
post url: http://www.boydiskfile.com/DiskFile/CephEntry/addDocTypeFileExtRelation

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 579
content-type: application/x-www-form-urlencoded;charset=utf-8

body message:
docTypeId=21&fileExtID=22

--获取平台文件类型-文件扩展名关系列表(get the platform document type and supported file extension name mapping relationship list)
post url: http://www.boydiskfile.com/DiskFile/CephEntry/seekDocTypeFileExtRelation

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 135
content-type: application/json

body message:
{}

--写入分布式存储文件 (write distributed file)  
post url: http://www.boydiskfile.com/DiskFile/CephEntry/vim?isBig=0&docTypeId=21&platformfileExtID=22&userGuid=static
 
Content-Type: multipart/form-data; boundary=-------------------------acebdf13572468
User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 223

---------------------------acebdf13572468
Content-Disposition: form-data; name="inputBufferFiles"; filename="test4.xml"
Content-Type: text/xml

<@INCLUDE *D:\WorkSpace\The5\UT\Boy1.0\Board\diskFile\test4.xml*@>
---------------------------acebdf13572468--

--读取分布式存储文件 (read distributed file)

post url: http://www.boydiskfile.com/DiskFile/CephEntry/cat

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 579
content-type: application/x-www-form-urlencoded;charset=utf-8

-下载模式body报文(download body message):
userGuid=static&docTypeId=21&platformfileExtID=22&fileName=test4.xml&catType=download&isBig=0

-优先下载微服务节点缓存文件模式body报文(seekCached body message):
userGuid=static&docTypeId=21&platformfileExtID=22&fileName=test4.xml&catType=seekCached&isBig=0

-在微服务节点临时生成并"返回"分布式文件资源对应的静态资源文件URL模式body报文(getStaticUrl body message):
userGuid=static&docTypeId=21&platformfileExtID=22&fileName=test4.xml&catType=getStaticUrl&isBig=0&viewerCacheLength=3

-在微服务节点临时生成并"跳转至"分布式文件资源对应的静态资源文件URL模式body报文(redirectToStaticUrl body message):
userGuid=static&docTypeId=21&platformfileExtID=22&fileName=test4.xml&catType=redirectToStaticUrl&isBig=0&viewerCacheLength=3


--读目录文件列表 (seek files list by path for platform's user)

post url:  http://www.boydiskfile.com/DiskFile/CephEntry/ls

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 579
content-type: application/x-www-form-urlencoded;charset=utf-8

body message:
userGuid=static


--重命名目录或文件 (rename ceph dir or file)

post url: http://www.boydiskfile.com/DiskFile/CephEntry/rename

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 579
content-type: application/x-www-form-urlencoded;charset=utf-8

body message:
userGuid=static&oldFileName=test4.xml&newFileName=testNew.xml&platformfileExtID=22&docTypeId=21


--删除文件 (delete ceph file) 

post url: http://www.boydiskfile.com/DiskFile/CephEntry/rm

User-Agent: Fiddler
Host: www.boydiskfile.com
Content-Length: 579
content-type: application/x-www-form-urlencoded;charset=utf-8

body message:
userGuid=static&fileName=testNew.xml&platformfileExtID=22&docTypeId=21

--跳转到当前活动的ceph cluster dashboard(Jump to current actived ceph cluster dashboard) [start up ceph-rest-api server on application.yml's apiUrl's host]

request url: http://www.boydiskfile.com/DiskFile/CephEntry/seekCephDashBoard