package mgkim.framework.online.cmm.vo.sessionexpmng;

import java.util.List;

import mgkim.framework.online.com.dto.KCmmVO;
import mgkim.framework.online.com.session.KToken;

public class CmmSessionMngListVO extends KCmmVO {

	private List<KToken> sessionList = null;

	public List<KToken> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<KToken> sessionList) {
		this.sessionList = sessionList;
	}
}
