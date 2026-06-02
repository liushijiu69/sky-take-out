# Day 5: Redis/Redisson 配置 + C端店铺接口&#x20;

## 完成的工作

### 1. Redis & Redisson 依赖管理

| 操作                                               | 位置                   |
| ------------------------------------------------ | -------------------- |
| 父 pom 新增 `redisson` 版本属性 `3.37.0`                | `pom.xml:33`         |
| 父 pom `dependencyManagement` 新增 Redisson starter | `pom.xml`            |
| `sky-server/pom.xml` 引入 Redisson 依赖（无版本号）        | `sky-server/pom.xml` |

### 2. RedisAndRedissonConfiguration 创建与优化

**新建文件：**
`sky-server/.../config/RedisAndRedissonConfiguration.kt`

**修复内容：**

- 参数名 `redisConnectFactory` → `redisConnectionFactory`（补全缺失的 `i`）
- 方法名 `RedisTemplate` → `redisTemplate`（Kotlin 驼峰小写）
- Redisson 地址/库号改为从 `application.yml` 读取（`@Value` 注入）

### 3. C端店铺状态接口

**新建文件：**
`sky-server/.../controller/user/ShopController.kt`

**端点：**

| 方法    | 路径                  | 说明       |
| ----- | ------------------- | -------- |
| `GET` | `/user/shop/status` | 获取店铺营业状态 |

直接复用 Service 层 `ShopService.getStatus()`（Redis 存储），无需新增 Service 代码。

### 4. Knife4j 文档页面修复

#### 问题现象

访问 `http://localhost:9100/doc.html` 时页面能加载，但弹出消息框提示 **"Knife4j文档请求异常"**，无法正常显示接口文档。

#### 排查过程

| 步骤 | 操作 | 结果 |
|------|------|------|
| 1 | 检查 `doc.html` 是否可访问 | ✅ 200（1.9KB，页面正常加载） |
| 2 | 检查 JS/CSS 资源块是否正常加载 | ✅ 全部 14 个 JS chunk 和 2 个 CSS chunk 均返回 200 |
| 3 | 检查 `/v3/api-docs/swagger-config` 配置端点 | ✅ 200，返回正确的 Swagger 配置 JSON |
| 4 | 检查 `/v3/api-docs` 和 `/v3/api-docs/管理端接口` 端点 | ⚠️ 200，但用二进制检查首字节发现：**首字节为 `0x22`（`"`）而非正常的 `0x7b`（`{`）** |
| 5 | 用 StreamReader 读取原始响应体 | 发现响应体是 **`"<Base64编码的OpenAPI JSON>"`**——一个 JSON 字符串，内容为 Base64 编码的 OpenAPI 规范 |
| 6 | 对比 `/v3/api-docs/swagger-config` 端点 | ✅ 首字节 `0x7b`，为正常 JSON 对象 |
| 7 | 定位到 `GlobalExceptionHandler` 捕获 `NoResourceFoundException` | 返回 JSON (`{"code":0,"msg":"访问的资源不存在!"}`)，可能干扰某些静态资源请求，但非根本原因 |
| 8 | 深入分析 `JacksonObjectMapper` + `extendMessageConverters` | **确认根本原因** |

#### 根本原因

`WebMvcConfiguration` 中 `extendMessageConverters` 方法将自定义的 `MappingJackson2HttpMessageConverter`（使用 `JacksonObjectMapper`）插入到 Spring MVC 消息转换器队列的**第一位（priority 0）**。

`JacksonObjectMapper` 直接 `extends ObjectMapper`，**跳过了 Spring Boot 的 Jackson 自动配置**，导致：
1. 缺少 Swagger/Springdoc 所需的 Jackson 模块（`swagger-core` 的 OpenAPI 序列化模块）
2. Spring Boot 的 `jackson-datatype-jsr310` 等自动配置模块也未注册
3. 当 Springdoc 控制器返回 `OpenAPI` 对象时，由于缺少正确的序列化器，被异常序列化为 **Base64 编码的 JSON 字符串**

Knife4j 前端收到 Base64 字符串而非正常 JSON 对象，解析失败，弹出 **"Knife4j文档请求异常"**。

#### 修复方案

**第一步：删除自定义消息转换器**

删除 `WebMvcConfiguration` 中的 `extendMessageConverters` 方法及相关导入（`JacksonObjectMapper`、`MappingJackson2HttpMessageConverter`、`HttpMessageConverter`、`List`），让 Spring Boot 使用自动配置的 `ObjectMapper`。

**第二步：使用 Spring Boot 全局 Jackson 配置**

在 `application.yml` 中添加：

```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm
    time-zone: Asia/Shanghai
    deserialization:
      fail-on-unknown-properties: false
    serialization:
      write-dates-as-timestamps: false
```

等价替代原 `JacksonObjectMapper` 中三个功能：
- `FAIL_ON_UNKNOWN_PROPERTIES = false`
- 自定义日期格式（`yyyy-MM-dd HH:mm`）
- 时间区设置

#### 验证结果

重启后：

| 端点 | 修复前 | 修复后 |
|------|--------|--------|
| `/v3/api-docs` | `"<Base64>"`（0x22 开头） | ✅ `{...}`（0x7b 开头） |
| `/v3/api-docs/管理端接口` | `"<Base64>"`（0x22 开头） | ✅ `{...}`（0x7b 开头） |
| `/v3/api-docs/swagger-config` | ✅ 正常（未受影响） | ✅ 正常 |
| `http://localhost:9100/doc.html` | ⚠️ 页面加载但报错 | ✅ **页面正常显示接口文档** |

### 5. 新建文件清单（共 2 个）

| 文件                                                       | 说明                  |
| -------------------------------------------------------- | ------------------- |
| `sky-server/.../config/RedisAndRedissonConfiguration.kt` | Redis + Redisson 配置 |
| `sky-server/.../controller/user/ShopController.kt`       | C端营业状态 Controller   |

### 6. 修改文件清单（共 5 个）

| 文件 | 变更 |
|------|------|
| `pom.xml` (parent) | 新增 Redisson 版本属性 + dependencyManagement |
| `sky-server/pom.xml` | 新增 Redisson 依赖 |
| `application.yml` | 新增 `spring.jackson.*` 全局 Jackson 配置；删除 `springdoc.swagger-ui.urls` 配置 |
| `sky-server/.../config/WebMvcConfiguration.java` | 删除 `extendMessageConverters` 方法及相关导入；新增 `addResourceHandlers` 方法映射 Knife4j 静态资源 |
| `sky-server/.../handler/GlobalExceptionHandler.java` | 新增 `NoResourceFoundException` 异常处理器 |

## 技术要点

- **Knife4j 4.x + Spring Boot 3.x** 中需要手动添加 `ResourceHandler`（`WebMvcConfigurer.addResourceHandlers`）映射 `/doc.html` 和 `/webjars/**`
- **避免覆盖 Spring Boot 的 Jackson 自动配置**：若使用 `extendMessageConverters` 插入自定义 `ObjectMapper`，会丢失 Swagger/Springdoc 所需的 Jackson 模块，导致 OpenAPI 规范被序列化为 Base64 字符串
- 使用 `spring.jackson.*` 配置属性替代自定义 `ObjectMapper`，可以在保留自动配置的同时定制日期格式等行为
- **调试技巧**：通过检查 HTTP 响应首字节的十六进制值（`0x22` vs `0x7b`）快速判断 JSON 对象是否被异常包装为字符串
- **Redisson** **`@Value`** **属性注入** 可从 `spring.data.redis.*` 读取配置，保持与 Spring Data Redis 一致
- **用户端店铺接口** 只读（查询状态），复用了 Admin 端 `ShopService` 的 `getStatus()` 方法

