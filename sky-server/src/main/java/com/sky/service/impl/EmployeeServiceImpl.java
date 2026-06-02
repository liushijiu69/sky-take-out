package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.EmployeeConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.*;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * 员工业务实现
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     * 1. 根据用户名查询数据库
     * 2. 处理各种异常情况（用户名不存在、密码不对、账号被锁定）
     * 3. 返回实体对象
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        //1、根据用户名查询数据库中的数据
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.Employee.NOT_FOUND);
        }
        //密码比对
        // 对明文密码进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.Employee.PASSWORD_ERROR);
        }

        if (employee.getStatus() == EmployeeConstant.Status.DISABLE.getCode()) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.Employee.LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * 1. 校验账号唯一性
     * 2. 设置默认密码、默认启用状态
     * 3. 向数据库插入数据
     */
    @Override
    public void saveEmployee(EmployeeDTO employeeDTO) {
        // 校验账号唯一性
        Integer count = employeeMapper.selectOne(employeeDTO.getUsername());
        if (count != null) {
            //已存在,不得重复
            throw new AlreadyExistedException(EmployeeConstant.USERNAME
                    + employeeDTO.getUsername()
                    + MessageConstant.Param.ALREADY_EXISTS);
        }

        //校验通过,向数据库插入数据
        Employee employee = Employee.builder()
                .name(employeeDTO.getName())
                .phone(employeeDTO.getPhone())
                .username(employeeDTO.getUsername())
                .sex(employeeDTO.getSex())
                .idNumber(employeeDTO.getIdNumber())
                // 为缺失字段设置默认值
                .password(DigestUtils.md5DigestAsHex(EmployeeConstant.DEFAULT_PASSWORD.getBytes()))
                .status(EmployeeConstant.Status.ENABLE.getCode())
                .build();
        employeeMapper.insertEmployee(employee);
    }

    /**
     * 员工分页查询
     */
    @Override
    public PageResult queryEmployeeByPage(EmployeePageQueryDTO empPageQueryDTO) {
        // 开始分页查询
        PageHelper.startPage(empPageQueryDTO.getPage(), empPageQueryDTO.getPageSize());
        Page<EmployeeVO> result = employeeMapper.selectByPage(empPageQueryDTO);
        return new PageResult(result.getTotal(), result.getResult());
    }

    /**
     * 启用或禁用员工账号
     */
    @Override
    public void startOrStopEmpAccount(Integer status, Long id) {
        //校验完成,更新数据库
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 修改员工
     * 1. 校验账号唯一性（排除自身）
     * 2. 更新数据库
     */
    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        // 校验账号唯一性（排除自身）
        Employee emp = employeeMapper.getByUsername(employeeDTO.getUsername());
        if (emp != null && !emp.getId().equals(employeeDTO.getId())) {
            //账号已存在,不得重复
            throw new AlreadyExistedException(EmployeeConstant.USERNAME
                    + employeeDTO.getUsername()
                    + MessageConstant.Param.ALREADY_EXISTS);
        }

        //校验通过,更新数据库
        Employee employee = Employee.builder()
                .id(employeeDTO.getId())
                .username(employeeDTO.getUsername())
                .name(employeeDTO.getName())
                .phone(employeeDTO.getPhone())
                .sex(employeeDTO.getSex())
                .idNumber(employeeDTO.getIdNumber())
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * 1. 查询数据库
     * 2. 转换为vo返回
     */
    @Override
    public EmployeeVO getById(Long id) {
        //查询数据库
        Employee employee = employeeMapper.getById(id);
        if (employee == null) {
            //员工不存在
            throw new AccountNotFoundException(MessageConstant.Employee.NOT_FOUND);
        }
        //转换为vo返回
        return EmployeeVO.builder()
                .id(employee.getId())
                .idNumber(employee.getIdNumber())
                .name(employee.getName())
                .password("******")
                .phone(employee.getPhone())
                .sex(employee.getSex())
                .status(employee.getStatus())
                .username(employee.getUsername())
                .createTime(employee.getCreateTime())
                .updateTime(employee.getUpdateTime())
                .createUser(employee.getCreateUser())
                .updateUser(employee.getUpdateUser())
                .build();
    }

}
