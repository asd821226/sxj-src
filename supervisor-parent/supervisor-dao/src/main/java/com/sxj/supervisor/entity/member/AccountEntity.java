package com.sxj.supervisor.entity.member;

import java.io.Serializable;

import com.sxj.mybatis.pagination.Pagable;

/**
 * 子账号实体
 * @author Administrator
 *
 */
public class AccountEntity extends Pagable implements Serializable {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = -7970981306803065289L;

	/**
	 * 主键标识
	**/
	private String id;
	
	/**
	 * 所属会员ID
	**/
	private String parentId;
	
	/**
	 * 子账户名称
	**/
	private String accountName;
	
	/**
	 * 姓名
	**/
	private String name;
	
	/**
	 * 子账户状态
	**/
	private Integer state;
	
	/**
	 * 注册日期
	**/
	private Object regDate;
	
	/**
	 * 子账户密码
	**/
	private String password;
	
	/**
	 * 子账户ID
	**/
	private String accountNo;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Object getRegDate() {
		return regDate;
	}

	public void setRegDate(Object regDate) {
		this.regDate = regDate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
}
