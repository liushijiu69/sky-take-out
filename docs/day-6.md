# Day 6: UserController 微信登录 + 商品浏览功能导入 + 校验重构 + MessageConstant 优化

## 完成的工作

### 1. C端用户微信登录接口

手动编写用户端微信登录功能，包括 Controller、Service、Mapper 及配套设施。

#### 涉及文件

| 文件 | 说明 |
|------|------|
| `UserController.kt` | 接收 `code` 参数，调用 Service 完成微信登录 |
| `UserLoginDTO.java` | 接收前端传来的临时 code |
| `UserLoginVO.java` | 返回 id / openid / token |
| `UserService.kt` | 接口定义 |
| `UserServiceImpl.kt` | 调用微信接口获取 openid、新用户自动注册、生成 JWT |
| `UserMapper.java` + `UserMapper.xml` | 根据 openid 查询 / 插入新用户 |
| `WeChatProperties.java` | 微信登录配置属性（appid / secret） |
| `JwtProperties.java` | JWT 密钥及过期时间配置（新增 user 相关字段） |
| `application.yml` | 新增 `sky.wechat` 和 `sky.jwt.user-*` 配置项 |

#### 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/user/user/login` | 微信登录，返回 JWT token |

#### 登录流程

1. Controller 接收 `code`，通过 `@Valid @RequestBody UserLoginDTO` 校验
2. Service 调用微信服务端 `jscode2session` 接口换取 `openid`
3. 根据 `openid` 查询数据库：存在则直接登录，不存在则自动注册新用户
4. 生成 JWT token 返回客户端

### 2. C端商品浏览功能导入

从 `docs/商品浏览` 导入用户端分类、菜品、套餐浏览接口，适配现有项目代码风格。

#### 新建 Controller（3 个）

| 文件 | 路径 | 接口 |
|------|------|------|
| `CategoryController.java` | `/user/category/list` | 查询分类列表 |
| `DishController.java` | `/user/dish/list` | 根据分类id查询菜品（仅起售） |
| `SetmealController.java` | `/user/setmeal/list` | 根据分类id查询套餐（仅起售） |
| | `/user/setmeal/dish/{id}` | 根据套餐id查询菜品列表 |

#### 水土不服修正

| 原代码问题 | 修正 |
|-----------|------|
| `@Api` / `@ApiOperation`（Swagger 2） | → `@Tag` / `@Operation`（Swagger 3 / OpenAPI 3） |
| `StatusConstant.ENABLE`（不存在） | → `DishConstant.DishStatus.ON_SALE.getCode()` / `SetmealConstant.SetmealStatus.ON_SALE.getCode()` |
| `DishService.listWithFlavor` 未定义 | 接口+实现新增方法 |
| `SetmealService.list` / `getDishItemById` 未定义 | 接口+实现新增方法 |
| `DishMapper.list(Dish)` 不存在 | 新增 mapper + XML 动态查询 |
| `SetmealMapper.list(Setmeal)` 不存在 | 新增 mapper + XML 动态查询 |
| `DishFlavorsMapper.getByDishId` 不存在 | → 已有 `selectByDishId` |
| Kotlin enum `code` Java 中直接访问 | → 改为 `getCode()` |
| `DishServiceImpl` / `SetmealServiceImpl` 为 Kotlin | 方法合并到现有 Kotlin 文件 |

#### 修改文件清单

| 文件 | 变更 |
|------|------|
| `DishService.kt` | 新增 `listWithFlavor(Dish)` |
| `SetmealService.kt` | 新增 `list(Setmeal)` / `getDishItemById(Long)` |
| `DishServiceImpl.kt` | 新增 `listWithFlavor` 实现 |
| `SetmealServiceImpl.kt` | 新增 `list` / `getDishItemById` 实现 |
| `DishMapper.java` | 新增 `list(Dish)` 方法 |
| `SetmealMapper.java` | 新增 `list(Setmeal)` / `getDishItemBySetmealId(Long)` |
| `DishMapper.xml` | 新增动态 `list` 查询 |
| `SetmealMapper.xml` | 新增动态 `list` 查询 |
| `CategoryMapper.xml` | 已存在 `list(Integer)`（兼容使用） |

---

### 2. 参数格式校验重构到 Controller 层

将全部参数格式校验（null、长度、范围、格式）从 Service 层前置到 Controller 层，使用 Jakarta Bean Validation 注解 + 手动 if-check，保留业务校验在 Service。

#### 实现方式

- `spring-boot-starter-validation` 已存在，无需新增依赖
- DTO 添加 `@NotBlank` / `@NotNull` / `@Size` / `@Positive` / `@Pattern` 注解
- Controller 添加 `@Valid` 触发校验，`@Validated` 支持 `@PathVariable`/`@RequestParam` 校验
- 因 save/update 共用 DTO 导致字段要求不同，`id`、`status`、`type`、`sort` 等使用手动 if-check

#### DTO 注解汇总

| DTO | 注解 |
|-----|------|
| `EmployeeLoginDTO` | `@NotBlank @Size(max=32) username`, `@NotBlank @Size(max=64) password` |
| `EmployeeDTO` | `@NotBlank name/username/phone/sex/idNumber`, `@Pattern` 手机号/身份证/性别 |
| `CategoryDTO` | `@Size(max=32) name` |
| `CategoryPageQueryDTO` | `@Min(1) page`, `@Min(1) pageSize` |
| `EmployeePageQueryDTO` | `@NotNull @Min(1) page`, `@NotNull @Min(1) pageSize` |
| `DishDTO` | `@NotBlank @Size(max=32) name`, `@NotNull categoryId/price/image`, `@Positive price` |
| `DishPageQueryDTO` | `@Min(1) page`, `@Min(1) pageSize` |
| `SetmealDTO` | `@NotBlank @Size(max=32) name`, `@NotNull categoryId/price/image`, `@Positive price` |
| `SetmealPageQueryDTO` | `@Min(1) page`, `@Min(1) pageSize` |
| `UserLoginDTO` | `@NotBlank code` |

#### Controller 手动格式校验

| Controller | 校验内容 |
|-----------|---------|
| `EmployeeController` | status 0/1 合法性；update 时 id 必传 |
| `CategoryController` | save 时 name null/长度、type 1/2、sort >=0；update 时 id 必传；type/status 合法性 |
| `DishController.kt` | update 时 id 必传；status 0/1 合法性 |
| `SetmealController.kt` | save 时 status 0/1、setmealDishes 非空+各字段非空；update 时 id 必传；status 0/1 合法性 |

#### 校验分层对比

```
Controller 层（新增）              Service 层（保留）
───────────────────────          ────────────────────────
@NotBlank / @Size / @Min         账号已存在（去重）
@Pattern(手机号/身份证/性别)       密码比对/账号锁定/不存在
手动 if-check: status/type       分类关联菜品/套餐
手动 if-check: id 非空            菜品起售中不可删
手动 if-check: setmealDishes     套餐起售中不可删
                                 categoryId/dishId 存在性
```

#### GlobalExceptionHandler 新增

```java
@ExceptionHandler
public Result handleConstraintViolation(ConstraintViolationException ex)
```

处理 `@Validated` + `@PathVariable`/`@RequestParam` 校验失败的异常。

#### 删除的格式校验代码统计

| ServiceImpl | 删除行数 | 保留的业务校验 |
|------------|---------|---------------|
| `EmployeeServiceImpl` | ~70 行 | 账号已存在(去重)、账号不存在、密码比对、账号锁定 |
| `CategoryServiceImpl` | ~35 行 | 分类关联菜品/套餐检查 |
| `DishServiceImpl.kt` | ~30 行 | 起售检查、套餐关联检查、资源不存在检查 |
| `SetmealServiceImpl.kt` | ~40 行 | categoryId/dishId 存在性、起售检查、资源不存在检查 |

---

### 3. MessageConstant 重构

将杂乱的常量按领域分组为 nested class，修复命名不一致。

#### 新旧结构对比

| 旧 | 新 |
|----|----|
| `ParamIllegal.PARAMETERS_ILLEGAL` | `Param.REQUIRED` |
| `ParamIllegal.TO_LONG_OR_BLANK` | `Param.TOO_LONG_OR_BLANK`（修复错别字） |
| `ParamIllegal.ALREADY_EXISTED` | `Param.ALREADY_EXISTS` |
| `ParamIllegal.FILE_HAS_NO_ORIGINAL_NAME` | `Param.FILE_NO_NAME` |
| `ServerError.SERVER_ERROR` | `Server.ERROR` |
| `ServerError.File_UPLOAD_ERROR` | `Server.UPLOAD_FAILED` |
| `LoginError.*` | `Employee.*`（领域归属员工模块） |
| 顶层 `CATEGORY_BE_RELATED_BY_DISH` 等 | `Category.LINKED_BY_DISH` / `Category.LINKED_BY_SETMEAL` |
| 顶层 `DISH_ON_SALE` / `DISH_BE_RELATED_BY_SETMEAL` | `Dish.ON_SALE_CANNOT_DELETE` / `Dish.LINKED_BY_SETMEAL` |
| 顶层 `SETMEAL_ON_SALE` / `SETMEAL_ENABLE_FAILED` | `Setmeal.ON_SALE_CANNOT_DELETE` / `Setmeal.ENABLE_FAILED` |
| 顶层 `SHOPPING_CART_IS_NULL` / `ADDRESS_BOOK_IS_NULL` / `ORDER_STATUS_ERROR` / `ORDER_NOT_FOUND` | `Order.CART_EMPTY` / `ADDRESS_EMPTY` / `STATUS_ERROR` / `NOT_FOUND` |
| `LOGIN_FAILED` / `PASSWORD_EDIT_FAILED` | `Login.FAILED` / `Login.PASSWORD_EDIT_FAILED` |

涉及 10 个源文件共 39 处调用更新。

## 新建文件清单（共 6 个）

| 文件 | 说明 |
|------|------|
| `sky-server/.../controller/user/CategoryController.java` | C端分类接口 |
| `sky-server/.../controller/user/DishController.java` | C端菜品浏览接口 |
| `sky-server/.../controller/user/SetmealController.java` | C端套餐浏览接口 |

## 修改文件清单（共 20 个）

| 文件 | 变更 |
|------|------|
| `MessageConstant.java` | 重构成 8 个领域 nested class |
| `EmployeeLoginDTO.java` | 新增 `@NotBlank` / `@Size` 注解 |
| `EmployeeDTO.java` | 新增 `@NotBlank` / `@Pattern` 注解 |
| `CategoryDTO.java` | 新增 `@Size` 注解 |
| `CategoryPageQueryDTO.java` | 新增 `@Min` 注解 |
| `EmployeePageQueryDTO.java` | 新增 `@NotNull` / `@Min` 注解 |
| `DishDTO.java` | 新增 `@NotBlank` / `@NotNull` / `@Positive` / `@Size` 注解 |
| `DishPageQueryDTO.java` | 新增 `@Min` 注解 |
| `SetmealDTO.java` | 新增 `@NotBlank` / `@NotNull` / `@Positive` / `@Size` 注解 |
| `SetmealPageQueryDTO.java` | 新增 `@Min` 注解 |
| `UserLoginDTO.java` | 新增 `@NotBlank` 注解 |
| `UserController.kt` | 添加 `@Valid`；删除手动格式校验（由注解接管） |
| `EmployeeController.java` | 添加 `@Validated` / `@Valid`；新增手动格式校验（status/id） |
| `CategoryController.java` | 添加 `@Valid`；新增手动格式校验（name/type/sort/id/status） |
| `DishController.kt` | 添加 `@Validated` / `@Valid`；新增手动格式校验（id/status） |
| `SetmealController.kt` | 添加 `@Validated` / `@Valid`；新增手动格式校验（id/status/setmealDishes） |
| `GlobalExceptionHandler.java` | 新增 `ConstraintViolationException` 处理器 |
| `EmployeeServiceImpl.java` | 删除 ~70 行格式校验代码 |
| `CategoryServiceImpl.java` | 删除 ~35 行格式校验代码 |
| `DishServiceImpl.kt` | 删除 ~30 行格式校验代码 |
| `SetmealServiceImpl.kt` | 删除 ~40 行格式校验代码 |

## 技术要点

- **Spring Boot 3.x + Jakarta Bean Validation**：DTO 注解 + `@Valid` + `@Validated` 三层配合实现参数校验
- **校验前置原则**：格式校验（null/长度/范围/格式）在 Controller 完成，业务校验（去重/存在性/权限）留在 Service，职责分明
- **`ConstraintViolationException`**：`@Validated` + `@PathVariable`/`@RequestParam` 校验失败时抛出，需单独在 `GlobalExceptionHandler` 处理
- **常量组织策略**：按领域分组（Employee / Category / Dish / Setmeal / Order / Login）的 nested class，替代平铺散落的顶层常量，提升可维护性
- **DTO 注解 vs 手动校验**：save/update 共用 DTO 时部分字段要求不同（如 `id`），无法统一用注解时在 Controller 中手动 if-check 是合理的折中
