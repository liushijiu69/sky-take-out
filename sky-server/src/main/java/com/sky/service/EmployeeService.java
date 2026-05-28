package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeVO;

/**
 * 员工业务接口
 */
public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void saveEmployee(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     * @param empPageQueryDTO
     * @return
     */
    PageResult queryEmployeeByPage(EmployeePageQueryDTO empPageQueryDTO);

    /**
     * 启用或禁用员工账号
     * @param status 状态(1启用 0禁用)
     * @param id 员工id
     */
    void startOrStopEmpAccount(Integer status, Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    void updateEmployee(EmployeeDTO employeeDTO);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    EmployeeVO getById(Long id);
}
