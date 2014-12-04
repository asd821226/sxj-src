//
//
//  Generated by StarUML(tm) Java Add-In

//
//  @ Project : 私享家架构设计
//  @ File Name : MemberEntity.java
//  @ Date : 2014/8/11
//  @ Author :   
//
//

package com.sxj.finance.entity.member;

import java.io.Serializable;
import java.util.Date;

import com.sxj.finance.dao.member.IMemberDao;
import com.sxj.finance.enu.member.MemberCheckStateEnum;
import com.sxj.finance.enu.member.MemberStatesEnum;
import com.sxj.finance.enu.member.MemberTypeEnum;
import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Sn;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.orm.annotations.Version;
import com.sxj.mybatis.pagination.Pagable;

/**
 * 会员信息实体
 * 
 * @author Administrator
 *
 */
@Entity(mapper = IMemberDao.class)
@Table(name = "M_MEMBER")
public class MemberEntity extends Pagable implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 420909608351630992L;

	/**
	 * 标识
	 **/
	@Id(column = "ID")
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	/**
	 * 会员号
	 **/
	@Column(name = "MEMBER_NO")
	@Sn(pattern = "000000", step = 1, table = "T_SN", stub = "F_SN_NAME", sn = "F_SN_NUMBER", stubValueProperty = "noType")
	private String memberNo;

	private String noType;

	/**
	 * 会员密码
	 **/
	@Column(name = "PASSWORD")
	private String password;

	/**
	 * 会员名称
	 **/
	@Column(name = "NAME")
	private String name;

	/**
	 * 营业执照号
	 **/
	@Column(name = "B_LICENSE_NO")
	private String bLicenseNo;

	/**
	 * 会员类型
	 **/
	@Column(name = "TYPE")
	private MemberTypeEnum type;

	/**
	 * 地理区域
	 **/
	@Column(name = "AREA")
	private String area;

	/**
	 * 联系人名称
	 **/
	@Column(name = "CONTACTS")
	private String contacts;

	/**
	 * 节能标识号
	 **/
	@Column(name = "ENERGY_NO")
	private String energyNo;

	/**
	 * 联系人手机
	 **/
	@Column(name = "PHONE_NO")
	private String phoneNo;

	/**
	 * 公司地址
	 **/
	@Column(name = "ADDRESS")
	private String address;

	/**
	 * 联系电话
	 **/
	@Column(name = "TEL_NUM")
	private String telNum;

	/**
	 * 注册日期
	 **/
	@Column(name = "REG_DATE")
	private Date regDate;

	/**
	 * 认证日期
	 **/
	@Column(name = "AUTHOR_DATE")
	private Date authorDate;

	/**
	 * 账户状态
	 **/
	@Column(name = "STATE")
	private MemberStatesEnum state;

	/**
	 * 审核状态
	 **/
	@Column(name = "CHECK_STATE")
	private MemberCheckStateEnum checkState;

	/**
	 * 总交易额
	 **/
	@Column(name = "TOTAL_AMOUNT")
	private Long totalAmount;

	/**
	 * 子账户数目
	 **/
	@Column(name = "ACCOUNT_NUM")
	private Integer accountNum;

	/**
	 * 营业执照图片path
	 **/
	@Column(name = "B_LICENSE_PATH")
	private String bLicensePath;

	/**
	 * 节能标识图片path
	 **/
	@Column(name = "ENERGY_PATH")
	private String energyPath;

	/**
	 * 邮政编码
	 */
	@Column(name = "CODE")
	private String code;

	/**
	 * 是否完善会员资料标记
	 * 
	 * @return
	 */
	@Column(name = "FLAG")
	private Boolean flag;

	/**
	 * 扫描设备号
	 * 
	 * @return
	 */
	@Column(name = "DEVICE_NUMBER")
	private String deviceNumber;

	@Column(name = "VERSION_LOCK")
	@Version
	private Long version;

	@Column(name = "FINANCE_STATE")
	private Integer financeState;

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	public Boolean getFlag() {
		return flag;
	}

	public void setFlag(Boolean flag) {
		this.flag = flag;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getbLicensePath() {
		return bLicensePath;
	}

	public void setbLicensePath(String bLicensePath) {
		this.bLicensePath = bLicensePath;
	}

	public String getEnergyPath() {
		return energyPath;
	}

	public void setEnergyPath(String energyPath) {
		this.energyPath = energyPath;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getbLicenseNo() {
		return bLicenseNo;
	}

	public void setbLicenseNo(String bLicenseNo) {
		this.bLicenseNo = bLicenseNo;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getContacts() {
		return contacts;
	}

	public void setContacts(String contacts) {
		this.contacts = contacts;
	}

	public String getEnergyNo() {
		return energyNo;
	}

	public void setEnergyNo(String energyNo) {
		this.energyNo = energyNo;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTelNum() {
		return telNum;
	}

	public void setTelNum(String telNum) {
		this.telNum = telNum;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public Date getAuthorDate() {
		return authorDate;
	}

	public void setAuthorDate(Date authorDate) {
		this.authorDate = authorDate;
	}

	public Long getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Long totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Integer getAccountNum() {
		return accountNum;
	}

	public void setAccountNum(Integer accountNum) {
		this.accountNum = accountNum;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMemberNo() {
		return memberNo;
	}

	public void setMemberNo(String memberNo) {
		this.memberNo = memberNo;
	}

	public MemberStatesEnum getState() {
		return state;
	}

	public void setState(MemberStatesEnum state) {
		this.state = state;
	}

	public MemberCheckStateEnum getCheckState() {
		return checkState;
	}

	public void setCheckState(MemberCheckStateEnum checkState) {
		this.checkState = checkState;
	}

	public MemberTypeEnum getType() {
		return type;
	}

	public void setType(MemberTypeEnum type) {
		this.type = type;
	}

	public String getNoType() {
		return noType;
	}

	public void setNoType(String noType) {
		this.noType = noType;
	}

	public Integer getFinanceState() {
		return financeState;
	}

	public void setFinanceState(Integer financeState) {
		this.financeState = financeState;
	}

}