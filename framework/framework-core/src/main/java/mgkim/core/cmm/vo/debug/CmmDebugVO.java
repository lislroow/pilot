package mgkim.core.cmm.vo.debug;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mgkim.core.com.dto.KCmmVO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CmmDebugVO extends KCmmVO {

	public String ssid;
	public String userId;
	public long duration;
	public Date startTime;
	public Date stopTime;
	public String ipAddr;
	public String debugFilePath;

	public String getSessionId() {
		return ssid;
	}
	public void setSessionId(String sessionId) {
		this.ssid = sessionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long durationTimeSec) {
		this.duration = durationTimeSec;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getStopTime() {
		return stopTime;
	}
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	public String getIpAddr() {
		return ipAddr;
	}
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	public String getDebugFilePath() {
		return debugFilePath;
	}
	public void setDebugFilePath(String debugFilePath) {
		this.debugFilePath = debugFilePath;
	}


}