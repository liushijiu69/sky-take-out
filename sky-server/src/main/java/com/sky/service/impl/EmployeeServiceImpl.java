package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.AlreadyExistedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

/**
 * 员工业务实现
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {

            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对明文密码进行md5加密，然后再进行比对
        password =  DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void saveEmployee(EmployeeDTO employeeDTO) {
        // 校验账户是否已存在
        Integer count =  employeeMapper.selectOne(employeeDTO.getUsername());
        if (count != null) {
            throw new AlreadyExistedException(employeeDTO.getUsername() + MessageConstant.ALREADY_EXISTED);
        }
        //校验通过,向数据库插入数据
        Employee employee = Employee.builder()
                // 设置传入的参数
//                .id(employeeDTO.getId())
                .name(employeeDTO.getName())
                .phone(employeeDTO.getPhone())
                .username(employeeDTO.getUsername())
                .sex(employeeDTO.getSex())
                .idNumber(employeeDTO.getIdNumber())
                // 为缺失字段设置默认值
                .password(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()))
                .status(StatusConstant.ENABLE)
                // 设置当前记录的创建时间和修改时间
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                // 设置当前记录创建人id和修改人id
                .createUser(BaseContext.getCurrentId())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.insertEmployee(employee);
    }

}
