package com.sxj.supervisor.manage.controller.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sxj.supervisor.entity.system.FunctionEntity;
import com.sxj.supervisor.entity.system.SystemAccountEntity;
import com.sxj.supervisor.manage.controller.BaseController;
import com.sxj.supervisor.model.system.FunctionModel;
import com.sxj.supervisor.model.system.SysAccountQuery;
import com.sxj.supervisor.service.system.IFunctionService;
import com.sxj.supervisor.service.system.IRoleService;
import com.sxj.supervisor.service.system.ISystemAccountService;
import com.sxj.util.exception.WebException;
import com.sxj.util.persistent.ResultList;

@Controller
@RequestMapping("/system")
public class SystemAccountController extends BaseController {

	@Autowired
	private ISystemAccountService accountService;

	@Autowired
	private IFunctionService functionService;

	@Autowired
	private IRoleService roleService;

	@RequestMapping("account-list")
	public String getSysAccountList(SysAccountQuery query, ModelMap map) {
		if (query != null) {
			query.setPagable(true);
		}
		ResultList<SystemAccountEntity> list = accountService
				.queryAccounts(query);
		List<FunctionEntity> functionList = functionService
				.queryChildrenFunctions("0");
		map.put("list", list.getResults());
		map.put("functions", functionList);
		map.put("query", query);
		return "manage/system/account-list";

	}

	@RequestMapping("account-info")
	public String getgetSysAccount(String accountId, ModelMap map) {
		SystemAccountEntity account = accountService.getAccount(accountId);
		map.put("account", account);
		return "manage/system/account-info";
	}

	@RequestMapping("to_edit")
	public String toEditAccount(String accountId, ModelMap map) {
		SystemAccountEntity account = accountService.getAccount(accountId);
		map.put("account", account);
		return "manage/system/account-edit";
	}

	@RequestMapping("to_add")
	public String toAddAccount() {
		return "manage/system/account-add";
	}

	@RequestMapping("remove")
	public @ResponseBody Map<String, Object> remove(String accountId) {
		Map<String, Object> map = new HashMap<String, Object>();
		accountService.deleteAccount(accountId);
		map.put("isOK", true);
		return map;
	}

	@RequestMapping("add_account")
	public @ResponseBody Map<String, Object> addAccount(
			SystemAccountEntity account,
			@RequestParam("password_confirm") String password_confirm,
			@RequestParam("functionIds") String[] functionIds)
			throws WebException {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (!password_confirm.equals(account.getPassword())) {
				throw new WebException("两次密码不一致");
			}
			accountService.addAccount(account, functionIds);
			map.put("isOK", true);
			return map;
		} catch (Exception e) {
			throw new WebException(e.getMessage());
		}

	}

	@RequestMapping("init_password")
	public @ResponseBody Map<String, Object> initPassword(String accountId) {
		String password = accountService.initPassword(accountId);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("isOK", true);
		map.put("password", password);
		return map;
	}

	@RequestMapping("edit_account")
	public @ResponseBody Map<String, Object> editAccount(
			SystemAccountEntity account,
			@RequestParam("functionIds") String[] functionIds) {
		accountService.modifyAccount(account, functionIds);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("isOK", true);
		map.put("account", account);
		map.put("functionIds", functionIds);
		return map;
	}

	@RequestMapping("get_role_function")
	public String getRloeFunctions(String accountId, String type, ModelMap map) {
		if ("get".equals(type)) {
			List<FunctionModel> list = roleService.getRoleFunction(accountId);
			map.put("list", list);
			return "manage/system/role_function";
		} else if ("add".equals(type)) {
			List<FunctionModel> allList = functionService.queryFunctions();
			map.put("allList", allList);
			return "manage/system/edit_role";

		} else if ("edit".equals(type)) {
			List<FunctionEntity> list = roleService
					.getAllRoleFunction(accountId);
			List<FunctionModel> allList = functionService.queryFunctions();
			map.put("allList", allList);
			map.put("roleList", list);
			return "manage/system/edit_role";
		} else {
			return null;
		}

	}
}