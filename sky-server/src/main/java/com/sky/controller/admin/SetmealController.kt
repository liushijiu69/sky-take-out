package com.sky.controller.admin

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
import org.slf4j.LoggerFactory
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
@RestController
@RequestMapping("/admin/setmeal")
@Validated
class SetmealController(
    private val setmealService: SetmealService
) {
    private val log = LoggerFactory.getLogger(SetmealController::class.java)

    /**
     * 新增套餐
     */
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
        log.info("新增套餐,参数:${setmealDTO}")
        setmealService.saveWithDish(setmealDTO)
        return Result.success()
    }

    /**
     * 套餐分页查询
     */
    @Operation(summary = "套餐分页查询")
    @GetMapping("/page")
    fun pageQuery(@Valid setmealPageQueryDTO: SetmealPageQueryDTO): Result<PageResult> {
        log.info("套餐分页查询,参数:${setmealPageQueryDTO}")
        return Result.success(setmealService.pageQuery(setmealPageQueryDTO))
    }

    /**
     * 批量删除套餐
     */
    @Operation(summary = "批量删除套餐")
    @DeleteMapping
    fun delete(@RequestParam ids: String): Result<String> {
        log.info("批量删除套餐,ids:${ids}")
        setmealService.deleteBatch(ids)
        return Result.success()
    }

    /**
     * 根据id查询套餐
     */
    @Operation(summary = "根据id查询套餐")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Result<SetmealVO> {
        log.info("根据id查询套餐,id:${id}")
        return Result.success(setmealService.getById(id))
    }

    /**
     * 修改套餐
     */
    @Operation(summary = "修改套餐")
    @PutMapping
    fun update(@Valid @RequestBody setmealDTO: SetmealDTO): Result<String> {
        //校验必填参数（id在save时不需要，在update时必须）
        if (setmealDTO.id == null) throw IllegalException(MessageConstant.Param.REQUIRED)
        log.info("修改套餐,参数:${setmealDTO}")
        setmealService.updateWithDish(setmealDTO)
        return Result.success()
    }

    /**
     * 套餐起售、停售
     */
    @Operation(summary = "套餐起售、停售")
    @PostMapping("/status/{status}")
    fun startOrStop(@PathVariable status: Int, @RequestParam id: Long): Result<String> {
        //校验状态值是否合法(0停售 1起售)
        if (!SetmealConstant.SetmealStatus.contains(status)) {
            throw IllegalException(SetmealConstant.STATUS + MessageConstant.Param.NOT_IN_RANGE)
        }
        log.info("套餐起售停售,status:${status},id:${id}")
        setmealService.startOrStop(status, id)
        return Result.success()
    }
}
