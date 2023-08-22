# HamiCommunity
端口-DEV
```text
mysql: 8300
redis: 8301
rabbitmq: 8302, 8303
spring-boot: 8304
front-end: 8305
```

### 重置密码方案
1. 用户输入邮箱，然后请求验证码，用户输入验证码，后端校验正确后告诉前端可以进行下一步，
  同时设计校验成功标识，前端用户填写新的密码后，提交请求，后端验证是否有之前的标识，有则重置密码
2. 用户带着验证码请求，后端仅验证是否正确，用户填写新的密码后，需要提交验证码一起校验

### 项目功能

#### 用户

- [x] 登录
- [x] 注册
- [x] 修改密码
- [ ] 用户信息
- [ ] 用户数据
- [ ] 信息修改
- [ ] 关注/取关
- [ ] 账户信息修改

#### 文章

- [ ] 文章列表
- [ ] 文章详情
- [ ] 文章点赞/取消
- [ ] 文章收藏/取消
- [ ] 文章阅读量统计
- [ ] 文章编写/发表/更新删除
- [ ] 热门文章
- [ ] 历史记录

#### 标签

- [ ] 标签文章
- [ ] 热门标签

#### 搜索

- [ ] 文章搜索
- [ ] 用户搜索

#### 评论 

- [ ] 发表评论
- [ ] 评论列表查询
- [ ] 评论点赞/取消
- [ ] 评论删除

#### 通知

- [ ] 关注用户文章发表通知
- [ ] 新增粉丝
- [ ] 评论通知
- [ ] 收藏通知
- [ ] 点赞文章通知