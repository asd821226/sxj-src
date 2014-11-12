package com.sxj.supervisor.service.rfid.window;

import java.util.List;

import com.sxj.supervisor.entity.rfid.window.WindowRfidEntity;
import com.sxj.supervisor.enu.rfid.window.WindowTypeEnum;
import com.sxj.supervisor.model.rfid.base.LogModel;
import com.sxj.supervisor.model.rfid.window.WindowRfidQuery;
import com.sxj.util.exception.ServiceException;

public interface IWindowRfidService {
	/**
	 * 根据条件高级查询
	 * 
	 * @param query
	 * @return
	 * @throws ServiceException
	 */
	public List<WindowRfidEntity> queryWindowRfid(WindowRfidQuery query)
			throws ServiceException;

	/**
	 * 更新
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public void updateWindowRfid(WindowRfidEntity win) throws ServiceException;

	/**
	 * 启用门窗RFID
	 * 
	 * @throws ServiceException
	 */
	public void startWindowRfid(Long itemQuantity, Long useQuantity,
			String refContractNo, String minRfid, String maxRfid, String gRfid,
			String lRfid, WindowTypeEnum windowType) throws ServiceException;

	/**
	 * 批量新增
	 * 
	 * @param rfids
	 * @throws ServiceException
	 */
	public Integer batchAddWindowRfid(WindowRfidEntity[] rfids)
			throws ServiceException;

	/**
	 * 批量更新
	 * 
	 * @param rfids
	 * @throws ServiceException
	 */
	public void batchUpdateWindowRfid(WindowRfidEntity[] rfids)
			throws ServiceException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public List<LogModel> getRfidStateLog(String id) throws ServiceException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public WindowRfidEntity getWindowRfid(String id) throws ServiceException;

	/**
	 * 
	 * @param contractNo
	 * @param count
	 * @return
	 * @throws ServiceException
	 */
	public String[] getMaxRfidNo(String contractNo, Long count)
			throws ServiceException;

	/**
	 * 补损RFID标签
	 */
	public void lossWindowRfid(String refContractNo, String minRfid,
			String maxRfid, String gRfid, String lRfid, String[] addRfid,
			Long count) throws ServiceException;

	/**
	 * 安装
	 */
	public int stepWindow(String rfidNo) throws ServiceException;

	/**
	 * 质检
	 */
	public int testWindow(String contractNo, String[] rfidNos)
			throws ServiceException;
}
