package com.sky.controller.admin

import com.sky.annotation.AutoLog
import com.sky.constant.MessageConstant
import com.sky.constant.SetmealConstant
import com.sky.dto.SetmealDTO
import com.sky.dto.SetmealPageQueryDTO
import com.sky.exception.IllegalException
import com.sky.result.PageResult
import com.sky.result.Result
import com.sky.service.SetmealService
import com.sky.vo.SetmealVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 套餐管理 Controller
 */
@Tag(name = "套餐相关接口")
@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Validated
class SetmealController(
    private val setmealService: SetmealService
) {

    /**
     * 新增套餐
     */
    @AutoLog(msg = "新增套餐")
    @Operation(summary = "新增套餐")
    @PostMapping
    fun save(@Valid @RequestBody setmealDTO: SetmealDTO): Result<String> {
        //校验售卖状态是否合法(0停售 1起售)
        if (setmealDTO.status == null || !SetmealConstant.SetmealStatus.contains(setmealDTO.status)) {
            throw IllegalException(SetmealConstant.STATUS + MessageConstant.Param.NOT_IN_RANGE)
        }
        //校验套餐菜品列表
        if (setmealDTO.setmealDishes.isEmpty()) {
            throw IllegalException(MessageConstant.Param.REQUIRED)
        }
        setmealDTO.setmealDishes.forEach {
            if (it.dishId == null || it.copies == null || it.name == null || it.price == null) {
                throw IllegalException(MessageConstant.Param.REQUIRED)
            }
        }
        setmealService.saveWithDish(setmealDTO)
        return Result.success()
    }

    /**
     * 套餐分页查询
     */
    @AutoLog(msg = "套餐分页查询")
    @Operation(summary = "套餐分页查询")
    @GetMapping("/page")
    fun pageQuery(@Valid setmealPageQueryDTO: SetmealPageQueryDTO): Result<PageResult> {
        return Result.success(setmealService.pageQuery(setmealPageQueryDTO))
    }

    /**
     * 批量删除套餐
     */
    @AutoLog(msg = "批量删除套餐")
    @Operation(summary = "批量删除套餐")
    @DeleteMapping
    fun delete(@RequestParam ids: String): Result<String> {
        setmealService.deleteBatch(ids)
        return Result.success()
    }

    /**
     * 根据id查询套餐
     */
    @AutoLog(msg = "根据id查询套餐")
    @Operation(summary = "根据id查询套餐")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Result<SetmealVO> {
        return Result.success(setmealService.getById(id))
    }

    /**
     * 修改套餐
     */
    @AutoLog(msg = "修改套餐")
    @Operation(summary = "修改套餐")
    @PutMapping
    fun update(@Valid @RequestBody setmealDTO: SetmealDTO): Result<String> {
        //校验必填参数（id在save时不需要，在update时必须）
        if (setmealDTO.id == null) throw IllegalException(MessageConstant.Param.REQUIRED)
        setmealService.updateWithDish(setmealDTO)
        return Result.success()
    }

    /**
     * 套餐起售、停售
     */
    @AutoLog(msg = "套餐起售停售")
    @Operation(summary = "套餐起售、停售")
    @PostMapping("/status/{status}")
    fun startOrStop(@PathVariable status: Int, @RequestParam id: Long): Result<String> {
        //校验状态值是否合法(0停售 1起售)
        if (!SetmealConstant.SetmealStatus.contains(status)) {
            throw IllegalException(SetmealConstant.STATUS + MessageConstant.Param.NOT_IN_RANGE)
        }
        setmealService.startOrStop(status, id)
        return Result.success()
    }
}
