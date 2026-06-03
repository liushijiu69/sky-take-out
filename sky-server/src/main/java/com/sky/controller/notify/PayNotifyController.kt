package com.sky.controller.notify

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.sky.properties.WeChatProperties
import com.sky.service.OrderService
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.http.entity.ContentType
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/notify")
class PayNotifyController(
    private val orderService: OrderService,
    private val weChatProperties: WeChatProperties,
) {
    private val log = LoggerFactory.getLogger(PayNotifyController::class.java)

    @PostMapping("/paySuccess")
    fun paySuccessNotify(request: HttpServletRequest, response: HttpServletResponse) {
        val body = readData(request)
        log.info("支付成功回调：{}", body)

        val plainText = decryptData(body)
        log.info("解密后的文本：{}", plainText)

        val jsonObject = JSON.parseObject(plainText)
        val outTradeNo = jsonObject.getString("out_trade_no")
        val transactionId = jsonObject.getString("transaction_id")

        log.info("商户平台订单号：{}", outTradeNo)
        log.info("微信支付交易号：{}", transactionId)

        orderService.paySuccess(outTradeNo)

        responseToWeixin(response)
    }

    private fun readData(request: HttpServletRequest): String {
        val reader = request.reader
        val result = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (result.isNotEmpty()) {
                result.append("\n")
            }
            result.append(line)
        }
        return result.toString()
    }

    private fun decryptData(body: String): String {
        val resultObject = JSON.parseObject(body)
        val resource = resultObject.getJSONObject("resource")
        val ciphertext = resource.getString("ciphertext")
        val nonce = resource.getString("nonce")
        val associatedData = resource.getString("associated_data")

        val aesUtil = AesUtil(weChatProperties.apiV3Key.toByteArray(StandardCharsets.UTF_8))
        return aesUtil.decryptToString(
            associatedData.toByteArray(StandardCharsets.UTF_8),
            nonce.toByteArray(StandardCharsets.UTF_8),
            ciphertext,
        )
    }

    private fun responseToWeixin(response: HttpServletResponse) {
        response.status = 200
        val map = HashMap<String, String>()
        map["code"] = "SUCCESS"
        map["message"] = "SUCCESS"
        response.setHeader("Content-type", ContentType.APPLICATION_JSON.toString())
        response.outputStream.write(JSON.toJSONString(map).toByteArray(StandardCharsets.UTF_8))
        response.flushBuffer()
    }
}
