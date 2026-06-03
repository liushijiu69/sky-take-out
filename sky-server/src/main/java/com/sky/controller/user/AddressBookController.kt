package com.sky.controller.user

import com.sky.annotation.AutoLog
import com.sky.constant.MessageConstant
import com.sky.context.BaseContext
import com.sky.entity.AddressBook
import com.sky.exception.IllegalException
import com.sky.result.Result
import com.sky.service.AddressBookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "C端-地址簿接口")
@RestController("userAddressBookController")
@RequestMapping("/user/addressBook")
class AddressBookController(
    private val addressBookService: AddressBookService
) {
    @AutoLog(msg = "查询当前登录用户的所有地址信息")
    @Operation(summary = "查询当前登录用户的所有地址信息")
    @GetMapping("/list")
    fun list(): Result<List<AddressBook>> {
        val addressBook = AddressBook().apply { userId = BaseContext.getCurrentId() }
        val list = addressBookService.list(addressBook)
        return Result.success(list)
    }

    @AutoLog(msg = "新增地址")
    @Operation(summary = "新增地址")
    @PostMapping
    fun save(@RequestBody addressBook: AddressBook): Result<String> {
        // 必填参数非空校验和格式校验
        if (addressBook.detail.isNullOrBlank()) throw IllegalException(MessageConstant.Param.REQUIRED)
        if (addressBook.phone.isNullOrBlank()) throw IllegalException(MessageConstant.Param.REQUIRED)
        if (addressBook.phone.length != 11) throw IllegalException(MessageConstant.Param.TOO_LONG_OR_BLANK)
        if (addressBook.sex.isNullOrBlank()) throw IllegalException(MessageConstant.Param.REQUIRED)
        // 设置用户id
        addressBookService.save(addressBook)
        return Result.success()
    }

    @AutoLog(msg = "根据id查询地址")
    @Operation(summary = "根据id查询地址")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Result<AddressBook> {
        val addressBook = addressBookService.getById(id)
        return Result.success(addressBook)
    }

    @AutoLog(msg = "根据id修改地址")
    @Operation(summary = "根据id修改地址")
    @PutMapping
    fun update(@RequestBody addressBook: AddressBook): Result<String> {
        if (addressBook.id == null) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        if (addressBook.detail.isNullOrBlank()) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        if (addressBook.phone.isNullOrBlank()) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        if (!addressBook.phone.matches(Regex("\\d{11}"))) {
            throw IllegalException(MessageConstant.Param.ILLEGAL)
        }
        if (addressBook.sex.isNullOrBlank()) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        addressBookService.update(addressBook)
        return Result.success()
    }

    @AutoLog(msg = "设置默认地址")
    @Operation(summary = "设置默认地址")
    @PutMapping("/default")
    fun setDefault(@RequestBody addressBook: AddressBook): Result<String> {
        if (addressBook.id == null) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        addressBookService.setDefault(addressBook)
        return Result.success()
    }

    @AutoLog(msg = "根据id删除地址")
    @Operation(summary = "根据id删除地址")
    @DeleteMapping
    fun deleteById(@RequestParam id: Long): Result<String> {
        addressBookService.deleteById(id)
        return Result.success()
    }

    @AutoLog(msg = "查询默认地址")
    @Operation(summary = "查询默认地址")
    @GetMapping("/default")
    fun getDefault(): Result<AddressBook> {
        val addressBook = AddressBook().apply {
            isDefault = 1
            userId = BaseContext.getCurrentId()
        }
        val list = addressBookService.list(addressBook)
        if (list.size == 1) {
            return Result.success(list[0])
        }
        return Result.error("没有查询到默认地址")
    }
}
