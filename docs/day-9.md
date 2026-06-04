# Day 9 — 订单模块：完整接口开发

基于 day-8 的框架，补齐全部订单接口。

## 1. 订单号生成器修复

`OrderNumberGenerator` 的 `IdUtil.fastUUID()` 返回 36 位 UUID（含横线），加时间戳共 52 位 > `orders.number varchar(50)`。改为 `IdUtil.fastSimpleUUID()`（32 位无横线），共 48 位。

## 2. 购物车批量插入

`ShoppingCartMapper.java` + XML：新增 `insertBatch`（`<foreach>`），供再来一单功能使用。

## 3. 客户端订单接口

| 接口 | 方法 | 关键逻辑 |
|------|------|---------|
| `GET /user/order/orderDetail/{id}` | 查订单 + 查明细 + 拼 orderDishes → OrderVO | day-8 已实现框架，当天补齐 |
| `GET /user/order/historyOrders` | PageHelper + pageQueryByUserId → PageResult | day-8 已实现框架，当天补齐 |
| `PUT /user/order/cancel/{id}` | 校验归属 + 仅待付款/待接单可取消 → 已取消 + cancelTime | 新 |
| `POST /user/order/repetition/{id}` | 查明细 → 清空购物车 → 明细转购物车 → insertBatch | 新 |

## 4. 管理端订单接口

### Controller

新建 `OrderController.kt`（`com.sky.controller.admin`），`@RestController("adminOrderController")`，`@RequestMapping("/admin/order")`。

### 接口一览

| 接口 | 方法 | 关键逻辑 |
|------|------|---------|
| `GET /conditionSearch` | PageHelper + pageQueryByCondition → PageResult | 支持 number(模糊)/phone/status/beginTime/endTime |
| `GET /statistics` | 三次 countByStatus → OrderStatisticsVO | 待接单/待派送/派送中 |
| `GET /details/{id}` | 复用 `orderService.orderDetail(id)` | |
| `PUT /confirm` | 待接单(2) → 已接单(3) | |
| `PUT /rejection` | 待接单(2) → 已取消(6) + rejectionReason + cancelTime | |
| `PUT /cancel` | 非已完成/已取消均可取消 + cancelReason（必填） | 区别于用户端：不限归属、不限支付状态 |
| `PUT /delivery/{id}` | 已接单(3) → 派送中(4) | |
| `PUT /complete/{id}` | 派送中(4) → 已完成(5) + deliveryTime=now | |

### 新增 Mapper

| Mapper 方法 | 说明 |
|------------|------|
| `OrderMapper.pageQueryByCondition(OrdersPageQueryDTO)` | 动态多条件分页查询 |
| `OrderMapper.countByStatus(Integer)` | 按状态统计订单数 |

### 对应 XML

- `<select id="pageQueryByCondition">`：number concat 模糊 + phone 精确 + status 精确 + beginTime/endTime 时间范围
- `<update id="update">`：动态 set，支持 status/rejectionReason/cancelReason/cancelTime/payStatus/checkoutTime/deliveryTime

## 5. 状态流转图

```
待付款(1) ──→ 待接单(2) ──→ 已接单(3) ──→ 派送中(4) ──→ 已完成(5)
   │              │
   └── 用户取消 ──┴── admin拒单/admin取消 ──→ 已取消(6)
```

## 文件变更汇总（仅 day-9 新增/改动的部分）

| 模块 | 文件 | 变更 |
|------|------|------|
| 工具 | `OrderNumberGenerator.kt` | `UUID.fastUUID()` → `IdUtil.fastSimpleUUID()` |
| Mapper | `ShoppingCartMapper.java` + XML | 新增 `insertBatch` |
| Mapper | `OrderMapper.java` + XML | 新增 `pageQueryByCondition`、`countByStatus`、`update`、`selectById` |
| Service | `OrderService.kt` | 新增 cancelOrder/repetition/conditionSearch/statistics/confirm/rejection/adminCancelOrder/delivery/complete |
| Service | `OrderServiceImpl.kt` | 全部对应实现 |
| Controller (user) | `OrderController.kt` | 新增 orderDetail/historyOrders/payment/cancelOrder/repetition |
| Controller (admin) | `OrderController.kt` | **新建**：conditionSearch/statistics/details/confirm/rejection/cancel/delivery/complete |
| Controller (notify) | `PayNotifyController.kt` | **新建**：支付回调 |
