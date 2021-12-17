package mgkim.framework.online.com.session;

public class KToken {

	@Deprecated
	private long createTime;
	private long expireTime;
	private String siteTpcd;
	private String userTpcd;
	private String aumthTpcd;
	private String jwtTpcd;
	private String userId;
	private String guid;
	private String ssid;
	private String browsInf;
	private String ip;
	private int ssvaldSec;


	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	public String getSiteTpcd() {
		return siteTpcd;
	}
	public void setSiteTpcd(String siteTpcd) {
		this.siteTpcd = siteTpcd;
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
	public String getJwtTpcd() {
		return jwtTpcd;
	}
	public void setJwtTpcd(String jwtTpcd) {
		this.jwtTpcd = jwtTpcd;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
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
	public int getSsvaldSec() {
		return ssvaldSec;
	}
	public void setSsvaldSec(int ssvaldSec) {
		this.ssvaldSec = ssvaldSec;
	}
}
