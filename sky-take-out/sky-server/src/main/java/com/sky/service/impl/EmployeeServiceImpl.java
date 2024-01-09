package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

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
        //密码MD5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
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

    /**
     * 新增用户
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //把DTO对象转换为PO
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置用户状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置用户初始密码
        //先进行MD5加密
        String md5Password = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());
        employee.setPassword(md5Password);
        //设置新增时间
        //employee.setCreateTime(LocalDateTime.now());
        //设置修改时间
        //employee.setUpdateTime(LocalDateTime.now());
        // 需要把新增人和修改人改为动态的，现在是写死的，需要根据管理用户来修改  2023年12月10日21:55:35
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        //调用mapper接口
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //1.开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page =  employeeMapper.pageQuery(employeePageQueryDTO);
        //2.1获取total总记录数
        long total = page.getTotal();
        //2.2 获取数据集合
        List<Employee> records = page.getResult();
        //3. 封装为PageResult对象并返回
        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //update employee set status = ? where id = ?
        //为了提高复用性，我们选择用员工实体类封装
        //使用Builder
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
       Employee employee =  employeeMapper.getById(id);
       return employee;
    }

    /**
     * 编辑员工信息 2023年12月12日15:06:15
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        //1.先把DTO对象转为PO实体类
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        //2.把部分属性补全
        //employee.setUpdateTime(LocalDateTime.now());
        //mployee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);

    }

    /**
     * 修改密码 2024年1月7日22:27:06 -- 在完成所有接口后发现遗漏的修改密码功能
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        //获取当前登录用户的id
        Long empId = BaseContext.getCurrentId();
        //根据id查询数据库，校验原始密码是否正确，不正确则抛出异常
        Employee employeeDB = employeeMapper.getById(empId);
        //正确密码
        String correctPassword = employeeDB.getPassword();
        //传过来的原始密码
        String oldPassword = passwordEditDTO.getOldPassword();
        //数据库中的密码是进行过MD5加密处理，所以先把传过来的原始密码进行md5加密
        String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        //密码比对
        if (!correctPassword.equals(md5OldPassword)){
            throw new PasswordErrorException("原密码错误！");
        }
        //如果输入的原密码与数据库密码相同，则进行修改，把newPassword进行MD5加密存入数据库，密码修改成功
        Employee employee = Employee.builder()
                .password(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()))
                .id(empId)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee);

    }

}
