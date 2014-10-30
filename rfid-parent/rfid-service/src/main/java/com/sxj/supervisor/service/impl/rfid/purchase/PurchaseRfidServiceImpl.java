package com.sxj.supervisor.service.impl.rfid.purchase;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sxj.spring.modules.mapper.JsonMapper;
import com.sxj.supervisor.dao.rfid.purchase.IRfidPurchaseDao;
import com.sxj.supervisor.entity.rfid.apply.RfidApplicationEntity;
import com.sxj.supervisor.entity.rfid.logistics.LogisticsRfidEntity;
import com.sxj.supervisor.entity.rfid.purchase.RfidPurchaseEntity;
import com.sxj.supervisor.entity.rfid.sale.RfidPriceEntity;
import com.sxj.supervisor.entity.rfid.sale.RfidSaleStatisticalEntity;
import com.sxj.supervisor.entity.rfid.window.WindowRfidEntity;
import com.sxj.supervisor.enu.rfid.RfidStateEnum;
import com.sxj.supervisor.enu.rfid.RfidTypeEnum;
import com.sxj.supervisor.enu.rfid.purchase.DeliveryStateEnum;
import com.sxj.supervisor.enu.rfid.purchase.ImportStateEnum;
import com.sxj.supervisor.enu.rfid.window.LabelProgressEnum;
import com.sxj.supervisor.model.rfid.RfidLog;
import com.sxj.supervisor.model.rfid.purchase.PurchaseRfidQuery;
import com.sxj.supervisor.service.rfid.IRfidKeyService;
import com.sxj.supervisor.service.rfid.app.IRfidApplicationService;
import com.sxj.supervisor.service.rfid.logistics.ILogisticsRfidService;
import com.sxj.supervisor.service.rfid.purchase.IPurchaseRfidService;
import com.sxj.supervisor.service.rfid.sale.IRfidPriceService;
import com.sxj.supervisor.service.rfid.sale.IRfidSaleStatisticalService;
import com.sxj.supervisor.service.rfid.window.IWindowRfidService;
import com.sxj.util.common.DateTimeUtils;
import com.sxj.util.exception.ServiceException;
import com.sxj.util.logger.SxjLogger;
import com.sxj.util.persistent.CustomDecimal;
import com.sxj.util.persistent.QueryCondition;

@Service
@Transactional
public class PurchaseRfidServiceImpl implements IPurchaseRfidService {

	@Autowired
	private IRfidPurchaseDao rfidPurchaseDao;

	@Autowired
	private IRfidSaleStatisticalService saleStatisticalService;

	@Autowired
	private IRfidPriceService rfidPriceService;

	@Autowired
	private IRfidApplicationService applyService;

	@Autowired
	private IWindowRfidService winRfidService;

	@Autowired
	private ILogisticsRfidService logisticsRfidService;

	@Autowired
	private IRfidKeyService keyService;

	@Override
	public List<RfidPurchaseEntity> queryPurchase(PurchaseRfidQuery query)
			throws ServiceException {
		try {
			if (query == null) {
				return null;
			}
			QueryCondition<RfidPurchaseEntity> condition = new QueryCondition<RfidPurchaseEntity>();
			condition.addCondition("purchaseNo", query.getPurchaseNo());
			condition.addCondition("applyNo", query.getApplyNo());
			condition.addCondition("contractNo", query.getContractNo());
			condition.addCondition("supplierName", query.getSupplierName());
			condition.addCondition("rfidType", query.getRfidType());
			condition.addCondition("payState", query.getPayState());
			condition.addCondition("receiptState", query.getReceiptState());
			condition.addCondition("importState", query.getImportState());
			condition.addCondition("startDate", query.getStartDate());
			condition.addCondition("endDate", query.getEndDate());
			condition.setPage(query);
			List<RfidPurchaseEntity> rfidList = rfidPurchaseDao
					.queryList(condition);
			query.setPage(condition);
			return rfidList;
		} catch (Exception e) {
			throw new ServiceException("查询采购单错误", e);
		}
	}

	@Override
	public void updatePurchase(RfidPurchaseEntity purchase)
			throws ServiceException {
		try {
			rfidPurchaseDao.updateRfidPurchase(purchase);
		} catch (Exception e) {
			throw new ServiceException("更新采购单错误", e);
		}

	}

	@Override
	@Transactional
	public void addPurchase(RfidPurchaseEntity purchase, String applyId,
			String hasNumber) throws ServiceException {
		try {
			RfidApplicationEntity app = applyService
					.getApplicationInfo(applyId);
			app.setHasNumber(Long.valueOf(hasNumber) + purchase.getCount());
			purchase.setApplyNo(app.getApplyNo());
			rfidPurchaseDao.addRfidPurchase(purchase);
			applyService.updateApp(app);
		} catch (Exception e) {
			throw new ServiceException("新增采购单错误", e);
		}

	}

	@Override
	public RfidPurchaseEntity getRfidPurchase(String id)
			throws ServiceException {
		try {
			RfidPurchaseEntity purchase = rfidPurchaseDao.getRfidPurchase(id);
			return purchase;
		} catch (Exception e) {
			throw new ServiceException("获取采购单错误", e);
		}

	}

	/**
	 * 确认发货
	 */
	@Override
	@Transactional
	public void confirmDelivery(String id) throws ServiceException {
		try {
			RfidPurchaseEntity purchase = rfidPurchaseDao.getRfidPurchase(id);
			purchase.setReceiptState(DeliveryStateEnum.receiving);
			rfidPurchaseDao.updateRfidPurchase(purchase);
			RfidSaleStatisticalEntity entity = new RfidSaleStatisticalEntity();
			entity.setApplyNo(purchase.getApplyNo());
			entity.setPurchaseNo(purchase.getPurchaseNo());
			entity.setSaleDate(new Date());
			entity.setCount(purchase.getCount());
			entity.setRfidType(purchase.getRfidType());
			List<RfidPriceEntity> list = rfidPriceService.queryPrice();
			if (list != null && list.size() > 0) {
				RfidPriceEntity price = list.get(0);
				if (purchase.getRfidType().getId() == 0) {
					entity.setPrice(price.getWindowPrice());
				} else {
					entity.setPrice(price.getLogisticsPrice());
				}

			} else {
				throw new ServiceException();
			}
			saleStatisticalService.add(entity);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional
	public void importRfid(String purchaseId) throws ServiceException {
		try {
			RfidPurchaseEntity purchase = getRfidPurchase(purchaseId);
			RfidApplicationEntity apply = applyService.getApplication(purchase
					.getApplyNo());
			RfidTypeEnum rfidType = purchase.getRfidType();
			Long count = purchase.getCount();
			if (RfidTypeEnum.door.equals(rfidType)) {
				WindowRfidEntity[] winRfids = new WindowRfidEntity[count
						.intValue()];
				for (int i = 0; i < count; i++) {
					WindowRfidEntity rfid = new WindowRfidEntity();
					rfid.setPurchaseNo(purchase.getPurchaseNo());
					rfid.setContractNo(purchase.getContractNo());
					rfid.setImportDate(new Date());
					rfid.setMemberNo(apply.getMemberNo());
					rfid.setMemberName(apply.getMemberName());
					rfid.setRfidState(RfidStateEnum.unused);
					rfid.setProgressState(LabelProgressEnum.unfilled);
					RfidLog log = new RfidLog();
					log.setState(RfidStateEnum.unused.getName());
					log.setDate(DateTimeUtils.getDateTime());
					String jsonLog = JsonMapper.nonDefaultMapper().toJson(log);
					rfid.setLog(jsonLog);
					Long key = keyService.getKey();
					rfid.setGenerateKey(key);
					String rfidNo = CustomDecimal.getDecimalString(4,
							new BigDecimal(key));
					rfid.setRfidNo(rfidNo);
					winRfids[i] = rfid;
				}
				winRfidService.batchAddWindowRfid(winRfids);
			} else {
				LogisticsRfidEntity[] rfids = new LogisticsRfidEntity[count
						.intValue()];
				for (int i = 0; i < count; i++) {
					LogisticsRfidEntity rfid = new LogisticsRfidEntity();
					rfid.setPurchaseNo(purchase.getPurchaseNo());
					// rfid.setContractNo(purchase.getContractNo());
					rfid.setImportDate(new Date());
					rfid.setRfidState(RfidStateEnum.unused);
					rfid.setMemberNo(apply.getMemberNo());
					rfid.setMemberName(apply.getMemberName());
					rfid.setProgressState(com.sxj.supervisor.enu.rfid.logistics.LabelStateEnum.unfilled);
					rfid.setType(rfidType);
					RfidLog log = new RfidLog();
					log.setState(RfidStateEnum.unused.getName());
					log.setDate(DateTimeUtils.getDateTime());
					String jsonLog = JsonMapper.nonDefaultMapper().toJson(log);
					rfid.setLog(jsonLog);
					Long key = keyService.getKey();
					rfid.setGenerateKey(key);
					String rfidNo = CustomDecimal.getDecimalString(4,
							new BigDecimal(key));
					rfid.setRfidNo(rfidNo);
					rfids[i] = rfid;
				}
				logisticsRfidService.batchAddLogistics(rfids);
			}
			purchase.setImportState(ImportStateEnum.imported);
			updatePurchase(purchase);
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException("导入RFID错误", e);
		}

	}
}
