package com.sky.service.impl

import com.sky.constant.MessageConstant
import com.sky.constant.OrderConstant
import com.sky.context.BaseContext
import com.sky.dto.OrdersPaymentDTO
import com.sky.dto.OrdersSubmitDTO
import com.sky.entity.OrderDetail
import com.sky.entity.Orders
import com.sky.entity.ShoppingCart
import com.sky.entity.User
import com.github.pagehelper.Page
import com.github.pagehelper.PageHelper
import com.sky.exception.AddressBookBusinessException
import com.sky.exception.IllegalException
import com.sky.exception.OrderBusinessException
import com.sky.exception.ShoppingCartBusinessException
import com.sky.mapper.AddressBookMapper
import com.sky.mapper.OrderDetailMapper
import com.sky.mapper.OrderMapper
import com.sky.mapper.ShoppingCartMapper
import com.sky.mapper.UserMapper
import com.sky.result.PageResult
import com.sky.service.OrderService
import com.sky.utils.IDGenerator
import com.sky.utils.WeChatPayUtil
import com.sky.vo.OrderPaymentVO
import com.sky.vo.OrderSubmitVO
import com.sky.vo.OrderVO
import org.springframework.beans.BeanUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
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
    @CacheEvict(cacheNames = ["orderCache"], key ="#result.id" )
    @Transactional
    override fun submitOrder(ordersSubmitDTO: OrdersSubmitDTO): OrderSubmitVO {
        //处理业务异常
        val addressBook = (addressBookMapper.getById(ordersSubmitDTO.addressBookId)// 查询地址
            ?: throw AddressBookBusinessException(MessageConstant.Order.ADDRESS_EMPTY))// 地址为空, 抛出异常
        val userId = BaseContext.getCurrentId()
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

    override fun historyOrders(page: Int, pageSize: Int, status: Int?): PageResult {
        val userId = BaseContext.getCurrentId()
        PageHelper.startPage<Orders>(page, pageSize)
        val ordersPage = orderMapper.pageQueryByUserId(userId, status) as Page<Orders>
        val orderVOList = (ordersPage.result as List<Orders>).map { orders ->
            val detailList = orderDetailMapper.selectByOrderId(orders.id)
            val orderDishes = detailList.joinToString(", ") { detail ->
                if (detail.number > 1) "${detail.name}×${detail.number}" else detail.name
            }
            OrderVO().apply {
                BeanUtils.copyProperties(orders, this)
                orderDetailList = detailList
                this.orderDishes = orderDishes
            }
        }
        return PageResult(ordersPage.total, orderVOList)
    }

    override fun payment(ordersPaymentDTO: OrdersPaymentDTO): OrderPaymentVO {
        val userId = BaseContext.getCurrentId()
        val user = userMapper.selectById(userId)
            ?: throw OrderBusinessException(MessageConstant.Login.USER_NOT_FOUND)
        val jsonObject = weChatPayUtil.pay(
            ordersPaymentDTO.orderNumber,
            BigDecimal(0.01),
            "苍穹外卖订单",
            user.openid,
        )
        if (jsonObject.getString("code") != null
            && jsonObject.getString("code") == "ORDERPAID"
        ) {
            throw OrderBusinessException("该订单已支付")
        }
        val vo = jsonObject.toJavaObject(OrderPaymentVO::class.java)
        vo.packageStr = jsonObject.getString("package")
        return vo
    }

    override fun paySuccess(outTradeNo: String) {
        val ordersDB = orderMapper.getByNumber(outTradeNo)
            ?: throw OrderBusinessException(MessageConstant.Order.NOT_FOUND)
        val orders = Orders.builder()
            .id(ordersDB.id)
            .status(OrderConstant.Status.WAITING_FOR_ORDERS.code)
            .payStatus(OrderConstant.PayStatus.PAID.code)
            .checkoutTime(LocalDateTime.now())
            .build()
        orderMapper.update(orders)
    }
}
