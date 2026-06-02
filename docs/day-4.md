# Day 4: 菜品状态 + 套餐模块 (CRUD)

## 完成的功能点

| # | 接口/功能 | 核心内容 |
|---|---|---|
| 1 | `POST /admin/dish/status/{status}` | 菜品起售停售，复用 `dishMapper.update` 动态 SQL |
| 2 | `SetmealConstant.kt` | 套餐常量类，含 `SetmealStatus` 枚举 + `contains()` |
| 3 | `POST /admin/setmeal` | 新增套餐：校验 → 插入 setmeal（useGeneratedKeys 回显 id）→ 批量插入 setmeal_dish |
| 4 | `GET /admin/dish/list` | 根据分类 id 查询菜品列表，修复 `list` 被 `{id}` 吞掉的 bug |
| 5 | `GET /admin/setmeal/page` | 套餐分页查询，`Setmeal.categoryName` 映射字段 + left join 一次查出 |
| 6 | `DELETE /admin/setmeal` | 批量删除套餐：校验起售 → 删 setmeal_dish → 删 setmeal |
| 7 | `GET /admin/setmeal/{id}` | 根据 id 查询套餐（含 categoryName + setmealDishes） |
| 8 | `PUT /admin/setmeal` | 修改套餐：校验 → 全量替换 setmeal_dish → 更新 setmeal |
| 9 | `POST /admin/setmeal/status/{status}` | 套餐起售停售，复用 `setmealMapper.update` 动态 SQL |

## 新建文件（8 个）

| 文件 | 说明 |
|---|---|
| `sky-common/.../constant/SetmealConstant.kt` | 套餐常量 + 状态枚举 |
| `sky-server/.../controller/admin/SetmealController.kt` | 套餐管理 Controller |
| `sky-server/.../service/SetmealService.kt` | 套餐 Service 接口 |
| `sky-server/.../service/impl/SetmealServiceImpl.kt` | 套餐 Service 实现 |
| `sky-server/.../mapper/SetmealDishMapper.java` | 套餐菜品关系 Mapper |
| `sky-server/.../mapper/SetmealDishMapper.xml` | 套餐菜品关系 SQL |
| `sky-server/.../mapper/SetmealMapper.xml` | 套餐表所有 SQL |
| `.opencode/rules/sky-take-out.agent.md` | 项目约束规则（持续更新） |

## 修改文件（6 个）

| 文件 | 变更 |
|---|---|
| `DishConstant.kt` | 新增 `STATUS` 常量 + `DishStatus.contains()` |
| `DishController.kt` | 新增 `startOrStop`、`list` 端点；修复 import 重复 |
| `DishService.kt` | 新增 `startOrStop`、`listByCategoryId` 接口 |
| `DishServiceImpl.kt` | 实现 `startOrStop`、`listByCategoryId` |
| `DishMapper.java` | 新增 `selectByCategoryId`、`countByIds` |
| `DishMapper.xml` | 新增 `selectByCategoryId`、`countByIds` SQL |
| `Setmeal.java` (entity) | 新增 `categoryName` 映射字段 |
| `SetmealMapper.java` | 新增 `insert`、`selectByPage`、`selectById`、`countByIdsAndStatus`、`deleteByIds`、`update` |
| `SetmealDishMapper.java` | 新增 `deleteBySetmealIds`、`selectBySetmealId` |
| `SetmealDishMapper.xml` | 新增 `deleteBySetmealIds`、`selectBySetmealId` SQL |
| `SetmealServiceImpl.kt` | 实现所有 6 个 Service 方法；注入 `CategoryMapper` 做逻辑外键校验 |
| `SetmealController.kt` | 新增全部 6 个端点 |

## 已覆盖的套餐接口清单

| 端点 | 状态 |
|---|---|
| `POST /admin/setmeal` | ✅ |
| `DELETE /admin/setmeal?ids=...` | ✅ |
| `GET /admin/setmeal/page` | ✅ |
| `GET /admin/setmeal/{id}` | ✅ |
| `PUT /admin/setmeal` | ✅ |
| `POST /admin/setmeal/status/{status}` | ✅ |

## 关键设计决策

- **`categoryName` 用 Entity 映射字段 + join SQL**：避免 N+1 查询，分页列表和单查都一次 SQL 拿到
- **全量替换策略**：setmeal_dish 更新始终 `delete + insertBatch`，与 Dish flavor 模式一致
- **Mapper 只返回 Entity**：`selectById`/`selectByPage` 返回 `Setmeal`，Service 层转 `SetmealVO`
- **校验复用**：save 和 update 共享相同的校验逻辑（name 长度、price、categoryId 外键、setmealDishes 内部字段）
- **update 状态可选**：PUT 中 status 标记为非必填，校验 + SQL 都用 `<if>` 保护，不传则不更新
