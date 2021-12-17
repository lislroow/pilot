package mgkim.framework.online.cmm.vo.privacy;

import mgkim.framework.core.dto.KCmmVO;

public class CmmPrivacyLogVO extends KCmmVO {

	private String siteTpcd;
	private String mngtgId;
	private String mngtgTpcd;
	private String logDt;
	private String dailySeq;
	private String ip;
	private String userId;
	private String ssid;
	private String txid;
	private String logFpath;

	public String getSiteTpcd() {
		return siteTpcd;
	}
	public void setSiteTpcd(String siteTpcd) {
		this.siteTpcd = siteTpcd;
	}
	public String getMngtgId() {
		return mngtgId;
	}
	public void setMngtgId(String mngtgId) {
		this.mngtgId = mngtgId;
	}
	public String getMngtgTpcd() {
		return mngtgTpcd;
	}
	public void setMngtgTpcd(String mngtgTpcd) {
		this.mngtgTpcd = mngtgTpcd;
	}
	public String getLogDt() {
		return logDt;
	}
	public void setLogDt(String logDt) {
		this.logDt = logDt;
	}
	public String getDailySeq() {
		return dailySeq;
	}
	public void setDailySeq(String dailySeq) {
		this.dailySeq = dailySeq;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
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
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
	public String getLogFpath() {
		return logFpath;
	}
	public void setLogFpath(String logFpath) {
		this.logFpath = logFpath;
	}
}