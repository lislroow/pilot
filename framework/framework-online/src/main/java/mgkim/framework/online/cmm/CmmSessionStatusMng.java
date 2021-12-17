package mgkim.framework.online.cmm;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.core.session.KToken;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;

@KModule(name = "세션 상태 관리", required = true)
public interface CmmSessionStatusMng {

	public int insertNewStatus(KToken token) throws Exception;

	public CmmSessionStatusVO selectStatusForIsLogin(KToken token) throws Exception;

	public void updateDupl(KToken token) throws Exception;

	public int updateRefresh(CmmSessionMngListVO vo) throws Exception;

	public void updateInvalidStatus() throws Exception;

}
