package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工数据访问层
 */
@Mapper
public interface EmployeeMapper {

    Employee getByUsername(String username);

    @AutoFill(AutoFill.OperationType.INSERT)
    void insertEmployee(Employee employee);

    Integer selectOne(String username);

    Page<EmployeeVO> selectByPage(EmployeePageQueryDTO empPageQueryDTO);

    Employee getById(Long id);

    @AutoFill(AutoFill.OperationType.UPDATE)
    void update(Employee employee);
}
