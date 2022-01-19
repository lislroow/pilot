package mgkim.framework.cmm.online.vo;

import mgkim.framework.core.dto.KCmmVO;

public class CmmSessionStatusVO extends KCmmVO {

	private String appCd = null;
	private String userId = null;
	private String aumthTpcd = null;
	private String ssid = null;
	private String loginDttm = null;
	private String ssStcd = null;
	private String expireDttm = null;
	private String browsInf = null;
	private String ip = null;
	private String privateKey = null;
	private String publicKey = null;
	private String symKey = null;
	private String dloginAlowYn;

	public String getAppCd() {
		return appCd;
	}
	public void setAppCd(String appCd) {
		this.appCd = appCd;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAumthTpcd() {
		return aumthTpcd;
	}
	public void setAumthTpcd(String aumthTpcd) {
		this.aumthTpcd = aumthTpcd;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getLoginDttm() {
		return loginDttm;
	}
	public void setLoginDttm(String loginDttm) {
		this.loginDttm = loginDttm;
	}
	public String getSsStcd() {
		return ssStcd;
	}
	public void setSsStcd(String ssStcd) {
		this.ssStcd = ssStcd;
	}
	public String getExpireDttm() {
		return expireDttm;
	}
	public void setExpireDttm(String expireDttm) {
		this.expireDttm = expireDttm;
	}
	public String getBrowsInf() {
		return browsInf;
	}
	public void setBrowsInf(String browsInf) {
		this.browsInf = browsInf;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getSymKey() {
		return symKey;
	}
	public void setSymKey(String symKey) {
		this.symKey = symKey;
	}
	public String getDloginAlowYn() {
		return dloginAlowYn;
	}
	public void setDloginAlowYn(String dloginAlowYn) {
		this.dloginAlowYn = dloginAlowYn;
	}
}
