package mgkim.core.cmm.vo.sessionexpmng;

import java.util.List;

import mgkim.core.com.dto.KCmmVO;
import mgkim.core.com.session.KToken;

public class CmmSessionMngListVO extends KCmmVO {

	private List<KToken> sessionList = null;

	public List<KToken> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<KToken> sessionList) {
		this.sessionList = sessionList;
	}
}
