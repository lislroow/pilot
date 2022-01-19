package mgkim.framework.cmm.online.vo;

import java.util.List;

import mgkim.framework.core.dto.KCmmVO;

public class CmmSessionMngListVO extends KCmmVO {

	private List<String> sessionList = null;

	public List<String> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<String> sessionList) {
		this.sessionList = sessionList;
	}
}
