package com.sky.service.impl

import com.github.pagehelper.Page
import com.github.pagehelper.PageHelper
import com.sky.constant.MessageConstant
import com.sky.constant.OrderConstant
import com.sky.context.BaseContext
import com.sky.dto.OrdersCancelDTO
import com.sky.dto.OrdersConfirmDTO
import com.sky.dto.OrdersPageQueryDTO
import com.sky.dto.OrdersPaymentDTO
import com.sky.dto.OrdersRejectionDTO
import com.sky.dto.OrdersSubmitDTO
import com.sky.entity.OrderDetail
import com.sky.entity.Orders
import com.sky.entity.ShoppingCart
import com.sky.entity.User
import com.sky.exception.AddressBookBusinessException
import com.sky.exception.IllegalException
import com.sky.exception.OrderBusinessException
import com.sky.exception.ShoppingCartBusinessException
import com.sky.mapper.*
import com.sky.result.PageResult
import com.sky.service.OrderService
import com.sky.utils.IDGenerator
import com.sky.utils.WeChatPayUtil
import com.sky.vo.OrderPaymentVO
import com.sky.vo.OrderStatisticsVO
import com.sky.vo.OrderSubmitVO
import com.sky.vo.OrderVO
import org.springframework.beans.BeanUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderServiceImpl(
    private val orderMapper: OrderMapper,
    private val orderDetailMapper: OrderDetailMapper,
    private val addressBookMapper: AddressBookMapper,
    private val shoppingCartMapper: ShoppingCartMapper,
    private val userMapper: UserMapper,
    private val weChatPayUtil: WeChatPayUtil,
) : OrderService {
    @Transactional
    override fun submitOrder(ordersSubmitDTO: OrdersSubmitDTO, userId: Long): OrderSubmitVO {
        //处理业务异常
        val addressBook = (addressBookMapper.getById(ordersSubmitDTO.addressBookId)// 查询地址
            ?: throw AddressBookBusinessException(MessageConstant.Order.ADDRESS_EMPTY))// 地址为空, 抛出异常

        val cartList = shoppingCartMapper.selectByShoppingCart(//获取当前用户的购物车数据
            ShoppingCart().apply {// 构造查询条件
                this.userId = userId
            }
        )// 判断购物车数据是否为空
            ?.let { if (it.isNotEmpty()) it else null }// 为空则抛出异常
            ?: throw ShoppingCartBusinessException(MessageConstant.Order.CART_EMPTY)
        // 获取当前用户
        val user: User =  userMapper.selectById(userId)
            ?: throw IllegalException(MessageConstant.Login.USER_NOT_FOUND)
        //校验地理距离是否在五公里以内
        //没有公网ip,无法申请到百度服务
        // 1. 向订单表插入1条数据
        val orders = Orders().apply {
            BeanUtils.copyProperties(ordersSubmitDTO, this)
            orderTime = LocalDateTime.now()// 下单时间
            payStatus = OrderConstant.PayStatus.UN_PAID.code// 未支付
            status = OrderConstant.Status.PENDING_PAYMENT.code// 待支付
            number = IDGenerator.generate()// 订单号
            phone = addressBook.phone// 手机号
            consignee = addressBook.consignee// 收货人
            this.userId = userId
            address = "${addressBook.provinceName} ${addressBook.cityName} ${addressBook.districtName} ${addressBook.detail}"
            userName = user.name
        }
        orderMapper.insert(orders)
        //  2. 向订单明细表插入n条数据
        val detailList = cartList.map {
            OrderDetail().apply {
                BeanUtils.copyProperties(it, this)
                orderId = orders.id
            }
        }
        orderDetailMapper.insertBatch(detailList)
        // 3. 清空当前用户的购物车数据
        shoppingCartMapper.delete(ShoppingCart().apply {this.userId = userId})
        // 4. 构建OrderSubmitVO对象返回
        return OrderSubmitVO().apply {
            this.id = orders.id
            this.orderTime = orders.orderTime
            this.orderNumber = orders.number
            this.orderAmount = orders.amount
        }
    }

    override fun orderDetail(id: Long): OrderVO {
        // 1. 根据id查询订单数据
        val orders = orderMapper.selectById(id)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        // 2. 根据订单id查询订单明细数据
        val detailList = orderDetailMapper.selectByOrderId(id)
        // 3. 获取订单菜品列表
        val orderDishes = detailList.joinToString(", ") { detail ->
            if (detail.number > 1) "${detail.name}×${detail.number}" else detail.name
        }
        // 4. 封装VO对象并返回
        return OrderVO().apply {
            BeanUtils.copyProperties(orders, this)
            orderDetailList = detailList
            this.orderDishes = orderDishes
        }
    }

    override fun historyOrders(page: Int, pageSize: Int, status: Int?, userId: Long): PageResult {
        PageHelper.startPage<Orders>(page, pageSize)// 分页
        val ordersPage = orderMapper.pageQueryByUserId(userId, status) as Page<Orders>// 查询分页数据
        // 将订单数据转换为VO对象
        val orderVOList = (ordersPage.result as List<Orders>).map { orders ->
            val detailList = orderDetailMapper.selectByOrderId(orders.id)// 获取订单菜品
            val orderDishes = detailList.joinToString(", ") { detail ->
                if (detail.number > 1) "${detail.name}×${detail.number}" else detail.name
            }
            OrderVO().apply {// 封装VO对象
                BeanUtils.copyProperties(orders, this)
                orderDetailList = detailList
                this.orderDishes = orderDishes
            }
        }
        return PageResult(ordersPage.total, orderVOList)
    }

    override fun payment(ordersPaymentDTO: OrdersPaymentDTO): OrderPaymentVO {
        // ====== 模拟支付：跳过微信支付，直接标记为已支付 ======
        paySuccess(ordersPaymentDTO.orderNumber)
        return OrderPaymentVO.builder()
            .nonceStr("mock_nonce_str")
            .paySign("mock_pay_sign")
            .timeStamp(System.currentTimeMillis().toString())
            .signType("MD5")
            .packageStr("prepay_id=mock_prepay_id")
            .build()
    }

    override fun paySuccess(orderNumber: String) {
        val ordersDB = orderMapper.getByNumber(orderNumber)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        val orders = Orders.builder()
            .id(ordersDB.id)
            .status(OrderConstant.Status.WAITING_FOR_ORDERS.code)
            .payStatus(OrderConstant.PayStatus.PAID.code)
            .checkoutTime(LocalDateTime.now())
            .build()
        orderMapper.update(orders)
    }

    override fun cancelOrder(id: Long) {
        // 1. 校验订单
        val orders = orderMapper.selectById(id)// 查询订单
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        if (orders.userId != BaseContext.getCurrentId()) {// 订单不属于当前用户
            throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        }
        if (orders.status !in setOf(// 订单状态错误
                OrderConstant.Status.PENDING_PAYMENT.code,
                OrderConstant.Status.WAITING_FOR_ORDERS.code,
            )
        ) {
            throw OrderBusinessException(MessageConstant.Order.STATUS_ERROR)
        }
        val updateOrder = Orders.builder()
            .id(orders.id)// 订单id
            .status(OrderConstant.Status.CANCELLED.code)// 订单状态
            .cancelReason("用户取消")// 取消原因
            .cancelTime(LocalDateTime.now())// 取消时间
            .build()// 构建订单对象
        orderMapper.update(updateOrder)// 更新订单数据
    }

    override fun repetition(id: Long) {
        // 1. 校验订单
        val orders = orderMapper.selectById(id)// 查询订单
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        if (orders.userId != BaseContext.getCurrentId()) {// 订单不属于当前用户
            throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        }
        // 2. 查询订单明细
        val detailList = orderDetailMapper.selectByOrderId(id)
            .takeIf { it.isNotEmpty() }// 订单明细不能为空
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)// 订单明细为空
        // 3. 清空当前购物车
        val userId = BaseContext.getCurrentId()// 获取当前用户
        shoppingCartMapper.delete(ShoppingCart().apply { this.userId = userId })// 清空当前用户购物车数据
        // 4. 将订单明细转为购物车条目
        val shoppingCartList = detailList.map { detail ->
            ShoppingCart().apply {// 购物车对象
                BeanUtils.copyProperties(detail, this)
                this.userId = userId
                createTime = LocalDateTime.now()
            }
        }
        // 5. 批量插入购物车
        shoppingCartMapper.insertBatch(shoppingCartList)
    }

    override fun conditionSearch(ordersPageQueryDTO: OrdersPageQueryDTO): PageResult {
        // 1. 分页
        PageHelper.startPage<Orders>(ordersPageQueryDTO.page, ordersPageQueryDTO.pageSize)
        // 2. 条件查询
        val ordersPage = orderMapper.pageQueryByCondition(ordersPageQueryDTO) as Page<Orders>
        // 3. 封装VO对象
        val orderVOList = (ordersPage.result as List<Orders>).map { orders ->
            val detailList = orderDetailMapper.selectByOrderId(orders.id)// 获取订单菜品
            val orderDishes = detailList.joinToString(", ") { detail ->
                if (detail.number > 1) "${detail.name}×${detail.number}" else detail.name
            }
            // 封装VO对象
            OrderVO().apply {
                BeanUtils.copyProperties(orders, this)
                orderDetailList = detailList
                this.orderDishes = orderDishes
            }
        }
        // 4. 封装结果并返回
        return PageResult(ordersPage.total, orderVOList)
    }

    override fun statistics(): OrderStatisticsVO {
        return OrderStatisticsVO().apply {
            toBeConfirmed = orderMapper.countByStatus(OrderConstant.Status.WAITING_FOR_ORDERS.code)
            confirmed = orderMapper.countByStatus(OrderConstant.Status.ACCEPTED.code)
            deliveryInProgress = orderMapper.countByStatus(OrderConstant.Status.DELIVERY_IN_PROGRESS.code)
        }
    }

    override fun confirm(id: Long) {
        // 1. 校验订单
        val orders = orderMapper.selectById(id)// 查询订单
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)// 订单不存在
        // 2. 校验订单状态
        if (orders.status != OrderConstant.Status.WAITING_FOR_ORDERS.code) {// 订单状态错误
            throw OrderBusinessException(MessageConstant.Order.STATUS_ERROR)
        }
        // 3. 更新订单状态
        orderMapper.update(Orders.builder()
            .id(id)
            .status(OrderConstant.Status.ACCEPTED.code)
            .build())
    }

    override fun rejection(ordersRejectionDTO: OrdersRejectionDTO) {
        // 1. 校验订单
        val orders = orderMapper.selectById(ordersRejectionDTO.id)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)// 订单不存在
        // 2. 校验订单状态
        if (orders.status != OrderConstant.Status.WAITING_FOR_ORDERS.code) {// 订单状态错误
            throw OrderBusinessException(MessageConstant.Order.STATUS_ERROR)
        }
        // 3. 更新订单状态
        orderMapper.update(Orders.builder()
            .id(ordersRejectionDTO.id)
            .status(OrderConstant.Status.CANCELLED.code)
            .rejectionReason(ordersRejectionDTO.rejectionReason)
            .cancelTime(LocalDateTime.now())
            .build())
    }

    override fun adminCancelOrder(ordersCancelDTO: OrdersCancelDTO) {
        // 1. 校验订单
        val orders = orderMapper.selectById(ordersCancelDTO.id)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)// 订单不存在
        if (orders.status == OrderConstant.Status.COMPLETED.code ||
            orders.status == OrderConstant.Status.CANCELLED.code
        ) {// 订单状态错误
            throw OrderBusinessException(MessageConstant.Order.STATUS_ERROR)
        }
        // 更新订单状态
        orderMapper.update(Orders.builder()
            .id(ordersCancelDTO.id)
            .status(OrderConstant.Status.CANCELLED.code)
            .cancelReason(ordersCancelDTO.cancelReason)
            .cancelTime(LocalDateTime.now())
            .build())
    }

    override fun delivery(id: Long) {
        // 1. 校验订单
        val orders = orderMapper.selectById(id)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)// 订单不存在
        // 2. 校验订单状态
        if (orders.status != OrderConstant.Status.ACCEPTED.code) {
            throw OrderBusinessException(MessageConstant.Order.STATUS_ERROR)// 订单状态错误
        }
        // 3. 更新订单状态
        orderMapper.update(Orders.builder()
            .id(id)
            .status(OrderConstant.Status.DELIVERY_IN_PROGRESS.code)
            .build())
    }

    override fun complete(id: Long) {
        // 1. 校验订单
        val orders = orderMapper.selectById(id)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)// 订单不存在
        if (orders.status != OrderConstant.Status.DELIVERY_IN_PROGRESS.code) {
            throw OrderBusinessException(MessageConstant.Order.STATUS_ERROR)// 订单状态错误
        }
        // 2. 获取订单菜品
        orderMapper.update(Orders.builder()
            .id(id)
            .status(OrderConstant.Status.COMPLETED.code)
            .deliveryTime(LocalDateTime.now())
            .build())
    }
}
