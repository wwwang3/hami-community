# HamiCommunity
## 项目功能

#### 用户

- [x] 登录
- [x] 注册
- [x] 修改密码
- [x] 用户信息
- [x] 用户数据
- [x] 信息修改
- [x] 关注/取关
- [x] 账户信息修改
- [x] 用户关注列表
- [x] 用户粉丝列表

#### 文章

- [x] 文章列表
- [x] 文章详情
- [x] 文章点赞/取消
- [x] 文章收藏/取消
- [x] 文章阅读量统计
- [x] 文章编写/发表/更新删除
- [x] 热门文章
- [x] 历史记录
- [x] 用户主页文章
- [x] 用户点赞/收藏文章列表
- [x] 关注用户的文章列表

#### 标签

- [ ] ~~标签文章~~
- [ ] ~~热门标签~~

#### 搜索

- [x] 文章搜索
- [ ] ~~用户搜索~~

#### 评论 

- [x] 发表评论
- [x] 评论列表查询
- [x] 评论点赞/取消
- [x] 评论删除

#### 通知

- [x] 关注用户文章发表通知
- [x] 新增粉丝
- [x] 评论通知
- [x] 收藏通知
- [x] 点赞文章通知
- [x] 通知查询

#### 后台管理

暂定

## Canal整合RabbitMQ

Canal发送消息到RabbitMQ不支持消息分区，但支持单表的路由，单个消费者可以保证单表的顺序性

`canal.properties`

```properties
#主要配置 canal:v1.1.7
canal.serverMode=rabbitMQ
canal.destinations=example
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
canal.instance.connectionCharset = UTF-8
# table regex
# Only match all tables under db_hami_community
canal.instance.filter.regex=db_hami_community\\..*
# table black regex
canal.instance.filter.black.regex=mysql\\.slave_.*
```

> 配置RabbitMQ动态路由时, `.*\\..*`与`test\\..*`都会发送至`数据库名_表名`对应的路由上, 与文档描述不一致

## 前端

- [x] 评论
- [x] 个人空间
- [x] 文章
- [x] 消息通知
- [x] 创作者中心
- [x] 历史记录
- [x] 搜索

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
