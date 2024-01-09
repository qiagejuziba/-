# sky-take-out

## 一、苍穹外卖项目介绍
1. 本项目是使用 Spring Boot 框架开发的一个在线外卖订购系统。
 
## 二、技术栈

1. 后端框架
    - SpringBoot（2.7.17）
    - Mybatis
2. 数据库
    - MySQL
    - Redis
3. 前端框架
    - Vue
    - Uniapp
    - ElementUI
4. 前后端通信
    - RESTful API

## 三、Windows开发环境搭建
1. 安装 Java JDK 11 并配置环境变量
2. 安装 MySQL、Redis 数据库并创建相应数据库，在提供的资料文件中有数据库脚本，运行 sky.sql。
3. 安装Maven构建工具
1. 下载nodejs
5. 下载安装 Nginx，打开资料中的前端运行环境，可以找到Nginx的配置文件，Nginx配置文件相对路径（/conf/nginx.conf），Nginx文件不要放在有中文目录的文件中。如果点击nginx.exe出现闪一下，并且任务管理器中没有nginx进程的情况，大概率是Nginx的端口号80被其他应用程序占用了。可以选择杀死占用80端口的其他进程，或者修改nginx的端口号，参考这2篇文章解决问题：1、 [下载安装Nginx以及会遇到的问题详解](https://blog.csdn.net/cxy_ydj/article/details/122866499?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522170476952216800213042819%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=170476952216800213042819&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduend~default-1-122866499-null-null.142^v99^pc_search_result_base7&utm_term=nginx%E7%AB%AF%E5%8F%A3%E5%8F%B7%E8%A2%AB%E5%8D%A0%E7%94%A8&spm=1018.2226.3001.4187)
，2、 [80端口号被System进程占用解决办法](https://www.cnblogs.com/xwgcxk/p/11819018.html)


5. resources目录下的配置文件application.yml

```
server:
  port: 8080

spring:
  profiles:
    active: dev
  main:
    allow-circular-references: true
  datasource:
    druid:
      driver-class-name: ${sky.datasource.driver-class-name}
      url: jdbc:mysql://${sky.datasource.host}:${sky.datasource.port}/${sky.datasource.database}?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convert_To_Null&useSSL=false&allowPublicKeyRetrieval=true
      username: ${sky.datasource.username}
      password: ${sky.datasource.password}
  redis:
    host: ${sky.redis.host}
    port: ${sky.redis.port}
    password: ${sky.redis.password}
    database: ${sky.redis.database}

mybatis:
  #mapper配置文件
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.sky.entity
  configuration:
    #开启驼峰命名
    map-underscore-to-camel-case: true

logging:
  level:
    com:
      sky:
        mapper: debug
        service: info
        controller: info

sky:
  jwt:
    # 设置jwt签名加密时使用的秘钥
    admin-secret-key: itcast
    # 设置jwt过期时间
    admin-ttl: 720000000
    # 设置前端传递过来的令牌名称
    admin-token-name: token
    user-secret-key: itheima
    user-ttl: 720000000
    user-token-name: authentication
    #阿里云
  alioss:
    endpoint: ${sky.alioss.endpoint}
    access-key-id: ${sky.alioss.access-key-id}
    access-key-secret: ${sky.alioss.access-key-secret}
    bucket-name: ${bucket-name}
    # 微信小程序
  wechat:
    appid: ${sky.wechat.appid}
    secret: ${sky.wechat.secret}
    notify-url: ${sky.wechat.notify-url}
    refund-notify-url: ${sky.wechat.refund-notify-url}
```
6. resources目录下的application-dev.yml文件，把配置信息修改为自己的对应信息

```

sky:
  #MySQL数据库配置信息
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: localhost
    port: 3306
    database: sky_take_out #数据库名称
    username: root   #数据库用户名
    password: 123456 #数据库密码
  #阿里云配置信息
  alioss:
    endpoint: #填写自己的
    access-key-id: #填写自己的
    access-key-secret: #填写自己的
    bucket-name: #填写自己的
  #Redis数据库配置信息
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 10
  #微信小程序配置信息
  wechat:
    appid: #填写自己的
    secret: #填写自己的
   
```

## 四、使用说明

1.  打开idea，选择导入项目，选择项目存放路径。出现报错是因为需要加载pom.xml文件，很多依赖需要下载到本地，需要等待idea联网下载Maven依赖，或者自己重新加载一下Maven工程。![输入图片说明](https://foruda.gitee.com/images/1704768955708515740/c2a3e452_12656867.png "屏幕截图")
2.  - 项目用户端需要自己去微信小程序官方申请，需要按规则填写两项信息。![需要填写的两项信息](https://foruda.gitee.com/images/1704770234690370899/d1729420_12656867.png "屏幕截图") 
- 在开发管理中获取AppId和密钥，![输入图片说明](https://foruda.gitee.com/images/1704771236069651885/3603e50b_12656867.png "屏幕截图")
- 下载微信开发者工具，导入资料中提供的微信小程序代码（mp-weixin）。
3.  在redis安装目录下打开命令行窗口，输入：![输入图片说明](https://foruda.gitee.com/images/1704771851238671981/ec5463c7_12656867.png "屏幕截图"),出现![输入图片说明](https://foruda.gitee.com/images/1704771901221137687/75d3d5b7_12656867.png "屏幕截图")就代表运行成功了，不要关闭该窗口，redis是基于内存的key-value结构数据库，基于内存存储，读写性能高。“Another-Redis-Desktop-Manager”是一款redis数据库可视化软件，可以安装软件可视化redis。
4. 运行项目。




