package mgkim.framework.online.cmm.vo.sessionexpmng;

import java.util.List;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.session.KToken;

public class CmmSessionMngListVO extends KCmmVO {

	private List<KToken> sessionList = null;

	public List<KToken> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<KToken> sessionList) {
		this.sessionList = sessionList;
	}
}
