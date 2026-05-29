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

## 命名规范

| 层级 | 规范 | 示例 |
|---|---|---|
| Mapper 方法 | `操作 + 条件` | `selectById`、`selectByPage`、`insert`、`insertBatch`、`update`、`deleteById` |
| Service 接口/实现 | 与 Controller 方法名一致 | Controller 的 `pageQuery` → Service 也是 `pageQuery` |
| Controller 端点 | 直观描述操作 | `save`、`pageQuery` |

## Controller 层规范

- 每个方法标记 `@Operation(summary = "中文描述")`
- 每个方法在调用 Service 前用 `log.info("操作描述,参数:${param}")` 记录日志
- Logger 定义：`private val log = LoggerFactory.getLogger(XxxController::class.java)`

## Service 层规范

### 数据校验

- 校验规则依据 `docs/数据库设计文档.md`（字段长度/约束）和 `docs/苍穹外卖-管理端接口.md`（必填/可选）
- 校验模式：
  1. 必填字段非空检查 → `throw IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL)`
  2. 单个字段格式/范围检查 → `throw IllegalException(常量名 + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK/NOT_IN_RANGE)`
- 字段提示常量定义在 `sky-common/.../constant/` 下，使用 Kotlin `object`
- 取值有限的变量（如 status 0/1、类型枚举等）在对应的 Constant 中用 `enum class(val code: Int, val desc: String)` 定义，不写魔法数字

### 多表写操作

- 涉及多表修改的方法必须加 `@Transactional`
- 批量删除流程：校验业务约束 → 删子表数据 → 删主表数据

### VO 组装

- Mapper 层只返回 Entity，不返回 VO
- Service 层通过 `BeanUtils.copyProperties(entity, vo)` + 手动设置额外字段来组装 VO

### 文档注释

- 方法写 `/** 方法说明 + 分步骤中文说明业务逻辑 */`
- 方法体内每步用 `// 步骤说明` 标注

## 分页查询

```kotlin
PageHelper.startPage(dto.page, dto.pageSize)
Page<VO> page = mapper.selectByPage(dto)
return PageResult(page.total, page.result)
```

## MyBatis SQL 写法

| 场景 | 做法 |
|---|---|
| 动态条件 | `<where>` + `<if test="...">` |
| 模糊查询 | `like concat('%', #{name}, '%')` |
| 批量操作 | `<foreach collection="..." item="..." open="(" separator="," close=")">` |
| resultType | 写全限定类名（如 `com.sky.entity.Dish`） |
| 查所有列 | **禁用 `select *`**，必须显式列出所有列名 |
| update | 用 `<set>` + `<if>` 实现动态更新 |
| insert 返回主键 | `useGeneratedKeys="true" keyProperty="id"` |

## Mapper 层规范

### 接口

- **返回值类型必须是 Entity，不是 VO**（VO 在 Service 组装）
- 新增/修改方法标注 `@AutoFill`
  - insert → `@AutoFill(AutoFill.OperationType.INSERT)`
  - update → `@AutoFill(AutoFill.OperationType.UPDATE)`
- `@Select` 注解仅用于极简查询，复杂 SQL 统一写在 XML

### XML

- 每个 SQL 块上方写 `<!-- 方法说明 -->`
- 不要有未使用的 `<if>`、`<set>` 等标签

## 枚举类定义

取值有限的变量在对应 Constant 中用 Kotlin `enum class` 定义：

```kotlin
enum class Status(val code: Int, val desc: String) {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");
    companion object {
        fun contains(v: Int): Boolean = entries.any { it.code == v }
    }
}
```

引用时用 `.code` 获取原始值，用 `.desc` 获取中文描述。Java 调用方用 `.getCode()` / `.getDesc()`。

## 文档注释规范

- **所有类、接口、方法**必须写文档注释（JavaDoc / KDoc）
- Controller 方法：`@Operation(summary = "中文描述")` + `/** 中文说明 */`
- Service 方法：`/** 方法说明 + @param + @return */`
- Service 实现：`/** 方法说明 + 分步骤中文说明业务逻辑 */`，方法体内每步用 `// 步骤说明` 标注
- Mapper 接口：`/** 方法说明 + @param + @return */`
- Mapper XML：SQL 块上方写 `<!-- 方法说明 -->`
- 常量类：每条常量上方写 `/** 说明 */`

## 项目技术栈

- 语言：Java + Kotlin 混用
- ORM：MyBatis + PageHelper
- 构建：Maven 多模块
- Entity/DTO：Lombok（`@Data`）
- VO：Lombok（`@Data` `@Builder` `@NoArgsConstructor` `@AllArgsConstructor`）
- 日志：SLF4J + Logback
- 接口文档：SpringDoc OpenAPI
