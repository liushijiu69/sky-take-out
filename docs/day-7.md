# Day 7：缓存

## 1. 菜品缓存

使用 Redisson 简化代码。

## 2. 学习 Spring Cache

```java
@CachePut(cacheNames = "userCache", key = "abc")           // 生成的 key = userCache::abc
@CachePut(cacheNames = "userCache", key = "#user.id")      // 动态计算 key，如 userCache::1
@CachePut(cacheNames = "userCache", key = "#result.id")    // 从方法返回值获取 id
@CachePut(cacheNames = "userCache", key = "#p0")           // 从第一个传入参数获取
@CachePut(cacheNames = "userCache", key = "#a1")           // 从第二个传入参数获取
@CachePut(cacheNames = "userCache", key = "#root.args[0].id") // 第一个传入参数的 id
@Cacheable(cacheNames = "userCache", key = "#id")          // 缓存中查到则直接返回，没查到再执行方法
@CacheEvict(cacheNames = "userCache", key = "#id")         // 删除指定缓存
@CacheEvict(cacheNames = "userCache", allEntries = true)   // 删除 userCache 的所有缓存
```

## 3. 利用 AOP 实现日志记录

### 动机

35 个 Controller 方法都有手动 `log.info(...)` 调用，冗余且不统一（无耗时、无返回值、异常时无日志）。希望用 AOP 统一接管。

### 实现

**AutoLog 注解**（`sky-common/.../annotation/AutoLog.java`）

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoLog {
    String msg() default "";
}
```

**AutoLogAspect**（`sky-server/.../aspect/AutoLogAspect.kt`）

```kotlin
@Around("execution(* com.sky.controller..*.*(..)) && @annotation(com.sky.annotation.AutoLog)")
fun logAround(joinPoint: ProceedingJoinPoint): Any? {
    // 1. 前日志：msg + 方法签名 + 参数
    // 2. 执行目标方法（含 try-catch）
    // 3. 正常 → 后日志：msg + 耗时 + 返回值
    // 4. 异常 → 异常日志：msg + 耗时 + 异常信息，并 re-throw
}
```

**Controller 改造**：给 35 个方法加 `@AutoLog(msg = "中文描述")`，删除所有手动 `log.info(...)`，清理不再需要的 Logger 字段和 `@Slf4j`。

### 涉及文件（共 12 个）

| 文件                                | 变更                                                             |
| --------------------------------- | -------------------------------------------------------------- |
| `AutoLogAspect.kt`                | 新增异常处理（try-catch re-throw），清理无用 import                         |
| `EmployeeController.java`         | 7 方法加 `@AutoLog`，删除 `@Slf4j`                                   |
| `CategoryController.java` (admin) | 6 方法加 `@AutoLog`，删除 `@Slf4j`                                   |
| `DishController.kt` (admin)       | 7 方法加 `@AutoLog`，删除 LoggerFactory                              |
| `SetmealController.kt` (admin)    | 6 方法加 `@AutoLog`，删除 LoggerFactory                              |
| `ShopController.kt` (admin)       | 2 方法加 `@AutoLog`，删除 LoggerFactory                              |
| `CommonController.kt`             | 1 方法加 `@AutoLog`，删除 LoggerFactory                              |
| `UserController.kt`               | 1 方法加 `@AutoLog`，删除 LoggerFactory + 多余日志行                      |
| `CategoryController.java` (user)  | 1 方法加 `@AutoLog`，删除 `@Slf4j`                                   |
| `DishController.java` (user)      | 1 方法加 `@AutoLog`，删除 `@Slf4j`，修复 `listWithFlavor` 参数为 `DishDTO` |
| `SetmealController.java` (user)   | 2 方法加 `@AutoLog`，删除 `@Slf4j`                                   |
| `ShopController.kt` (user)        | 1 方法加 `@AutoLog`，删除 LoggerFactory                              |

### 日志效果

正常：

```
C端获取店铺营业状态 -> 执行开始,参数:[]
C端获取店铺营业状态 -> 执行结束,耗时:2ms,返回:Result(code=1, msg=success, data=0)
```

异常：

```
C端-根据分类id查询菜品 -> 执行开始,参数:[16]
C端-根据分类id查询菜品 -> 执行异常,耗时:41ms,异常:com.fasterxml.jackson...LocalDateTime...
```

相比手动日志，AOP 日志多了耗时、返回值/异常信息，且免去重复的 `log.info(...)` 代码。

### 后续：Redisson 序列化修复

运行时发现 `listWithFlavor` 方法在 `bucket.set(dishVOList)` 写入 Redis 时报错：`JsonJacksonCodec` 默认不支持 `LocalDateTime`。修复方式：

- 依赖 `jackson-datatype-jsr310` 已通过 Spring Boot 传递引入（无需新增）
- 在 `RedisAndRedissonConfiguration.kt` 中将 `JsonJacksonCodec()` 改为 `JsonJacksonCodec(createObjectMapper())` 并注册 `JavaTimeModule`：

```kotlin
private fun createObjectMapper(): ObjectMapper {
    return ObjectMapper().registerModule(JavaTimeModule())
}
```

## 4. 套餐缓存

利用 Spring Cache 完成套餐的缓存功能，新增自定义 `CustomRedisCacheManager` 继承 `RedisCacheManager` 来实现自定义过期时间。

## 5. 购物车功能模块

### 接口

| 方法       | 路径                         | 说明           |
| -------- | -------------------------- | ------------ |
| `POST`   | `/user/shoppingCart/add`   | 添加购物车（菜品或套餐） |
| `GET`    | `/user/shoppingCart/list`  | 查看购物车        |
| `DELETE` | `/user/shoppingCart/clean` | 清空购物车        |
| `POST`   | `/user/shoppingCart/sub`   | 删除购物车中指定商品   |

### Controller：`ShoppingCartController.kt`

4 个接口，均使用 `@AutoLog` 记录日志。`add` 和 `sub` 方法校验 `dishId` 与 `setmealId` 二选一（`Param.ILLEGAL`）。

### Service：`ShoppingCartServiceImpl.kt`

| 方法                   | 缓存注解                                                               | 说明                |
| -------------------- | ------------------------------------------------------------------ | ----------------- |
| `addShoppingCart`    | `@CacheEvict(allEntries = true)`                                   | 商品存在则数量 +1，否则插入新记录 |
| `list`               | `@Cacheable(cacheNames = "shoppingCartCache:30", key = "#userId")` | TTL 30 秒          |
| `cleanShoppingCart`  | `@CacheEvict(allEntries = true)`                                   | 按当前用户删除           |
| `deleteShoppingCart` | `@CacheEvict(allEntries = true)`                                   | 按条件删除             |

### Mapper + XML：`ShoppingCartMapper.java` / `ShoppingCartMapper.xml`

- `selectByShoppingCart`：动态条件查询（userId/dishId/setmealId/dishFlavor）
- `updateNumberById`：更新数量
- `insert`：插入购物车记录
- `delete`：动态条件删除

### DTO / VO / Entity

- `ShoppingCartDTO`：dishId + setmealId + dishFlavor（二选一校验）
- `ShoppingCartVO`：VO 增加 amount/image/createTime 等展示字段
- `ShoppingCart`（Entity）：与 VO 结构一致

### 缓存使用

- `list` 使用 `@Cacheable(cacheNames = "shoppingCartCache:30")` 缓存 30 秒（利用 `CustomRedisCacheManager` 的 TTL 解析功能）
- 增/删/改操作通过 `@CacheEvict(allEntries = true)` 清除该缓存区的所有 key

### 涉及文件

| 文件                           | 说明                |
| ---------------------------- | ----------------- |
| `ShoppingCartController.kt`  | C 端购物车 Controller |
| `ShoppingCartService.kt`     | 接口定义              |
| `ShoppingCartServiceImpl.kt` | 实现（含缓存注解）         |
| `ShoppingCartMapper.java`    | Mapper 接口         |
| `ShoppingCartMapper.xml`     | 动态 SQL            |
| `ShoppingCartDTO.java`       | 请求参数              |
| `ShoppingCartVO.java`        | 返回参数              |
| `ShoppingCart.java`          | 实体                |
