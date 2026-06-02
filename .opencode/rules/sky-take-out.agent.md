---
description: sky-take-out 苍穹外卖项目规范
globs: 
---
# sky-take-out 项目规则

## 数据流转

```
前端 JSON → Controller(@RequestBody DTO) → Service(入参 DTO)
Service(出参 VO) → Controller → 前端 JSON
数据库 ←→ Mapper(Entity)
```

## 三层 POJO 职责

| 包 | 用途 | 说明 |
|---|---|---|
| `sky-pojo/.../entity` | 与数据库表字段一一映射 | MyBatis ORM 使用 |
| `sky-pojo/.../dto` | Controller 接收前端参数 → Service | 可含多个 Entity 字段组合 |
| `sky-pojo/.../vo` | Service → Controller → 前端 | 可含额外字段（如 `categoryName`） |

## 分层校验原则

### Controller 层（格式校验）

- **Jakarta Bean Validation 注解**：DTO 字段用 `@NotBlank` / `@NotNull` / `@Size(max=N)` / `@Pattern(regexp=...)` / `@Positive` / `@Min(N)`
- **`@Valid`**：标注在 Controller 方法参数的 DTO 上触发校验
- **`@Validated`**：标注在 Controller 类上，支持 `@PathVariable`/`@RequestParam` 校验
- **手动 if-check**：DTO 内部分字段 save/update 要求不同时（如 `id` save 时 null、update 时必传，`status`/`type` 等有限取值字段），在 Controller 中手动校验后抛出 `IllegalException`，不写在 Service 中
- 校验规则依据：字段长度/约束来自 `docs/数据库设计文档.md`，必填/可选来自 `docs/苍穹外卖-管理端接口.md`

### Service 层（业务校验）

仅保留业务校验，绝不包含格式/参数校验：

- 唯一性检查（账号已存在、名称重复）
- 存在性检查（账号不存在、分类不存在）
- 引用完整性检查（分类关联菜品/套餐不可删除；菜品/套餐起售中不可删除）
- 权限检查（账号锁定、密码比对）

### GlobalExceptionHandler

| Handler | Exception | 响应 |
|---------|-----------|------|
| 1 | `BaseException` 及其子类 | `log.warn` → `Result.error(ex.getMessage())` |
| 2 | `NoResourceFoundException` | `Result.error(MessageConstant.Server.RESOURCE_NOT_FOUND)` |
| 3 | `ConstraintViolationException` | `log.warn` → `Result.error("参数格式错误: " + ex.getMessage())` |
| 4 | `Exception`（兜底） | `log.error` → `Result.error(MessageConstant.Server.ERROR)` |

## 项目技术栈

| 技术 | 版本 / 说明 |
|------|------------|
| Spring Boot | 3.2.5（Jakarta EE） |
| Java | 17 |
| Kotlin | 2.3.10（与 Java 混用，统一放在 `src/main/java`） |
| ORM | MyBatis + PageHelper |
| 构建 | Maven 多模块（sky-common / sky-pojo / sky-server） |
| 接口文档 | Knife4j 4.5.0（SpringDoc OpenAPI v3） |
| 数据库 | MySQL（druid 连接池） |
| 缓存 | Redis（redisson） |
| 日志 | SLF4J + Logback |
| 对象工具 | Lombok + BeanUtils(copyProperties) |

## Entity / VO / DTO 规范

| 类型 | Lombok 注解 |
|------|------------|
| Entity | `@Data @Builder @NoArgsConstructor @AllArgsConstructor` |
| VO | `@Data @Builder @NoArgsConstructor @AllArgsConstructor`（可选 `@Schema`） |
| DTO | `@Data`（可选 `@Schema`，可选 Jakarta Validation 注解） |

- 所有 POJO 实现 `Serializable` + `serialVersionUID`
- Entity 字段：`Long id`, `String`, `BigDecimal`, `Integer`, `LocalDateTime`
- Entity 公共审计字段：`createTime`, `updateTime`, `createUser`, `updateUser`（由 AOP `@AutoFill` 自动填充）
- VO 在 Entity 基础上可增加额外字段（如 `categoryName`, `flavors`, `setmealDishes`）

## Controller 层规范

- 类标记 `@Tag(name = "中文模块名")` + `@RestController` + `@RequestMapping("/模块")`
- 每个方法标记 `@Operation(summary = "中文描述")`
- 方法命名：`save`(POST)、`pageQuery`(GET)、`update`(PUT)、`deleteById`(DELETE)、`startOrStop`(POST)
- 参数校验：`@Valid` DTO + 可选的 `@Validated` 类级别 + 手动 if-check
- 调用 Service 前 `log.info("操作描述,参数:{}", param)` 记录日志
- 日志：Java 用 `@Slf4j`，Kotlin 用 `LoggerFactory.getLogger(XxxController::class.java)`
- 返回类型：始终是 `Result<T>`（统一包装，code / msg / data）
- 返回格式：无数据返回写 `Result.success()`，查询返回写 `Result.success(data)`，分页写 `Result.success(PageResult)`

## Service 层规范

- 接口定义在 `com.sky.service`，实现类在 `com.sky.service.impl`
- CRUD 方法名与 Controller 一致（`save`, `pageQuery`, `update`, `deleteById` 等）
- 写操作涉及多表时必须加 `@Transactional`
- 批量删除流程：校验业务约束 → 删子表数据 → 删主表数据
- VO 组装：
  - `BeanUtils.copyProperties(entity/entityList, vo)` + 手动设置额外字段
  - Kotlin 也可用 `.apply { ... }` 模式
- 查询不分页用 `List<T>`，分页用 `PageHelper`：
  ```kotlin
  PageHelper.startPage(dto.page, dto.pageSize)
  Page<VO> page = mapper.selectByPage(dto)
  return PageResult(page.total, page.result)
  ```
- 方法写 KDoc/JavaDoc 注释说明业务逻辑步骤，方法体内每步用 `// 步骤说明` 标注
- 日志：Java 用 `@Slf4j`，Kotlin 用 `LoggerFactory.getLogger`

## MessageConstant 规范

**单文件** `sky-common/.../constant/MessageConstant.java`，按模块分 nested class：

```java
public class MessageConstant {
    public static class Param {
        public static final String REQUIRED = "必填字段为空";
        public static final String TOO_LONG_OR_BLANK = "过长或为空";
        public static final String NOT_IN_RANGE = "不在允许范围内";
        public static final String ALREADY_EXISTS = "已存在";
    }
    public static class Employee { ... }
    public static class Category { ... }
    public static class Dish { ... }
    public static class Setmeal { ... }
    public static class Order { ... }
    public static class Login { ... }
    public static class Server { ... }
}
```

引用方式：`MessageConstant.Param.REQUIRED`, `MessageConstant.Employee.NOT_FOUND`。

## 模块级常量规范

`sky-common/.../constant/*Constant.kt`，Kotlin `object` 内含 `const val` 字段名 + 嵌套 `enum class`：

```kotlin
object EmployeeConstant {
    const val STATUS: String = "状态"
    enum class Status(val code: Int, val desc: String) {
        ENABLE(1, "启用"),
        DISABLE(0, "禁用");
        companion object {
            @JvmStatic
            fun contains(v: Int): Boolean = entries.any { it.code == v }
        }
    }
}
```

- 枚举的 `code` 是存数据库的原始值，`desc` 是中文描述
- Java 调用方用 `.getCode()` / `.getDesc()` / `.contains(v)`
- Kotlin 调用方用 `.code` / `.desc`

## Mapper 层规范

### 接口

- **返回值类型必须是 Entity，不是 VO**（VO 在 Service 组装）
- 新增/修改方法标注 `@AutoFill(OperationType.INSERT)` / `@AutoFill(OperationType.UPDATE)`
- `@Select` 仅用于极简查询，复杂 SQL 统一写在 XML

### XML

- 所有 `resultType` 写全限定类名（如 `com.sky.entity.Dish`）
- **禁用 `select *`**，必须显式列出所有列名
- 动态条件：`<where>` + `<if test="...">`
- 模糊查询：`like concat('%', #{name}, '%')`（不用 `${}`）
- 动态更新：`<set>` + `<if test="...">`
- 批量操作：`<foreach collection="..." item="..." open="(" separator="," close=")">`
- INSERT 返回主键：`useGeneratedKeys="true" keyProperty="id"`
- LEFT JOIN 用于关联查询（如 dish 联 category 查 categoryName）

## 异常体系

```
RuntimeException
  └── BaseException (abstract)
        ├── IllegalException            (参数校验失败 —— Controller 层抛出)
        ├── AlreadyExistedException     (唯一性冲突)
        ├── AccountNotFoundException
        ├── AccountLockedException
        ├── PasswordErrorException
        ├── PasswordEditFailedException
        ├── LoginFailedException
        ├── DeletionNotAllowedException (引用完整性)
        ├── SetmealEnableFailedException
        ├── AddressBookBusinessException
        ├── OrderBusinessException
        ├── ShoppingCartBusinessException
        └── UserNotLoginException
```

## 控制器命名冲突解决

不同包下同名 `@RequestMapping` 的 Controller，使用 `@RestController("xxx")` 的 Bean 名称区分：

```kotlin
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@RestController("userShopController")
@RequestMapping("/user/shop")
```

## @AutoFill AOP 拦截器

自定义 `@AutoFill` 注解，通过 `AutoFillAspect` 切面自动为 Entity 注入审计字段：

- INSERT 操作：设置 `createTime`, `updateTime`, `createUser`, `updateUser`
- UPDATE 操作：设置 `updateTime`, `updateUser`
- 用户 ID 从 `BaseContext.getCurrentId()` 的 `ThreadLocal` 获取

## JWT 拦截器

- `JwtTokenAdminInterceptor`：拦截 `/admin/**` 路径
- `JwtTokenUserInterceptor`：拦截 `/user/**` 路径（除 `/user/user/login`）
- 通过 `WebMvcConfiguration` 注册拦截器，并排除 Knife4j 静态资源路径

## MyBatis 配置

- `mybatis-plus` 不存在，仅用 `mybatis-spring-boot-starter`
- config-location: `classpath:mybatis-config.xml`（下划线转驼峰、二级缓存等）
- mapper-locations: `classpath:mapper/*.xml`
- type-aliases-package: `com.sky.entity`

## 文档注释规范

- **所有类、接口、方法**必须写文档注释（JavaDoc 或 KDoc）
- Controller 方法：`@Operation(summary = "中文描述")` + `/** 中文说明 */`
- Service 接口方法：`/** 方法说明 + @param + @return */`
- Service 实现方法：`/** 方法说明 + 分步骤中文说明业务逻辑 */`，方法体内每步用 `// 步骤说明` 标注
- Mapper 接口方法：`/** 方法说明 + @param + @return */`
- Mapper XML：SQL 块上方写 `<!-- 方法说明 -->`
