# HamiCommunity
端口-DEV
```text
mysql: 8300
redis: 8301
rabbitmq: 8302, 8303
spring-boot: 8304
front-end: 8305
```

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
- [ ] 关注用户的文章列表

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
- [ ] 通知查询

### 后台管理

暂定

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
{business_type}{use_id}:zset[{item_id}:{timestamp}]
```

### 文章数据和用户数据 

文章：

```
{article_stat}{article_id}:hash
```

### 首页文章

使用Redis zset保存最新发布的文章ID，每个zset最多保存1000篇，超过的回源数据库查询,每次更新时，没有超过1000，直接放入，

否则移除最晚发布的再放入

更新文章时，从原来的zset删除，再放到新的zset

删除时，从zset删除即可

```
{artile:list:total}:zset
{article:list:}{cate_id}:zset
{user:article:list}:zset
```

## BUG

```
com.fasterxml.jackson.databind.exc.InvalidTypeIdException: Could not resolve type id '1000' as a subtype of `java.lang.Object`: no such class found
 at [Source: (byte[])"[1000,1001]"; line: 1, column: 7]

```

`Jackson反序列化集合失败`

https://github.com/FasterXML/jackson-databind/issues/3892

使用List.of(...)时，NO_FINAL，序列化时没有包含完整的通用类型信息，改为EVERYTHING，

```java
objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                                   ObjectMapper.DefaultTyping.NON_FINAL,
                                   JsonTypeInfo.As.PROPERTY);
====>
objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                                   ObjectMapper.DefaultTyping.EVERYTHING,
                                   JsonTypeInfo.As.PROPERTY);
```

