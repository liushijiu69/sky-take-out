package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.EmployeeConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.*;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工业务实现
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;


    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        //校验传入参数格式合法
        if (employeeLoginDTO.getUsername() == null || employeeLoginDTO.getPassword() == null) {
            throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        //校验账号格式
        if (employeeLoginDTO.getUsername().length() > 32 || employeeLoginDTO.getUsername().isEmpty()) {
            //长度不合要求,直接返回错误
            throw new IllegalException(EmployeeConstant.USERNAME
                    + employeeLoginDTO.getUsername()
                    + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK
            );
        }
        //校验密码格式
        if (employeeLoginDTO.getPassword().length() > 64 || employeeLoginDTO.getPassword().isEmpty()) {
            //长度不合要求,直接返回错误
            throw new IllegalException(EmployeeConstant.PASSWORD
                    + employeeLoginDTO.getPassword()
                    + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK
            );
        }
        //校验通过,查询数据库
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.LoginError.ACCOUNT_NOT_FOUND);
        }
        //密码比对
        // 对明文密码进行md5加密，然后再进行比对
        password =  DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.LoginError.PASSWORD_ERROR);
        }

        if (employee.getStatus() == EmployeeConstant.Status.DISABLE.getValue()) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.LoginError.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void saveEmployee(EmployeeDTO employeeDTO) {
        //校验传入参数是否合规
        if (employeeDTO.getIdNumber() == null
                || employeeDTO.getName() == null
                || employeeDTO.getPhone() == null
                || employeeDTO.getSex() == null
                || employeeDTO.getUsername() == null
        ) {
            throw new  IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        
        //校验身份证是否合规
        if (employeeDTO.getIdNumber().length() != 18){
            throw new IllegalException(EmployeeConstant.ID_NUMBER
                    + employeeDTO.getIdNumber()
                    + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK
            );
        }
        //校验姓名是否合规
        if (employeeDTO.getName().length() > 32 || employeeDTO.getName().isEmpty()) {
            throw new IllegalException(EmployeeConstant.NAME
                    + employeeDTO.getName()
                    + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK
            );
        }
        // 校验手机号是否合规
        if (employeeDTO.getPhone().length() != 11){
            throw new IllegalException(EmployeeConstant.PHONE
                    + employeeDTO.getIdNumber()
                    + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK
            );
        }
        //校验性别是否合规
        if (!EmployeeConstant.Sex.contains(employeeDTO.getSex())){
            throw new IllegalException(EmployeeConstant.SEX
                    + employeeDTO.getSex()
                    + MessageConstant.ParamIllegal.NOT_IN_RANGE
            );
        }
        // 校验账户是否合规
        if (employeeDTO.getUsername().length() > 32 || employeeDTO.getUsername().isEmpty()) {
            //长度不合要求
            throw new IllegalException(EmployeeConstant.USERNAME
                    + employeeDTO.getUsername()
                    + MessageConstant.ParamIllegal.TO_LONG_OR_BLANK
            );
        }
        Integer count =  employeeMapper.selectOne(employeeDTO.getUsername());
        if (count != null) {
            //已存在,不得重复
            throw new AlreadyExistedException(EmployeeConstant.USERNAME
                    + employeeDTO.getUsername()
                    + MessageConstant.ParamIllegal.ALREADY_EXISTED
            );
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
                .password(DigestUtils.md5DigestAsHex(EmployeeConstant.DEFAULT_PASSWORD.getBytes()))
                .status(EmployeeConstant.Status.ENABLE.getValue())
                // 设置当前记录的创建时间和修改时间
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                // 设置当前记录创建人id和修改人id
                .createUser(BaseContext.getCurrentId())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.insertEmployee(employee);
    }

    @Override
    public PageResult queryEmployeeByPage(EmployeePageQueryDTO empPageQueryDTO) {
        //校验传入参数格式
        if (empPageQueryDTO.getPage() == null || empPageQueryDTO.getPageSize() == null ) {
            throw new  IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        }
        int page = empPageQueryDTO.getPage();//页码
        int pageSize = empPageQueryDTO.getPageSize();//每页记录数
        if (page < 1) throw new IllegalException(MessageConstant.ParamIllegal.NOT_IN_RANGE);
        if (pageSize < 0) throw new IllegalException(MessageConstant.ParamIllegal.NOT_IN_RANGE);
        //校验通过
        // 开始分页查询
        PageHelper.startPage(page, pageSize);
        Page<EmployeeVO> result =  employeeMapper.selectByPage(empPageQueryDTO);
        long total = result.getTotal();
        List<EmployeeVO> record = result.getResult();
        return new PageResult(total,record);
    }

    @Override
    public void startOrStopEmpAccount(Integer status, Long id) {
        //校验参数
        if (status==null || id==null) throw new IllegalException(MessageConstant.ParamIllegal.PARAMETERS_ILLEGAL);
        if (!EmployeeConstant.Status.contains(status)) {
            throw new IllegalException(EmployeeConstant.STATUS
                    + status
                    + MessageConstant.ParamIllegal.NOT_IN_RANGE
            );
        }
        //校验完成,更新数据库
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        employeeMapper.update(employee);
    }

}
