package com.sxj.supervisor.service.function;

import java.util.List;

import com.sxj.supervisor.entity.member.FunctionEntity;
import com.sxj.supervisor.model.function.FunctionModel;


public interface IFunctionService {

	/**
	 * 读取所有菜单
	 * @return
	 */
	public List<FunctionModel> queryFunctions();

	/**
	 * 根据获取系统功能信息
	 * @param id
	 * @return
	 */
	public FunctionEntity getFunction(String id);
}
