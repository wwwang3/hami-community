# HamiCommunity

### 前端地址

https://github.com/wwwang3/hami-community-front-end

### 项目结构

| 模块          | 描述                                      |
| :------------ | ----------------------------------------- |
| hami-api      | web模块，项目启动入口，包含全局异常处理   |
| hami-canal    | 整合Canal的模块                           |
| hami-common   | 公共模块，model，dto等类，一些基础的配置  |
| hami-core     | 核心功能实现                              |
| hami-mail     | 邮件发送功能，提供配置多个mail-sender功能 |
| hami-security | 安全模块，提供接口限流，权限配置          |

## 本地开发

#### 克隆本项目

```sh
git clone git@github.com:wwwang3/hami-community.git
```

#### 增加配置文件

涉及cos等隐私信息，为上传application.yml

在`hami-api/src/main/resources`下新增配置文件

`application.yml`

```yaml
spring:
  profiles:
    active:
      - mail
      - hami
      - @environment@
```

`applicatio-hami.yml`

```yml
hami:
  mode: test
  ## canal配置
  canal:
    exchange: hami-canal-exchange
    exchange-type: topic
    containers:
    ### 一个容器相当于一个消费者
      canal-article-container-1:
      # 监听的表
        tables:
          - name: article
      canal-article-container-2:
        tables:
          - name: article
      canal-interact-container-1:
        concurrency: 2
        tables:
          - name: user_follow
          - name: tb_like
          - name: article_collect
      canal-interact-container-2:
        # 对数据的同步, 只要保证+1和-1操作成功即可
        concurrency: 4
        tables:
          - name: user_follow
          - name: tb_like
          - name: article_collect
      canal-stat-container-1:
        tables:
          - name: article_stat
          - name: user_stat
    schema: db_hami_community
    dl-exchange: hami-dl-exchange-1
    dl-routing-key: /dead
    # 是否开启flat-message
    flat-message: true
  version: @app.version@
  ## 是否在项目启动时将文章，用户等缓存数据加载到Redis
  init:
    enable: false
  #    list:
  #      - ARTICLE_CACHE
  #      - USER_CACHE
  #      - STAT_CACHE
  #      - ARTICLE_LIST_CACHE
  #      - ACCOUNT_CACHE
  #      - RANK_LIST
  security:
    cookie:
      domain: localhost
      enable: true
      http-only: true
    ## jwt密钥 随便打的
    secret: gwefwlgergor124fwegerlkvrjgkwp23r23
    ### jwt密钥过期时间
    expire: 604800
    cors:
      allow-credentials: true
      allowed-origins:
        - http://localhost:8305
        - https://test.hami.wang3.top
      pattern: /**
    username-parameter: account
    password-parameter: password
    ## 限流配置
    rate-limit:
      enable: true
      configs:
        - patterns:
            - /api/v1/auth/login
          algorithm: fixed_window
          scope: ip_uri
          rate-meta:
            capacity: 400
            interval: 86400
    token-name: token
    ## 接口权限配置，也可在Controller方法上使用@Api注解配置
    api-infos:
      - patterns:
          - /actuator/health
        access-control: PUBLIC
        http-method:
          name: GET
  ## 接收告警消息的邮箱
  email: wang3.top@gmail.com
  ## servive方法执行时间日志
  cost-log: true
  ## 没啥用其实
  work-dir: hami/app
```

`application-mail.yml`

```yaml
hami:
  mail:
  	## 某个sender发送失败是否开启重试
    retry: false
    ## 发送策略
    strategy: ROUND_ROBIN
    ## mail-sender配置
    ## 与SpringBoot的MailSender配置一样，多了key[必须]
    configs:
      - key: wang3
        default-encoding: UTF-8
        host: smtp.qq.com
        username: your account
        password: your account pass
        protocol: smtp
        port: 465
        properties:
          mail:
            smtp:
              auth: true
              starttls:
                enable: true
                required: true
              socketFactory:
                port: 465
                class: javax.net.ssl.SSLSocketFactory
                fallback: false
      - key: wang4
        default-encoding: UTF-8
        host: smtp.qq.com
        username: your account
        password: your account pass
        protocol: smtp
        port: 465
        properties:
          mail:
            smtp:
              auth: true
              starttls:
                enable: true
                required: true
              socketFactory:
                port: 465
                class: javax.net.ssl.SSLSocketFactory
                fallback: false
```

- 需要更改account和password

`application-dev.yml`

```yaml
server:
  port: 8304
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/**.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-not-delete-value: 0
      logic-delete-value: 1
    banner: off
spring:
## docker配置，可以在项目启动时启动docker容器，需要本地有docker，compose文件在hami/
  docker:
    compose:
      enabled: true
      file: hami/docker-compose.yml
      lifecycle-management: start_only
  file-storage:
    default-platform: tencent-cos-1
    tencent-cos:
      - platform: tencent-cos-1 # 存储平台标识
        enable-storage: true  # 启用存储
        secret-id: xxxx
        secret-key: xxx
        region: xxx
        bucket-name: xxxx
        domain: xxx
        base-path: xxx
  datasource:
    url: jdbc:mysql://localhost:8300/db_hami_community?characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: test
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 200
      minimum-idle: 20
  jackson:
    time-zone: GMT+8
    serialization:
      write-dates-as-timestamps: true
  servlet:
    multipart:
      max-file-size: 4MB
      max-request-size: 10MB
  data:
    redis:
      host: localhost
      password: 123456
      port: 8301
      lettuce:
        pool:
          max-active: 64
          max-idle: 32
          min-idle: 8
      timeout: 60s
  rabbitmq:
    host: localhost
    port: 8302
    virtual-host: /
    username: root
    password: 123456
    listener:
      simple:
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 2000ms
  web:
    resources:
      add-mappings: false
```

- file-storage配置，可参考https://x-file-storage.xuyanwu.cn/#/

- docker配置在`hami/`下，可自行更改，更改了配置可能需要相应的调整SpringBoot的配置

## 部署

1. 将前端项目打包好的dist文件夹放入到`hami/build/nginx`目录下

2. 打包后端，会将输出jar放到`hami/build/app`目录下

   ```sh
   mvn clean && mvn package
   ```

3. 如果有https证书，将证书文件方到`hami/nginx/certs`目录下，没有则忽略

4. 修改`hami/nginx/conf/hami.conf`

   ```nginx
   upstream hami_app_server {
       server app:8304;
   }
   
   server {
   
           listen 80;
       	# 如果配置了证书
           listen 443 ssl;
   		# 如果你有域名
           server_name hami.wang3.top;
   
           #log
           access_log /data/nginx/log/hamiaccess_log.log;
           error_log /data/nginx/log/hami_error_log.log;
   
       	## 如果你有证书
           ssl_certificate /data/nginx/certs/hami.wang3.top_bundle.crt;
   
           ssl_certificate_key /data/nginx/certs/hami.wang3.top.key;
   
           ssl_session_timeout 5m;
   
           ssl_protocols TLSv1.2 TLSv1.3;
   
           ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
   
           ssl_prefer_server_ciphers on;
   
           client_max_body_size 1024m
   
   
           location ^~ /api/v1/ {
                   proxy_pass http://hami_app_server;
                   proxy_set_header HOST $host;
                   proxy_set_header X-Forwarded-Proto $scheme;
                   proxy_set_header X-Real-IP $remote_addr;
                   proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           }
   
           location /favicon.ico {
                   root /data/nginx/html;
           }
   
           location / {
                   root /data/nginx/html/hami/index.html;
                   index index.html;
                   try_files $uri $uri/ /index.html;
           }
   
   }
   ```

5. 修改mysql,redis,canal配置文件, 主要是密码等

6. 将hami文件夹上传到服务器，使用docker执行hami文件夹下的`docker-compose-prod.yml`

   ```sh
   docker compose -f docker-compose-prod.yml up -d
   ```

## Canal整合RabbitMQ

Canal发送消息到RabbitMQ不支持消息分区，但支持单表的路由，单个消费者可以保证单表的顺序性

`canal.properties`

```properties
#主要配置 canal:v1.1.7
canal.serverMode=rabbitMQ
canal.destinations=example
#flat-message
canal.mq.flatMessage=false 
canal.mq.topic=/canal
canal.mq.dynamicTopic=.*\\..*
rabbitmq.host=192.168.3.13
rabbitmq.virtual.host=/
rabbitmq.exchange=hami-canal-exchange
rabbitmq.username=root
rabbitmq.password=123456
rabbitmq.deliveryMode=topic
rabbitmq.queue=hami-canal-queue
rabbitmq.routingKey=/canal
```

`instance.properties`

```properties
# position info
canal.instance.master.address=192.168.3.11:3306
# username/password
canal.instance.dbUsername=canal
canal.instance.dbPassword=123456
canal.instance.defaultDatabaseName=db_hami_community
canal.instance.connectionCharset=UTF-8
# table regex
# Only match all tables under db_hami_community
canal.instance.filter.regex=db_hami_community\\..*
# table black regex
canal.instance.filter.black.regex=mysql\\.slave_.*
```

> 配置RabbitMQ动态路由时, `.*\\..*`与`test\\..*`都会发送至`数据库名_表名`对应的路由上, 与文档描述不一致

## 方案设计

### 重置密码方案

1. 用户输入邮箱，然后请求验证码，用户输入验证码，后端校验正确后告诉前端可以进行下一步，
   同时设计校验成功标识，前端用户填写新的密码后，提交请求，后端验证是否有之前的标识，有则重置密码
2. 用户带着验证码请求，后端仅验证是否正确，用户填写新的密码后，需要提交验证码一起校验

### 点赞，收藏，关注列表

redis+mysql存储

redis：

以业务类型用户ID为key，value为zset结构，member为实体ID，score为timestamp

不适合数据量大的情况

比如某个人的粉丝有十几万(百万up主(bushi^_^))

用户数据量大存在key过多的问题，可通过Redis集群+分区Key解决

```
{business_type}:{user_id}:zset[{timestamp}:{item_id}]
```

`timestamp`为score, `item_id`为member

### 点赞

- 对某个文章/评论点赞（取消点赞）
- 查询是否对 单个 或者 一批实体 点过赞 - 即点赞状态查询
- 查询某个实体的点赞数
- 查询某个用户的点赞列表
- 查询某个实体的点赞人列表
- 查询用户收到的总点赞数

Redis中维护用户的点赞数和用户最近的点赞列表,超过最大存储数量时，回源DB查询

> 目前存储的是用户所有的点赞数据

用户点赞后，先更新Redis，在异步更新MySQL，异步写入MySQL时，如果多个消费者同时消费，会可能会出现点赞-取消点赞写入顺序不一致的问题(对于点赞收藏，关注等行为貌似问题不大)，导致Redis和MySQL数据不一致，关注的前置判断以及关注状态的判断在Redis，MySQL关注记录的写入或者更新，不会影响Redis，在缓存过期重新读取前，用户感知不到MySQL写入成功与否。

通过RabbitMQ的topic主题路由，可以实现每个用户操作的串行写入，路由Key加上用户ID取模，比如：

```tex
xx.like.{user_id % 5}
```

或者直接单条队列，单个消费者。

收藏和关注类似于点赞

### 文章

使用Redis zset保存最新发布的文章ID，每个zset最多保存1000篇，超过的回源数据库查询,每次更新时，没有超过1000，直接放入，

否则移除最晚发布的再放入

更新文章时，从原来的zset删除，再放到新的zset

删除时，从zset删除即可

```
{artile:list:total}:zset
{article:list:}{cate_id}:zset
{user:article:list}:zset
```

### 文章数据

文章数据服务接收点赞，评论，收藏，文章发布消息，增加异步写入MySQL，同时使用Canal同步MySQL数据到Redis

### 用户数据

接收文章，点赞，评论，收藏，关注等消息

### 用户行为

包括点赞，评论，收藏，关注等行为，需要查询点赞记录，收藏列表，我的关注，粉丝等

```tex
user:interact:hash
- like:type:count
- collect:count
- follow:count
- follower:count
```
