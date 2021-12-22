package mgkim.service.www.cmm.vo;

public class CmmUserLoginPolicyVO {

	private String siteTpcd;
	private String userId;
	private String aumthTpcdList;
	private String dloginAlowYn;
	private String inactvYn;
	private String lockYn;
	private int loginFailCnt;
	private String pwchgDt;
	private int ssvaldSec;
	
	public String getSiteTpcd() {
		return siteTpcd;
	}
	public void setSiteTpcd(String siteTpcd) {
		this.siteTpcd = siteTpcd;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAumthTpcdList() {
		return aumthTpcdList;
	}
	public void setAumthTpcdList(String aumthTpcdList) {
		this.aumthTpcdList = aumthTpcdList;
	}
	public String getDloginAlowYn() {
		return dloginAlowYn;
	}
	public void setDloginAlowYn(String dloginAlowYn) {
		this.dloginAlowYn = dloginAlowYn;
	}
	public String getInactvYn() {
		return inactvYn;
	}
	public void setInactvYn(String inactvYn) {
		this.inactvYn = inactvYn;
	}
	public String getLockYn() {
		return lockYn;
	}
	public void setLockYn(String lockYn) {
		this.lockYn = lockYn;
	}
	public int getLoginFailCnt() {
		return loginFailCnt;
	}
	public void setLoginFailCnt(int loginFailCnt) {
		this.loginFailCnt = loginFailCnt;
	}
	public String getPwchgDt() {
		return pwchgDt;
	}
	public void setPwchgDt(String pwchgDt) {
		this.pwchgDt = pwchgDt;
	}
	public int getSsvaldSec() {
		return ssvaldSec;
	}
	public void setSsvaldSec(int ssvaldSec) {
		this.ssvaldSec = ssvaldSec;
	}
}
