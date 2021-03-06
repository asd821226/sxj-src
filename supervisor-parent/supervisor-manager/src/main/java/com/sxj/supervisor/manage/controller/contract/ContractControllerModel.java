package com.sxj.supervisor.manage.controller.contract;

import java.io.Serializable;
import java.util.List;

import com.sxj.supervisor.entity.contract.ContractEntity;
import com.sxj.supervisor.entity.contract.ContractItemEntity;

public class ContractControllerModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3300733982514378519L;

	/**
	 * 合同实体
	 */
	private ContractEntity contract;

	/**
	 * 产品条目
	 */
	private List<ContractItemEntity> items;
	/**
	 * 备案ID
	 */
	private String recordId;
	
	
	

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public List<ContractItemEntity> getItems() {
		return items;
	}

	public void setItems(List<ContractItemEntity> items) {
		this.items = items;
	}

	public ContractEntity getContract() {
		return contract;
	}

	public void setContract(ContractEntity contract) {
		this.contract = contract;
	}

}
