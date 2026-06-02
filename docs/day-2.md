# Day 2：完善员工管理

## 1. 新增员工

在 `EmployeeController` 中新增方法 `postEmployee`，访问 URL 为 `/admin/employee`。使用 `@RequestBody EmployeeDTO` 接收参数，返回 `Result.success()`。

完成 Service 层逻辑，留下 TODO 注释：`// TODO 设置当前记录创建人id和修改人id,暂时填充固定值1L`，留待后续完善。

完成 Mapper 层的 SQL 语句。

使用 Apifox 发送请求测试，发现报错。经排查，`EmployeeController` 上已定义 URL `/admin/employee`，而 `postEmployee` 方法上重复定义了 `/employee`。删除重复定义后，测试成功。

重复发送请求，报 `SQLIntegrityConstraintViolationException: Duplicate entry 'quanbo123' for key 'employee.idx_username'`。我认为应在 Service 层校验用户名是否重复，若重复则向客户端说明原因。

在 Mapper 层新增 `Integer selectOne(String username)` 方法，用于判断用户名是否已存在。新增一个异常类用于表示字段已存在。在 Service 层新增判断逻辑，若已存在则抛出业务异常，提醒客户端账号已存在。

再次测试，发现用户名已存在的情况下，依然会向数据库发送插入语句。仔细查看后发现 Service 层判断逻辑写反了，修正后恢复正常。

解决 TODO 问题：使用工具类 `BaseContext`，在拦截器校验 JWT 时将解析出的用户 ID 存入 `ThreadLocal`，在 Service 层取出 ID 填入创建人 ID 和修改人 ID。在拦截器的 `afterCompletion` 方法中使用 `remove` 方法移除存入的 ID。

再次测试，功能正常，新增员工功能完成。

测试分页查询时发现新增员工存在问题：前端发送时将性别字段设为 `"0"/"1"`，而我用 Apifox 发送时设为 `"男"`，两者均插入成功，说明 Service 层对传入参数缺少校验。

为 Service 层已有方法添加参数校验，依据数据库表的约束进行校验。

## 2. 员工分页查询

在 Controller 层定义方法 `public Result<PageResult> getEmployeesPage(@RequestParam EmployeePageQueryDTO empPageQueryDTO)`，访问 URL 为 `/admin/employee/page`，返回 `Result.success(pageResult)`。

新增 `EmployeeVO` 类作为向前端展示的数据。完成 Service 层逻辑和 Mapper 层代码。

Apifox 测试成功。

## 3. 禁用/启用员工账号

在 Controller 层定义方法 `public Result<String> postEmployeeAccountStatus(@PathVariable Integer status, @RequestParam Long id)`，访问 URL 为 `/admin/employee/status/{status}`，返回 `Result.success()`。

完成 Service 层代码和 Mapper 层代码。

测试成功。

## 4. 根据 ID 获取员工和修改员工

使用 OpenCode 快速完成，修改了一些有问题的逻辑。

使用 Apifox 测试发现修改逻辑有问题，修改校验账户已存在的判断逻辑。

测试成功。

## 5. 导入分类管理代码

导入代码，使用 OpenCode 修改导入的代码以适配当前项目。

查看 Service 层逻辑，指导 OpenCode 将代码改为符合项目风格的写法。

让 OpenCode 完善现有代码的注释。

前后端联调测试通过，提交并推送代码。
