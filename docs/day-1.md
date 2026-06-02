# day1 · 项目部署 / 环境搭建

## 1. 使用 Agent CLI 工具 OpenCode 快速将项目从 Spring Boot 2.7.3 重构为 3.2.5

### 📋 修改文件清单（共 10 个文件）

| 文件 | 改动内容 |
| --- | --- |
| `pom.xml`（根） | Spring Boot 2.7.3 → 3.2.5，添加 `<java.version>17</java.version>`，升级所有依赖版本 |
| `sky-server/pom.xml` | `mysql-connector-java` → `mysql-connector-j`，`knife4j-spring-boot-starter` → `knife4j-openapi3-jakarta-spring-boot-starter`，`javax.xml.bind:jaxb-api` → `jakarta.xml.bind:jakarta.xml.bind-api`，新增 `spring-boot-starter-validation` |
| `sky-common/pom.xml` | `commons-lang:commons-lang` → `org.apache.commons:commons-lang3`，`javax.xml.bind:jaxb-api` → `jakarta.xml.bind:jakarta.xml.bind-api` |
| `sky-pojo/pom.xml` | 删除显式 `jackson-databind:2.9.2`，`knife4j-spring-boot-starter` → `knife4j-openapi3-jakarta-spring-boot-starter` |
| `JwtTokenAdminInterceptor.java` | `javax.servlet.http.*` → `jakarta.servlet.http.*` |
| `JwtUtil.java` | jjwt 0.9.1（旧 API）→ 0.12.6（新 API）：`Keys.hmacShaKeyFor`、`signWith(key)`、`verifyWith(key).build().parseSignedClaims().getPayload()` |
| `WeChatPayUtil.java` | `org.apache.commons.lang.RandomStringUtils` → `org.apache.commons.lang3.RandomStringUtils` |
| `WebMvcConfiguration.java` | 完全重写：`extends WebMvcConfigurationSupport` → `implements WebMvcConfigurer`，Springfox Docket → Springdoc OpenAPI + GroupedOpenApi，移除手动资源映射 |

### ⚙️ 关键版本变更

| 依赖 | 旧版 | 新版 |
| --- | --- | --- |
| Spring Boot | 2.7.3 | 3.2.5 |
| Java | 8 / 11 | 17 |
| MyBatis | 2.2.0 | 3.0.3 |
| PageHelper | 1.3.0 | 2.1.0 |
| Druid | 1.2.1 | 1.2.22 |
| jjwt | 0.9.1 | 0.12.6 |
| Knife4j | 3.0.2 (swagger2) | 4.5.0 (OpenAPI 3 + jakarta) |
| POI | 3.16 | 5.2.5 |
| AspectJ | 1.9.4 | 1.9.22 |
| Lombok | 1.18.20 | 1.18.32 |
| commons-lang | 2.6（→ commons-lang3） | 3.14.0 |
| MySQL driver | `mysql-connector-java` | `mysql-connector-j` |
| JAXB | `javax.xml.bind:jaxb-api` | `jakarta.xml.bind:jakarta.xml.bind-api` |

## 2. 搭建前端环境

将后端配置的端口从 8080 改为 9100，在 nginx 的配置中将 8080 端口改为 9100。

**缺少 temp 目录以及其子目录：** 在 `nginx-1.20.2/` 下创建 `temp/` 目录及子目录：`client_body_temp`、`proxy_temp`、`fastcgi_temp`、`scgi_temp`、`uwsgi_temp`。

**解决端口 80 冲突：** 在 `nginx.conf` 中将 `listen 80` 改为其他端口 9101。

## 3. 搭建后端环境

将 `application-dev.yml` 里的 MySQL 端口 / 账户 / 密码修改为本地 Docker 部署的 MySQL 容器。

搭建 git 环境，配置用户名和邮箱，将初始代码推送至 GitHub。由于使用私密邮箱导致推送被拒，查看 GitHub 设置后通过 `git config user.email` 和 `git commit --amend --author=` 将邮箱换为 GitHub 提供的邮箱，成功推送。

## 4. 搭建数据库环境

通过 IDEA 连接到 Docker 中的 MySQL 容器，使用 IDEA 管理数据库，运行提供的 SQL 脚本批量创建表。

[数据库设计文档](./数据库设计文档.md)

## 5. 前后端联调

启动 Nginx 服务器，启动后端工程，发现出现错误。借助 OpenCode 帮助后仔细查找发现，重构后 Spring Boot 不认识 `application.yml` 原先的写法 `spring.datasource.druid.url`，于是改为 `spring.datasource.url`。

前端尝试登录时后端发生异常：

```
io.jsonwebtoken.security.WeakKeyException: The specified key byte array is 48 bits
which is not secure enough for any JWT HMAC-SHA algorithm. The JWT JWA Specification
(RFC 7518, Section 3.2) states that keys used with HMAC-SHA algorithms MUST have a
size >= 256 bits (the key size must be greater than or equal to the hash output size).
Consider using the Jwts.SIG.HS256.key() builder (or HS384.key() or HS512.key()) to
create a key guaranteed to be secure enough for your preferred HMAC-SHA algorithm.
```

原因是 jjwt 0.12.6 要求 HMAC-SHA 密钥至少 256 位（32 字节），于是将原来的密钥 `itcast` 修改为更长的密钥。

**登陆成功。**

## 6. 熟悉代码

- 跟踪登录请求，了解业务流程
- 为全局异常处理类里新增一个接受所有异常的方法
- 命令 OpenCode 为所有现存代码补上注释
- 将修改完的代码提交到仓库并推送到 GitHub

## 7. 完善员工登录功能

修改数据库，将原来的密码换成 MD5 加密后的密文。

前端尝试登录，跟预期一样密码错误。

修改登录逻辑，将传入的明文 password 通过 `DigestUtils.md5DigestAsHex` 转换为密文，再与数据库查出的密码字段对比。

## 8. 导入接口文档

使用 Apifox，选择导入格式为 YApi，导入管理端和客户端的接口文档（JSON）。

引入 Swagger、Knife4j 依赖、配置类，`implements WebMvcConfigurer` 不必主动配置静态资源映射。

为 `EmployeeController` 添加 API 相关注解，注意在新版本注解改变如下：

| Swagger 2 (Springfox) | OpenAPI 3 (Springdoc / Knife4j 4.x) | 包路径 |
| --- | --- | --- |
| `@Api` | `@Tag` | `io.swagger.v3.oas.annotations.tags.Tag` |
| `@ApiOperation` | `@Operation` | `io.swagger.v3.oas.annotations.Operation` |
| `@ApiModelProperty` | `@Schema` | `io.swagger.v3.oas.annotations.media.Schema` |
| `@ApiIgnore` | `@Hidden` | `io.swagger.v3.oas.annotations.Hidden` |
