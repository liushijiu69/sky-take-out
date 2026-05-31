package com.sky.controller.admin

import com.sky.dto.DishDTO
import com.sky.dto.DishPageQueryDTO
import com.sky.entity.Dish
import com.sky.result.PageResult
import com.sky.result.Result
import com.sky.service.DishService
import com.sky.vo.DishVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
class DishController(
    private val dishService: DishService
) {
    private val log = LoggerFactory.getLogger(DishController::class.java)
    @Operation(summary = "新增菜品")
    @PostMapping
    fun save(@RequestBody dishDTO: DishDTO): Result<String>{
        log.info("新增菜品,参数:${dishDTO}")
        dishService.saveWithFlavor(dishDTO)
        return Result.success()
    }

    @Operation(summary = "菜品分页查询")
    @GetMapping("/page")
    fun pageQuery(dishPageQueryDTO: DishPageQueryDTO): Result<PageResult> {
        log.info("菜品分页查询,参数:${dishPageQueryDTO}")
        val pageResult = dishService.pageQuery(dishPageQueryDTO)
        return Result.success(pageResult)
    }

    @Operation(summary = "根据分类id查询菜品")
    @GetMapping("/list")
    fun list(@RequestParam categoryId: Long): Result<List<Dish>> {
        log.info("根据分类id查询菜品,categoryId:${categoryId}")
        return Result.success(dishService.listByCategoryId(categoryId))
    }

    /**
     * 批量删除菜品
     * @param ids 菜品id字符串，多个id用逗号分隔（如 "1,2,3"）
     */
    @Operation(summary = "批量删除菜品")
    @DeleteMapping
    fun delete(@RequestParam ids: String): Result<String> {
        log.info("批量删除菜品,ids:${ids}")
        dishService.deleteBatch(ids)
        return Result.success()
    }

    @Operation(summary = "根据id查询菜品")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Result<DishVO> {
        log.info("根据id查询菜品,id:${id}")
        return Result.success(dishService.getById(id))
    }

    @Operation(summary = "修改菜品")
    @PutMapping
    fun update(@RequestBody dishDTO: DishDTO): Result<String> {
        log.info("修改菜品,参数:${dishDTO}")
        dishService.updateWithFlavor(dishDTO)
        return Result.success()
    }

    @Operation(summary = "菜品起售、停售")
    @PostMapping("/status/{status}")
    fun startOrStop(@PathVariable status: Int, @RequestParam id: Long): Result<String> {
        log.info("菜品起售停售,status:${status},id:${id}")
        dishService.startOrStop(status, id)
        return Result.success()
    }
}