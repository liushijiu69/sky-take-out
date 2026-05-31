package com.sky.controller.admin

import com.sky.dto.SetmealDTO
import com.sky.dto.SetmealPageQueryDTO
import com.sky.result.PageResult
import com.sky.result.Result
import com.sky.service.SetmealService
import com.sky.vo.SetmealVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
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
class SetmealController(
    private val setmealService: SetmealService
) {
    private val log = LoggerFactory.getLogger(SetmealController::class.java)

    /**
     * 新增套餐
     */
    @Operation(summary = "新增套餐")
    @PostMapping
    fun save(@RequestBody setmealDTO: SetmealDTO): Result<String> {
        log.info("新增套餐,参数:${setmealDTO}")
        setmealService.saveWithDish(setmealDTO)
        return Result.success()
    }

    @Operation(summary = "套餐分页查询")
    @GetMapping("/page")
    fun pageQuery(setmealPageQueryDTO: SetmealPageQueryDTO): Result<PageResult> {
        log.info("套餐分页查询,参数:${setmealPageQueryDTO}")
        return Result.success(setmealService.pageQuery(setmealPageQueryDTO))
    }

    @Operation(summary = "批量删除套餐")
    @DeleteMapping
    fun delete(@RequestParam ids: String): Result<String> {
        log.info("批量删除套餐,ids:${ids}")
        setmealService.deleteBatch(ids)
        return Result.success()
    }

    @Operation(summary = "根据id查询套餐")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Result<SetmealVO> {
        log.info("根据id查询套餐,id:${id}")
        return Result.success(setmealService.getById(id))
    }

    @Operation(summary = "修改套餐")
    @PutMapping
    fun update(@RequestBody setmealDTO: SetmealDTO): Result<String> {
        log.info("修改套餐,参数:${setmealDTO}")
        setmealService.updateWithDish(setmealDTO)
        return Result.success()
    }

    @Operation(summary = "套餐起售、停售")
    @PostMapping("/status/{status}")
    fun startOrStop(@PathVariable status: Int, @RequestParam id: Long): Result<String> {
        log.info("套餐起售停售,status:${status},id:${id}")
        setmealService.startOrStop(status, id)
        return Result.success()
    }
}
