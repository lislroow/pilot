package mgkim.framework.cmm.online;

import java.util.Map;

import mgkim.framework.cmm.online.vo.CmmSessionMngListVO;
import mgkim.framework.cmm.online.vo.CmmSessionStatusVO;
import mgkim.framework.core.annotation.KModule;

@KModule(name = "세션 상태 관리", required = true)
public interface CmmSessionStatusMng {

	public int insertNewStatus(Map<String, Object> claims) throws Exception;

	public CmmSessionStatusVO selectStatusForIsLogin(Map<String, Object> claims) throws Exception;

	public void updateDupl(Map<String, Object> claims) throws Exception;

	public int updateRefresh(CmmSessionMngListVO vo) throws Exception;

	public void updateInvalidStatus() throws Exception;

}
