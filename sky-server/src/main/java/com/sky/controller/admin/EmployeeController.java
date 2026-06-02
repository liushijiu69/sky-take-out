package com.sky.controller.admin;

import com.sky.annotation.AutoLog;
import com.sky.constant.EmployeeConstant;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.IllegalException;
import com.sky.properties.JwtProperties;
import com.sky.vo.EmployeeVO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@Tag(name = "员工管理")
@RestController
@RequestMapping("/admin/employee")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     */
    @AutoLog(msg = "员工登录")
    @Operation(summary = "员工登录")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@Valid @RequestBody EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     */
    @AutoLog(msg = "员工退出登录")
    @Operation(summary = "员工退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     */
    @AutoLog(msg = "新增员工")
    @Operation(summary = "新增员工")
    @PostMapping
    public Result<String> postEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        employeeService.saveEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     */
    @AutoLog(msg = "员工分页查询")
    @Operation(summary = "员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> getEmployeesPage(@Valid EmployeePageQueryDTO empPageQueryDTO) {
        PageResult pageResult = employeeService.queryEmployeeByPage(empPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用或禁用员工账号
     */
    @AutoLog(msg = "启用或禁用员工")
    @Operation(summary = "启用或禁用员工")
    @PostMapping("/status/{status}")
    public Result<String> postEmployeeAccountStatus(@PathVariable Integer status, @RequestParam Long id) {
        //校验状态值是否合法(0禁用 1启用)
        if (!EmployeeConstant.Status.contains(status)) {
            throw new IllegalException(EmployeeConstant.STATUS + status + MessageConstant.Param.NOT_IN_RANGE);
        }
        employeeService.startOrStopEmpAccount(status, id);
        return Result.success();
    }

    /**
     * 修改员工信息
     */
    @AutoLog(msg = "修改员工")
    @Operation(summary = "修改员工")
    @PutMapping
    public Result<String> putEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        //校验必填参数（id在save时不需要，在update时必须）
        if (employeeDTO.getId() == null) {
            throw new IllegalException(MessageConstant.Param.REQUIRED);
        }
        employeeService.updateEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 根据id查询员工
     */
    @AutoLog(msg = "根据id查询员工")
    @Operation(summary = "根据id查询员工")
    @GetMapping("/{id}")
    public Result<EmployeeVO> getEmployeeById(@PathVariable Long id) {
        EmployeeVO employeeVO = employeeService.getById(id);
        return Result.success(employeeVO);
    }
}
