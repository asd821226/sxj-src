package com.sxj.supervisor.entity.member;

import java.io.Serializable;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.pagination.Pagable;
import com.sxj.supervisor.dao.member.IMemberFunctionDao;

@Entity(mapper = IMemberFunctionDao.class)
@Table(name = "M_MEMBER_FUNCTION")
public class MemberFunctionEntity extends Pagable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7134010050835980843L;

	/**
	 * 主键标识
	 **/
	@Id(column = "ID")
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	/**
	 * 功能名称
	 **/
	@Column(name = "TITLE")
	private String title;

	/**
	 * 功能请求URL
	 **/
	@Column(name = "URL")
	private String url;

	/**
	 * 父系统功能ID
	 **/
	@Column(name = "PARENT_ID")
	private String parentId;
	/**
	 * 级别
	 **/
	@Column(name = "LEVEL")
	private Integer level;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

}
