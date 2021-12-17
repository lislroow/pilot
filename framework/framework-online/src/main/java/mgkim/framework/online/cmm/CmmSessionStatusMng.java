package mgkim.framework.online.cmm;

import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;
import mgkim.framework.online.com.annotation.KModule;
import mgkim.framework.online.com.session.KToken;

@KModule(name = "세션 상태 관리", required = true)
public interface CmmSessionStatusMng {

	public int insertNewStatus(KToken token) throws Exception;

	public CmmSessionStatusVO selectStatusForIsLogin(KToken token) throws Exception;

	public void updateDupl(KToken token) throws Exception;

	public int updateRefresh(CmmSessionMngListVO vo) throws Exception;

	public void updateInvalidStatus() throws Exception;

}
