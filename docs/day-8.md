# Day 8

## 1. 代码规范更新

### 1.1 Java/Kotlin 策略

已有 Java 代码不做 Kotlin 转换，仅手写新代码时用 Kotlin。（写入 `.opencode/rules/sky-take-out.agent.md`）

### 1.2 校验策略

Controller 参数校验改为**手动 if-check + `IllegalException`**，不再依赖 `@Valid`/`@Validated`。原因：同一 DTO 在不同接口的必填/约束可能不同，注解不够灵活。

### 1.3 日志

使用 `@AutoLog` AOP 统一记录日志，Controller 中不保留 `log.info(...)` 和 Logger 字段。

## 2. 导入地址簿模块（5 文件）

从 `docs/地址簿模块功能代码` 导入，水土不服修复：

| 源 | 问题 | 修复 |
|----|------|------|
| `AddressBookController.java` | `@Api`/`@ApiOperation`（Swagger2）、`@Autowired` 字段注入 | → Kotlin，`@Tag`/`@Operation`（Swagger3）、构造器注入；7 个方法加 `@AutoLog` |
| `AddressBookService.java` | Java 接口 | → Kotlin 接口 |
| `AddressBookServiceImpl.java` | `@Autowired`、`@Slf4j` | → Kotlin，构造器注入，去掉 `@Slf4j` |
| `AddressBookMapper.java` | 正常 | 保持 Java |
| `AddressBookMapper.xml` | `parameterType="addressBook"` 大小写不一致；`update` 缺少 province_code/name、city_code/name、district_code/name 共 6 个字段；`select *` | 修正 `parameterType`，补全 6 个字段，显式列名 |

## 3. 用户端接口校验补充

依据 `docs/苍穹外卖-用户端接口.md` 和 `docs/数据库设计文档.md`，为缺少校验的接口补充手动校验：

| 接口 | 校验内容 |
|------|---------|
| `POST /user/addressBook` | detail/phone/sex 非空；phone 11位数字 |
| `PUT /user/addressBook` | id/detail/phone/sex 非空；phone 11位数字 |
| `PUT /user/addressBook/default` | id 非空 |
| `GET /user/dish/list` | categoryId 非空 |
| `GET /user/setmeal/list` | categoryId 非空 |
| `GET /user/setmeal/dish/{id}` | id > 0 |

## 4. 订单模块框架搭建（8 文件）

| 文件 | 说明 |
|------|------|
| `OrderNumberGenerator.kt` | ID 生成器，格式 `yyyyMMdd-HHmmss_` + 32 位无横线 UUID（`IdUtil.fastSimpleUUID()`） |
| `OrderMapper.java` | `insert` + `selectById` + `getByNumber` + `update` + `pageQueryByUserId` |
| `OrderDetailMapper.java` | `insertBatch` + `selectByOrderId` |
| `OrderMapper.xml` | INSERT 全部 24 字段；动态 UPDATE；按 userId 分页查询 |
| `OrderDetailMapper.xml` | 批量 INSERT（foreach）；按 orderId 查询 |
| `OrderService.kt` | 接口：submitOrder、orderDetail、historyOrders、payment、paySuccess |
| `OrderServiceImpl.kt` | 实现：下单（查地址→查购物车→构造订单→INSERT→构造明细→INSERT→清空购物车→返回）；查详情；历史分页；支付调用；支付成功回调 |
| `OrderController.kt` | `POST /submit`、`GET /orderDetail/{id}`、`GET /historyOrders`、`PUT /payment` |

### 4.1 用户下单流程

```
POST /user/order/submit
  ├─ 手工校验 DTO 字段（addressBookId、amount、payMethod、deliveryStatus 等）
  ├─ 查地址 → 查购物车 → 构造 Orders（状态=待付款）→ INSERT
  ├─ 购物车条目 → OrderDetail → INSERT batch
  ├─ 清空购物车
  └─ 返回 OrderSubmitVO
```

### 4.2 订单详情

```
GET /user/order/orderDetail/{id}
  ├─ orderMapper.selectById → OrderVO
  ├─ orderDetailMapper.selectByOrderId → orderDetailList
  ├─ 拼接 orderDishes 展示字符串（"草鱼2斤×1, 王老吉"）
  └─ 返回 OrderVO
```

### 4.3 历史订单

```
GET /user/order/historyOrders?page=&pageSize=&status=
  ├─ PageHelper.startPage
  ├─ orderMapper.pageQueryByUserId (按 userId + 可选 status)
  ├─ 每个订单查明细、拼 orderDishes → List<OrderVO>
  └─ 返回 PageResult
```

## 5. 导入微信支付相关代码（6 文件）

从 `docs/微信支付功能代码` 导入，水土不服修复：

| 源 | 问题 | 修复 |
|----|------|------|
| `OrderController.java` | `@Api`、`@Autowired` | 仅提取 `payment` 端点追加到已有 Kotlin Controller |
| `OrderMapper.java` | `getByNumber` 用 `select *` | 改为显式列名 |
| `OrderMapper.xml` | 补 `<update>` 动态更新（cancelReason、payStatus、checkoutTime、status 等） |
| `OrderService.java` | Java 接口 | 追加 payment、paySuccess 到已有 Kotlin 接口 |
| `OrderServiceImpl.java` | `Orders.TO_BE_CONFIRMED` 常量不存在 | 改为 `OrderConstant.Status.WAITING_FOR_ORDERS.code`；不替换已有 submitOrder |
| `PayNotifyController.java` | Java，`javax.servlet`，`druid JSONUtils`，`@Autowired` | → Kotlin；`jakarta.servlet`；`fastjson.JSON`；构造器注入 |

### 5.1 支付流程

```
PUT /user/order/payment
  ├─ WeChatPayUtil.pay() → 微信统一下单
  ├─ 返回 OrderPaymentVO（nonceStr、paySign、timeStamp、signType、packageStr）
  └─ 前端调起微信支付

POST /notify/paySuccess  (微信异步回调)
  ├─ 读取请求体 → 解密 → 解析 out_trade_no
  ├─ orderService.paySuccess(outTradeNo)
  │   └─ 更新订单状态: 待付款 → 待接单，支付状态: 未支付 → 已支付
  └─ 响应 SUCCESS 给微信
```

### 5.2 遗留问题

- 删除购物车商品 `POST /user/shoppingCart/sub` 的业务逻辑（减1 -> <=0时删除）**已修正**

