package mgkim.framework.core.session;

public class KSession {

	private String appCd;
	private String userTpcd;
	private String aumthTpcd;
	private String userId;
	private String ssid;
	private String ip;

	public String getAppCd() {
		return appCd;
	}
	public void setAppCd(String appCd) {
		this.appCd = appCd;
	}
	public String getUserTpcd() {
		return userTpcd;
	}
	public void setUserTpcd(String userTpcd) {
		this.userTpcd = userTpcd;
	}
	public String getAumthTpcd() {
		return aumthTpcd;
	}
	public void setAumthTpcd(String aumthTpcd) {
		this.aumthTpcd = aumthTpcd;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
}
