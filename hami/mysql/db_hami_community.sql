/*
 Navicat Premium Data Transfer

 Source Server         : hami-mysql-local
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 172.21.176.1:8300
 Source Schema         : db_hami_community

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 15/09/2023 16:34:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

drop database if exists `db_hami_community`;
create database `db_hami_community`;
use db_hami_community;
-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user' COMMENT '角色',
  `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '密码',
  `state` tinyint NOT NULL DEFAULT 0 COMMENT ' 状态 0-未激活 1-激活',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账号表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES (1, 'wang3', 'wang3.top@qq.com', 'user', '$2a$10$Lo9MdkPkR5rz88TiJA7NG.n8FdErSuzYmLLqCgNdUYLwgma7iOgJu', 1, 0, '2023-09-02 22:54:23', '2023-09-02 22:54:23');
INSERT INTO `account` VALUES (2, 'wang5', '2780348784@qq.com', 'user', '$2a$10$dYlpHE6UQIzMQ2aDAbB3U.aNabXWwRXFEykOCfM8jUhTsuUpLIOji', 1, 0, '2023-09-02 23:07:34', '2023-09-02 23:07:34');

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文章id',
  `user_id` int NOT NULL COMMENT '作者id',
  `category_id` int NOT NULL COMMENT '分类id',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章标题',
  `summary` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章简介',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章内容',
  `picture` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'https://static-oss.wang3.top/hami-images/64f3e6a6e5c79555236b492e.jpg' COMMENT '文章封面',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article
-- ----------------------------

-- ----------------------------
-- Table structure for article_collect
-- ----------------------------
DROP TABLE IF EXISTS `article_collect`;
CREATE TABLE `article_collect`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fid` int NULL DEFAULT NULL COMMENT '收藏夹ID (备用/先不搞收藏夹)',
  `user_id` int NOT NULL COMMENT '用户ID',
  `article_id` int NOT NULL COMMENT '文章ID',
  `state` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `ctime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_article_id`(`user_id` ASC, `article_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article_collect
-- ----------------------------

-- ----------------------------
-- Table structure for article_draft
-- ----------------------------
DROP TABLE IF EXISTS `article_draft`;
CREATE TABLE `article_draft`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID,草稿ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `article_id` int NULL DEFAULT NULL COMMENT '文章ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `picture` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章图片地址',
  `summary` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章简介',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文章内容',
  `article_tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文章标签',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `state` tinyint NOT NULL COMMENT '草稿状态 0-未发表 1-已发表',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '版本号',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `pk_article_draft`(`article_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章草稿表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article_draft
-- ----------------------------
INSERT INTO `article_draft` VALUES (1, 2, 1, '测试测试', '', '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测测测', '# 一级标题\n## 二级标题', '[1000,1001]', 10000, 1, 0, 0, '2023-09-03 09:37:15', '2023-09-03 09:37:15');
INSERT INTO `article_draft` VALUES (2, 2, 2, '超超超', 'https://static-oss.wang3.top/hami-images/64f3e6a6e5c79555236b492e.jpg', '摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要摘要', '## 超超超', '[1000]', 10000, 1, 2, 0, '2023-09-03 09:50:09', '2023-09-03 09:52:03');
INSERT INTO `article_draft` VALUES (3, 2, 3, 'SpringAop', 'https://static-oss.wang3.top/hami-images/64f3e843e5c79555236b492f.jpg', 'AOP （Aspect Orient Programming）,直译过来就是 面向切面编程。AOP 是一种编程思想，是面向对象编程（OOP）的一种补充。面向对象编程将程序抽象成各个层次的对象，而面向切面编程是将程序抽象成各个切面。通俗来讲就是在不通过修改源代码的方式，增加新的功能。', '## Spring AOP\n\n### 什么是 AOP\n\nAOP （Aspect Orient Programming）,直译过来就是 面向切面编程。AOP 是一种编程思想，是面向对象编程（OOP）的一种补充。面向对象编程将程序抽象成各个层次的对象，而面向切面编程是将程序抽象成各个切面。通俗来讲就是在不通过修改源代码的方式，增加新的功能。\n\n### Spring aop\n\nSpring AOP 使用的动态代理，运行时生成 AOP 代理类，所谓的动态代理就是说 AOP 框架不会去修改字节码，而是在内存中临时为方法生成一个 AOP 对象，这个 AOP 对象包含了目标对象的全部方法，并且在特定的切点做了增强处理，并回调原对象的方法。\n\nSpring AOP 中的动态代理主要有两种方式，\n\n1.  JDK 动态代理\n1.  CGLIB 动态代理。\n\nJDK 动态代理通过反射来接收被代理的类，并且要求被代理的类必须实现一个接口。JDK 动态代理的核心是`InvocationHandler`接口和`Proxy`类。\n\n如果目标类没有实现接口，那么 Spring AOP 会选择使用 CGLIB 来动态代理目标类。\n\n> CGLIB 通过继承的方式实现动态代理，若类是 final 的则无法被继承，也就无法被动态代理。\n\nJDK 动态代理示例：\n\n```\npublic interface UserDao {\n    boolean addUser(String name);\n}\npublic class UserDaoImpl implements UserDao {\n    @Override\n    public boolean addUser(String name) {\n        System.out.println(\"add a user who named \" + name);\n        return true;\n    }\n}\n/////\npublic class UserDaoProxy implements InvocationHandler {\n    //被代理的对象\n    public Object proxiedObj;\n    public UserDaoProxy() {\n    }\n    //哪个对象要被代理，就传递哪个对象\n    public UserDaoProxy(Object proxiedObj) {\n        this.proxiedObj = proxiedObj;\n    }\n    //增强的逻辑\n    @Override\n    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {\n        //被代理方法执行前\n        System.out.println(\"prepare to add a user\");\n        //执行原来被代理对象的方法\n        Object res = method.invoke(proxiedObj, args);\n        //被代理方法执行后\n        System.out.println(\"A user is added\");\n        return res;\n    }\n    public static void main(String[] args) {\n        UserDao dao = (UserDao) Proxy.newProxyInstance(UserDaoProxy.class.getClassLoader(), new Class[]{UserDao.class}, new UserDaoProxy(new UserDaoImpl()));\n        dao.addUser(\"小明\");\n    }\n}\n```\n\n输出：\n\n![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a312aaf874d44c0aab7d1e179d444f10~tplv-k3u1fbpfcp-zoom-1.image)\n\n### aop 的中的术语\n\n1.  Joint point：表示在程序中明确定义的点，典型的包括方法调用，对类成员的访问以及异常处理程序块的执行等等，它自身还可以嵌套其它 joint point。\n1.  Pointcut：表示一组 joint point，这些 joint point 或是通过逻辑关系组合起来，或是通过通配、正则表达式等方式集中起来，它定义了相应的 Advice 将要发生的地方。\n1.  Advice：Advice 定义了在 pointcut 里面定义的程序点具体要做的操作，它通过 before、after 和 around 来区别是在每个 joint point 之前、之后还是代替执行的代码。\n1.  Aspect： Aspect 声明类似于 Java 中的类声明，在 Aspect 中会包含着一些 Pointcut 以及相应的 Advice。\n1.  Weaving：织入是把切面应用到目标对象并创建新的代理对象的过程。\n\n### 使用@AspectJ 注解实现 SpringAop\n\nSpring Aop 只是使用了 AspectJ 框架的注解，底层实现原理依然是动态代理。\n\n引入依赖：\n\n```\n<!--spring aop支持-->\n<dependency>\n    <groupId>org.springframework</groupId>\n    <artifactId>spring-aop</artifactId>\n    <version>5.1.8.RELEASE</version>\n</dependency>\n<!--aspectj支持-->\n<dependency>\n    <groupId>org.aspectj</groupId>\n    <artifactId>aspectjrt</artifactId>\n    <version>1.8.5</version>\n</dependency>\n<dependency>\n    <groupId>org.aspectj</groupId>\n    <artifactId>aspectjweaver</artifactId>\n    <version>1.8.9</version>\n</dependency>\n```\n\n#### 定义切面\n\n先定义一个接口和一个实现类，当做被代理对象。\n\n```\n//定义一个表演接口\npublic interface Performance {\n    void perform();\n}\npublic class PerformanceImpl  implements Performance {\n    @Override\n    public void perform() {\n        System.out.println(\"表演进行中\");\n    }\n}\n```\n\n> 案例来源于 spring in action\n\n演出没有观众可不行，而观众对于演出本身的功能来说，它又不是核心功能，而是一个单独的关注点，所以将观众定义为一个切面是更好的选择。\n\n使用@Aspect 注解定义切面类\n\n```\n@Aspect\npublic class Audience {\n}\n```\n\n@Aspect 注解表名 Audience 不仅是一个 POJO 对象，还是一个切面。其类中的方法可使用注解来定义切面的具体行为。\n\n#### 定义切点\n\n```\nexecution(modifiers-pattern? ret-type-pattern declaring-type-pattern?\n                                name-pattern(param-pattern)\n                throws-pattern?)\n```\n\n- `modifiers-pattern?` 为访问权限修饰符\n- `ret-type-pattern` 为返回类型，通常用 `*` 来表示任意返回类型\n- `declaring-type-pattern?` 为包名\n- `name-pattern` 为方法名，可以使用 `*` 来表示所有，或者 `set*` 来表示所有以 set 开头的方法\n- `param-pattern)` 为参数类型，多个参数可以用 `,` 隔开，各个参数也可以使用 `*` 来表示所有类型的参数，还可以使用 `(..)` 表示零个或者任意参数\n- `throws-pattern?` 为异常类型\n- `?` 表示前面的为可选项\n  > 参考自：沉默王二，链接：https://juejin.cn/post/7067342522837631006\n\n例：`execution(* * demo.concert.Performance.perform(..)))`\n\n表示 demo.concert 包下 Performance 类中的任意的 perform 方法。\n\nSpring 引入了一个新的 bean()指示器，它允许我们在切点表达式使用 bean 的 id 标识 bean，来限制切点只匹配特定的 bean。\n\n例：\n\n```\nexecution(* * demo.concert.Performance.perform(..))) and bean(\"demo\")\n```\n\n表示为 Performance 的 perform 方法应用通知，但限定 bean 的 id 为 demo。\n\n声明通知方法\n\nAspectJ 提供了五个用于声明通知的方法\n\n| 注解            | 通知                                         |\n| --------------- | -------------------------------------------- |\n| @Before         | 通知方法在目标方法之前调用                   |\n| @After          | 通知方法在目标方法返回之后或抛出异常之后调用 |\n| @AfterReturning | 通知方法在目标方法返回后调用                 |\n| @AfterThrowing  | 通知方法在目标方法抛出异常后调用             |\n| @Around         | 通知方法将目标方法封装起来                   |\n\n```\n@Before(\"performance()\")\npublic void silencePhones() {\n    System.out.println(\"====表演之前====\");\n    System.out.println(\"演出前将手机静音\");\n}\n@Before(\"execution(* *demo.concert.Performance.perform(..))\")\npublic void takeSeats() {\n    System.out.println(\"观众就坐\");\n}\n@AfterReturning(\"execution(* *demo.concert.Performance.perform(..))\")\npublic void applause() {\n    System.out.println(\"=====表演结束====\");\n    System.out.println(\"观众鼓掌\");\n}\n@AfterThrowing(\"execution(* *demo.concert.Performance.perform(..))\")\npublic void demandRefund() {\n    System.out.println(\"表演出意外了,xxx退钱!!\");\n}\n```\n\n上面相同的切点表达式重复使用了四次，我们可以使用@Pointcut 注解定义可重用的切点\n\n例：\n\n```\n@Pointcut(\"execution(* *demo.concert.Performance.perform(..)))\")\npublic void performance() {\n​\n}\n@Before(\"performane()\")\npublic void silencePhones() {\n    System.out.println(\"演出前将手机静音\");\n}\n```\n\n启用 AspectJ 自动代理\n\n在配置类上使用`*@EnableAspectJAutoProxy*`_注解即可_\n\n```\n@Configuration\n@EnableAspectJAutoProxy\npublic class ContextConfig {\n    @Bean\n    public Audience audience() {\n        return new Audience();\n    }\n}\n@RunWith(SpringJUnit4ClassRunner.class)\n@ContextConfiguration(classes = ContextConfig.class)\npublic class ContextConfigTest {\n    @Autowired\n    public Performance performance;\n\n    @Test\n    public void test1() {\n        performance.perform();\n    }\n}\n```\n\n![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5f6da848a8a442afbdd9cdb40aebeb47~tplv-k3u1fbpfcp-zoom-1.image)\n\n现在我们制造一个异常：\n\n![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/02cd8873880343a88147db24da409add~tplv-k3u1fbpfcp-zoom-1.image)\n\n再次测试\n\n![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/190080c65f2040ac8c4bed1e7e9faa35~tplv-k3u1fbpfcp-zoom-1.image)\n\n#### 环绕通知\n\n环绕通知能够将方法包装起来，实际上就像在通知方法中同时编写了前置和后置通知。\n\n```\n@Around(\"performance()\")\npublic void watchPerformance(ProceedingJoinPoint joinPoint) {\n    try {\n        System.out.println(\"====表演之前====\");\n        System.out.println(\"演出前将手机静音\");\n        //调用被通知的方法\n        joinPoint.proceed();\n        System.out.println(\"=====表演结束====\");\n        System.out.println(\"观众鼓掌\");\n    } catch (Throwable throwable) {\n        System.out.println(\"表演出意外了,xxx退钱!!\");\n    }\n}\n```\n\n`joinPoint.proceed()`用于调用被通知的方法，若不调用这个方法，那么你的通知实际上会阻塞对被通知方法的调用，同时我们也可以多次调用被通知的方法。-\n', '[1000,1004]', 10000, 1, 1, 0, '2023-09-03 09:58:02', '2023-09-03 09:58:34');
INSERT INTO `article_draft` VALUES (4, 1, 4, '超超超', 'https://static-oss.wang3.top/hami-images/64f5ca29e5c702298f2996c3.jpg', '法国丰富反反复复反反复复凤飞飞发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发发得到', '# 我爱JAVa\n## 爱你妹', '[1001]', 10000, 1, 1, 0, '2023-09-04 20:15:03', '2023-09-07 19:46:10');
INSERT INTO `article_draft` VALUES (5, 1, 100002, '测试测试测试超超超', '', 'efwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwh654尴尬土豪金', 'grfeokgjperigfjnfshnlirgwndwihfergprgregghtojj工具4O记估计会反而更火龙果普通话飞', '[1000,1001]', 10000, 1, 12, 0, '2023-09-07 19:52:31', '2023-09-07 20:21:55');

-- ----------------------------
-- Table structure for article_stat
-- ----------------------------
DROP TABLE IF EXISTS `article_stat`;
CREATE TABLE `article_stat`  (
  `article_id` int NOT NULL COMMENT '文章ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `views` int NOT NULL DEFAULT 0 COMMENT '阅读量',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comments` int NOT NULL DEFAULT 0 COMMENT '评论数',
  `collects` int NOT NULL DEFAULT 0 COMMENT '收藏数',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `hot_rank` decimal(10, 2) GENERATED ALWAYS AS (((((`likes` * 10) + (`comments` * 2)) + (`collects` * 3)) + (`views` * 0.33))) STORED NULL,
  PRIMARY KEY (`article_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_hot_index`(`hot_rank` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章数据记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article_stat
-- ----------------------------

-- ----------------------------
-- Table structure for article_tag
-- ----------------------------
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID,主键',
  `article_id` int NOT NULL COMMENT '文章ID',
  `tag_id` int UNSIGNED NOT NULL COMMENT '标签ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_tag_id`(`article_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章标签' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article_tag
-- ----------------------------

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '类目名称',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '路径',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10008 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (10000, '后端', 'backend', 0, '2023-03-10 12:04:11', '2023-03-21 20:12:19');
INSERT INTO `category` VALUES (10001, '前端', 'frontend', 0, '2023-03-10 12:04:31', '2023-03-21 20:12:21');
INSERT INTO `category` VALUES (10002, 'Android', 'android', 0, '2023-03-10 12:04:49', '2023-04-26 00:22:05');
INSERT INTO `category` VALUES (10003, 'IOS', 'ios', 0, '2023-03-10 12:05:22', '2023-03-21 20:12:26');
INSERT INTO `category` VALUES (10004, '人工智能', 'ai', 0, '2023-03-10 12:05:40', '2023-03-21 20:12:28');
INSERT INTO `category` VALUES (10005, '开发工具', 'tool', 0, '2023-03-10 12:06:01', '2023-03-21 20:13:20');
INSERT INTO `category` VALUES (10006, '代码人生', 'coding', 0, '2023-03-10 12:06:49', '2023-03-21 20:12:36');
INSERT INTO `category` VALUES (10007, '阅读', 'reading', 0, '2023-03-10 12:07:11', '2023-03-21 20:12:39');

-- ----------------------------
-- Table structure for collection
-- ----------------------------
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '收藏夹标题',
  `user_id` int NOT NULL COMMENT '用户ID',
  `public` tinyint NOT NULL COMMENT '是否公开 0-私有 1-公开',
  `default` tinyint NOT NULL DEFAULT 0 COMMENT '默认收藏夹 0-否 1是',
  `article_count` int NOT NULL DEFAULT 0 COMMENT '文章数量',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏夹' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of collection
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` int NOT NULL DEFAULT 0 COMMENT '文章ID',
  `user_id` int NOT NULL DEFAULT 0 COMMENT '用户ID',
  `is_author` bit(1) NOT NULL COMMENT '是否是作者评论',
  `ip_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '评论时的IP信息',
  `content` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '评论内容',
  `content_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '评论图片',
  `root_id` int NOT NULL DEFAULT 0 COMMENT '顶级评论ID 0-表示是根评论',
  `parent_id` int NOT NULL DEFAULT 0 COMMENT '父评论ID',
  `reply_to` int NOT NULL DEFAULT 0 COMMENT '回复的用户ID',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_root_id`(`root_id` ASC) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC, `root_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------

-- ----------------------------
-- Table structure for file_detail
-- ----------------------------
DROP TABLE IF EXISTS `file_detail`;
CREATE TABLE `file_detail`  (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件id',
  `url` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件访问地址',
  `size` bigint NULL DEFAULT NULL COMMENT '文件大小，单位字节',
  `filename` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件名称',
  `original_filename` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `base_path` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '基础存储路径',
  `path` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '存储路径',
  `ext` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `content_type` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `platform` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '存储平台',
  `object_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件所属对象id',
  `object_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件所属对象类型，例如用户头像，评价图片',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件上传记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_detail
-- ----------------------------
INSERT INTO `file_detail` VALUES ('1694353276255977474', 'https://static-oss.wang3.top/hami-images/64e61512e5c7fb5bc9fb4079.jpg', 5096, '64e61512e5c7fb5bc9fb4079.jpg', '1688359845807.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '2', 'avatar', '2023-08-23 22:17:55');
INSERT INTO `file_detail` VALUES ('1694698234896834561', 'https://static-oss.wang3.top/hami-images/64e75657e5c7a6dcb6efeebb.jpg', 3954, '64e75657e5c7a6dcb6efeebb.jpg', '1688359906925.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '2', 'avatar', '2023-08-24 21:08:39');
INSERT INTO `file_detail` VALUES ('1696916535929425921', 'https://static-oss.wang3.top/hami-images/64ef684be5c7e1800b902fb2.jpeg', 548632, '64ef684be5c7e1800b902fb2.jpeg', 'b087664f2b568dabd253684081f14712.jpeg', 'hami-images/', '', 'jpeg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 00:03:23');
INSERT INTO `file_detail` VALUES ('1696917179281133569', 'https://static-oss.wang3.top/hami-images/64ef68e4e5c7e1800b902fb3.jpg', 422110, '64ef68e4e5c7e1800b902fb3.jpg', '1688521176568.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 00:05:57');
INSERT INTO `file_detail` VALUES ('1696926280446857217', 'https://static-oss.wang3.top/hami-images/64ef715ee5c7e1800b902fb4.jpg', 422110, '64ef715ee5c7e1800b902fb4.jpg', '1688521176568.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 00:42:07');
INSERT INTO `file_detail` VALUES ('1696928137000996866', 'https://static-oss.wang3.top/hami-images/64ef7319e5c7e1800b902fb5.png', 1022035, '64ef7319e5c7e1800b902fb5.png', 'image.png', 'hami-images/', '', 'png', 'image/png', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 00:49:30');
INSERT INTO `file_detail` VALUES ('1696928455524831233', 'https://static-oss.wang3.top/hami-images/64ef7365e5c7e1800b902fb6.png', 2565, '64ef7365e5c7e1800b902fb6.png', 'image.png', 'hami-images/', '', 'png', 'image/png', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 00:50:46');
INSERT INTO `file_detail` VALUES ('1696929086872440834', 'https://static-oss.wang3.top/hami-images/64ef73fce5c7e1800b902fb7.png', 2654, '64ef73fce5c7e1800b902fb7.png', 'image.png', 'hami-images/', '', 'png', 'image/png', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 00:53:16');
INSERT INTO `file_detail` VALUES ('1697166176255234049', 'https://static-oss.wang3.top/hami-images/64f050cae5c79bebce766c54.jpg', 1393519, '64f050cae5c79bebce766c54.jpg', 'WallpaperEngineLockOverride_randomPDPTOW.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 16:35:22');
INSERT INTO `file_detail` VALUES ('1697259715362316289', 'https://static-oss.wang3.top/hami-images/64f0a7e7e5c7a8eb5fb34e56.jpg', 1393519, '64f0a7e7e5c7a8eb5fb34e56.jpg', 'WallpaperEngineLockOverride_randomPDPTOW.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 22:47:04');
INSERT INTO `file_detail` VALUES ('1697259799546191874', 'https://static-oss.wang3.top/hami-images/64f0a7fce5c7a8eb5fb34e57.jpg', 1393519, '64f0a7fce5c7a8eb5fb34e57.jpg', 'WallpaperEngineLockOverride_randomPDPTOW.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-08-31 22:47:24');
INSERT INTO `file_detail` VALUES ('1697988608186548226', 'https://static-oss.wang3.top/hami-images/64f34ebde5c79f362da870b8.jpeg', 5456, '64f34ebde5c79f362da870b8.jpeg', 'b087664f2b568dabd253684081f14712.jpeg', 'hami-images/', '', 'jpeg', 'image/jpeg', 'tencent-cos-1', '1', 'avatar', '2023-09-02 23:03:25');
INSERT INTO `file_detail` VALUES ('1698151722517475329', 'https://static-oss.wang3.top/hami-images/64f3e6a6e5c79555236b492e.jpg', 1393519, '64f3e6a6e5c79555236b492e.jpg', 'WallpaperEngineLockOverride_randomPDPTOW.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '2', 'article-picture', '2023-09-03 09:51:35');
INSERT INTO `file_detail` VALUES ('1698153452760150018', 'https://static-oss.wang3.top/hami-images/64f3e843e5c79555236b492f.jpg', 1393519, '64f3e843e5c79555236b492f.jpg', 'WallpaperEngineLockOverride_randomPDPTOW.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '2', 'article-picture', '2023-09-03 09:58:27');
INSERT INTO `file_detail` VALUES ('1698670890325061634', 'https://static-oss.wang3.top/hami-images/64f5ca29e5c702298f2996c3.jpg', 1393519, '64f5ca29e5c702298f2996c3.jpg', 'WallpaperEngineLockOverride_randomPDPTOW.jpg', 'hami-images/', '', 'jpg', 'image/jpeg', 'tencent-cos-1', '1', 'article-picture', '2023-09-04 20:14:34');

-- ----------------------------
-- Table structure for login_record
-- ----------------------------
DROP TABLE IF EXISTS `login_record`;
CREATE TABLE `login_record`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `ip_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '登录的IP地址信息',
  `login_time` timestamp NULL DEFAULT NULL COMMENT '登录时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  `ctime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '登录记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of login_record
-- ----------------------------

-- ----------------------------
-- Table structure for notify_msg
-- ----------------------------
DROP TABLE IF EXISTS `notify_msg`;
CREATE TABLE `notify_msg`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` int NOT NULL DEFAULT -1 COMMENT '备用 ',
  `item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '对应的实体名称',
  `related_id` int NOT NULL DEFAULT 0 COMMENT '关联的主键',
  `sender` int NOT NULL DEFAULT 0 COMMENT '源用户ID(发送通知)',
  `receiver` int NOT NULL DEFAULT 0 COMMENT '目标用户ID(接收通知)',
  `detail` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` int NOT NULL DEFAULT 0 COMMENT '类型: 0-系统，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-关注的用户发布新文章',
  `state` tinyint NOT NULL DEFAULT 0 COMMENT '阅读状态: 0-未读，1-已读',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_type_state`(`receiver` ASC, `type` ASC, `state` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notify_msg
-- ----------------------------

-- ----------------------------
-- Table structure for reading_record
-- ----------------------------
DROP TABLE IF EXISTS `reading_record`;
CREATE TABLE `reading_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '作者ID',
  `article_id` int NOT NULL COMMENT '文章ID',
  `reading_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reading_time`(`reading_time` DESC) USING BTREE,
  INDEX `uk_user_article_id`(`user_id` ASC, `article_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '阅读记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of reading_record
-- ----------------------------

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `type` int NOT NULL DEFAULT 1 COMMENT '标签类型：1-系统标签，2-自定义标签',
  `category_id` int NOT NULL DEFAULT 0 COMMENT '类目ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_category`(`name` ASC, `category_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1061 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1000, '后端', 1, 10000, 0, '2023-03-21 19:41:18', '2023-03-21 19:41:18');
INSERT INTO `tag` VALUES (1001, 'Java', 1, 10000, 0, '2023-03-21 19:41:29', '2023-03-21 19:45:15');
INSERT INTO `tag` VALUES (1002, 'Go', 1, 10000, 0, '2023-03-21 19:41:37', '2023-03-21 19:45:18');
INSERT INTO `tag` VALUES (1003, '大数据', 1, 10000, 0, '2023-03-21 19:42:01', '2023-03-21 19:45:20');
INSERT INTO `tag` VALUES (1004, 'Spring', 1, 10000, 0, '2023-03-21 19:42:09', '2023-03-21 19:45:22');
INSERT INTO `tag` VALUES (1005, 'SpringBoot', 1, 10000, 0, '2023-03-21 19:42:16', '2023-03-21 19:45:23');
INSERT INTO `tag` VALUES (1006, 'Mybatis', 1, 10000, 0, '2023-03-21 19:42:21', '2023-03-21 19:45:24');
INSERT INTO `tag` VALUES (1007, 'SpringMVC', 1, 10000, 0, '2023-03-21 19:42:29', '2023-03-21 19:45:27');
INSERT INTO `tag` VALUES (1008, 'Kubernetes', 1, 10000, 0, '2023-03-21 19:43:25', '2023-03-21 19:45:28');
INSERT INTO `tag` VALUES (1009, 'Python', 1, 10000, 0, '2023-03-21 19:43:38', '2023-03-21 19:45:29');
INSERT INTO `tag` VALUES (1010, 'ElasticSearch', 1, 10000, 0, '2023-03-21 19:44:59', '2023-03-21 19:45:31');
INSERT INTO `tag` VALUES (1011, '前端', 1, 10001, 0, '2023-03-21 19:45:55', '2023-03-21 19:47:12');
INSERT INTO `tag` VALUES (1012, 'JavaScript', 1, 10001, 0, '2023-03-21 19:45:59', '2023-03-21 19:47:13');
INSERT INTO `tag` VALUES (1013, 'Vue.js', 1, 10001, 0, '2023-03-21 19:46:06', '2023-03-21 19:47:13');
INSERT INTO `tag` VALUES (1014, 'React.js', 1, 10001, 0, '2023-03-21 19:46:22', '2023-03-21 19:47:14');
INSERT INTO `tag` VALUES (1015, 'CSS', 1, 10001, 0, '2023-03-21 19:46:25', '2023-03-21 19:47:15');
INSERT INTO `tag` VALUES (1016, 'TypeScript', 1, 10001, 0, '2023-03-21 19:46:29', '2023-03-21 19:47:16');
INSERT INTO `tag` VALUES (1017, 'Node.js', 1, 10001, 0, '2023-03-21 19:46:38', '2023-03-21 19:47:16');
INSERT INTO `tag` VALUES (1018, 'Webpack', 1, 10001, 0, '2023-03-21 19:47:03', '2023-03-21 19:47:18');
INSERT INTO `tag` VALUES (1019, 'Android', 1, 10002, 0, '2023-03-21 19:47:37', '2023-03-21 19:48:37');
INSERT INTO `tag` VALUES (1020, 'Kotlin', 1, 10002, 0, '2023-03-21 19:48:10', '2023-03-21 19:48:43');
INSERT INTO `tag` VALUES (1021, 'Android Jetpack', 1, 10002, 0, '2023-03-21 19:48:21', '2023-03-21 19:48:55');
INSERT INTO `tag` VALUES (1022, 'IOS', 1, 10003, 0, '2023-03-21 19:49:00', '2023-03-21 19:50:00');
INSERT INTO `tag` VALUES (1023, 'Swift', 1, 10003, 0, '2023-03-21 19:49:14', '2023-03-21 19:50:02');
INSERT INTO `tag` VALUES (1024, 'Object-C', 1, 10003, 0, '2023-03-21 19:49:18', '2023-03-21 19:50:03');
INSERT INTO `tag` VALUES (1025, 'SwiftUI', 1, 10003, 0, '2023-03-21 19:49:38', '2023-03-21 19:50:05');
INSERT INTO `tag` VALUES (1026, 'Xcode', 1, 10003, 0, '2023-03-21 19:49:42', '2023-03-21 19:50:06');
INSERT INTO `tag` VALUES (1027, 'MacOs', 1, 10003, 0, '2023-03-21 19:49:48', '2023-03-21 19:50:18');
INSERT INTO `tag` VALUES (1028, 'ChatGPT', 1, 10004, 0, '2023-03-21 19:50:25', '2023-03-21 19:51:16');
INSERT INTO `tag` VALUES (1029, 'GPT', 1, 10004, 0, '2023-03-21 19:50:28', '2023-03-21 19:51:18');
INSERT INTO `tag` VALUES (1030, '深度学习', 1, 10004, 0, '2023-03-21 19:50:41', '2023-03-21 19:51:19');
INSERT INTO `tag` VALUES (1031, '机器学习', 1, 10004, 0, '2023-03-21 19:50:50', '2023-03-21 19:51:20');
INSERT INTO `tag` VALUES (1032, '计算机视觉', 1, 10004, 0, '2023-03-21 19:51:00', '2023-03-21 19:51:21');
INSERT INTO `tag` VALUES (1033, 'OpenAI', 1, 10004, 0, '2023-03-21 19:51:11', '2023-03-21 19:51:48');
INSERT INTO `tag` VALUES (1034, 'Git', 1, 10005, 0, '2023-03-21 19:51:57', '2023-03-21 19:54:14');
INSERT INTO `tag` VALUES (1035, 'GitHub', 1, 10005, 0, '2023-03-21 19:52:20', '2023-03-21 19:54:17');
INSERT INTO `tag` VALUES (1036, 'IDEA', 1, 10005, 0, '2023-03-21 19:52:30', '2023-03-21 19:54:20');
INSERT INTO `tag` VALUES (1037, 'VSCode', 1, 10005, 0, '2023-03-21 19:52:58', '2023-03-21 19:54:23');
INSERT INTO `tag` VALUES (1038, 'DevC++', 1, 10005, 0, '2023-03-21 19:53:30', '2023-03-21 19:54:26');
INSERT INTO `tag` VALUES (1039, '代码规范', 1, 10006, 0, '2023-03-21 19:53:53', '2023-03-21 19:54:31');
INSERT INTO `tag` VALUES (1040, '年终总结', 1, 10006, 0, '2023-03-21 19:54:04', '2023-03-21 19:54:34');
INSERT INTO `tag` VALUES (1041, '测试', 1, 10006, 0, '2023-03-21 19:54:08', '2023-03-21 19:54:37');
INSERT INTO `tag` VALUES (1042, '架构', 1, 10006, 0, '2023-03-21 19:54:11', '2023-03-21 19:54:41');
INSERT INTO `tag` VALUES (1043, '程序员', 1, 0, 0, '2023-03-21 19:55:00', '2023-03-21 19:55:00');
INSERT INTO `tag` VALUES (1044, '面试', 1, 0, 0, '2023-03-21 19:55:05', '2023-03-21 19:55:05');
INSERT INTO `tag` VALUES (1045, '产品', 1, 0, 0, '2023-03-21 19:55:21', '2023-03-21 19:55:21');
INSERT INTO `tag` VALUES (1046, '数据库', 1, 10000, 0, '2023-03-21 19:55:31', '2023-03-21 19:55:31');
INSERT INTO `tag` VALUES (1047, 'Linux', 1, 0, 0, '2023-03-21 19:57:34', '2023-03-21 19:57:34');
INSERT INTO `tag` VALUES (1048, 'Windows', 1, 0, 0, '2023-03-21 19:57:34', '2023-04-25 20:15:37');
INSERT INTO `tag` VALUES (1049, 'Oracle', 1, 10000, 0, '2023-03-21 19:59:09', '2023-04-25 20:15:31');
INSERT INTO `tag` VALUES (1050, 'MySQL', 1, 10000, 0, '2023-03-21 19:59:09', '2023-03-21 20:01:28');
INSERT INTO `tag` VALUES (1051, '算法', 1, 0, 0, '2023-03-21 20:01:46', '2023-03-21 20:01:46');
INSERT INTO `tag` VALUES (1052, 'HTML', 1, 10001, 0, '2023-03-21 20:01:50', '2023-03-21 20:02:15');
INSERT INTO `tag` VALUES (1053, '微信小程序', 1, 0, 0, '2023-03-21 20:02:20', '2023-03-21 20:02:20');
INSERT INTO `tag` VALUES (1054, 'Nginx', 1, 0, 0, '2023-03-21 20:02:39', '2023-03-21 20:02:39');
INSERT INTO `tag` VALUES (1055, 'JQuery', 1, 10001, 0, '2023-03-21 20:02:55', '2023-03-21 20:02:55');
INSERT INTO `tag` VALUES (1057, 'Canal', 1, 10005, 0, '2023-09-14 21:25:42', '2023-09-14 21:25:42');
INSERT INTO `tag` VALUES (1059, 'Canal', 1, 10003, 0, '2023-09-05 13:49:26', '2023-09-05 14:23:00');
INSERT INTO `tag` VALUES (1060, 'Flink', 1, 10005, 0, '2023-09-05 14:26:08', '2023-09-05 14:29:13');

-- ----------------------------
-- Table structure for tb_like
-- ----------------------------
DROP TABLE IF EXISTS `tb_like`;
CREATE TABLE `tb_like`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` int NOT NULL COMMENT '实体ID -文章/评论',
  `item_type` tinyint NULL DEFAULT 1 COMMENT '实体类型 1-文章 2-评论',
  `liker_id` int NOT NULL COMMENT '点赞人ID',
  `state` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-未点赞 1-点赞',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_liker`(`item_id` ASC, `liker_id` ASC) USING BTREE,
  INDEX `idx_liker_id_type`(`item_type` ASC, `liker_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞通用表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_like
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL COMMENT '用户账号ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `avatar` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '头像',
  `position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '职位',
  `company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '公司',
  `profile` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '个人简介',
  `blog` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '个人主页',
  `tag` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '标签',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-删除',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `following` int NOT NULL COMMENT '关注的用户ID',
  `state` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-未关注 1关注',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow`(`user_id` ASC, `following` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follow
-- ----------------------------

-- ----------------------------
-- Procedure structure for insert_article_data
-- ----------------------------
DROP PROCEDURE IF EXISTS `insert_article_data`;
delimiter ;;
CREATE PROCEDURE `insert_article_data`()
BEGIN
    DECLARE a INT;
    DECLARE b INT;
    DECLARE i INT DEFAULT 1;

    SET @max_id = (SELECT MAX(id) FROM article);

    WHILE i <= @max_id DO
        SELECT id, user_id
        INTO a, b
        FROM article
        WHERE id = i;

        INSERT INTO article_stat (`article_id`, `user_id`)
        VALUES (a, b);

        SET i = i + 1;
    END WHILE;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
