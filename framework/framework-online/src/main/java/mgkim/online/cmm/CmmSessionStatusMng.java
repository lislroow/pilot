package mgkim.online.cmm;

import mgkim.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;
import mgkim.online.com.annotation.KModule;
import mgkim.online.com.session.KToken;

@KModule(name = "세션 상태 관리", required = true)
public interface CmmSessionStatusMng {

	public int insertNewStatus(KToken token) throws Exception;

	public CmmSessionStatusVO selectStatusForIsLogin(KToken token) throws Exception;

	public void updateDupl(KToken token) throws Exception;

	public int updateRefresh(CmmSessionMngListVO vo) throws Exception;

	public void updateInvalidStatus() throws Exception;

}
