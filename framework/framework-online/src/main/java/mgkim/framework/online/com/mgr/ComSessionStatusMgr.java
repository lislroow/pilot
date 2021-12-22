package mgkim.framework.online.com.mgr;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.type.TSsStcdType;
import mgkim.framework.online.cmm.CmmSessionStatusMng;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;

@KBean(name = "session 상태 관리")
public class ComSessionStatusMgr {

	@Autowired(required = true)
	private CmmSessionStatusMng cmmSessionStatusMng;

	public void insertNewStatus(Map<String, Object> claims) throws Exception {
		// `다중 session` 상태 확인
		{
			this.verifyDuplLogin(claims);
		}

		// `신규 session` 상태 저장
		{
			cmmSessionStatusMng.insertNewStatus(claims);
		}
	}

	public boolean isLoginStatus(Map<String, Object> claims) throws Exception {
		/*
		세션 상태 체크

		세션 상태 체크 filter 에서 대부분의 uri 를 체크하지만,
		filter 에서 처리할 수 없는 곳에서 체크를 해야할 경우도 있음
		*/

		try {
			TSsStcdType ssStcdType = null;
			CmmSessionStatusVO statusVO = cmmSessionStatusMng.selectStatusForIsLogin(claims);
			if (statusVO == null) {
				return false;
			}
			ssStcdType = TSsStcdType.get(statusVO.getSsStcd());
			if (ssStcdType == TSsStcdType.LOGIN) {
				return true;
			}

			if (ssStcdType == TSsStcdType.DUP_LOGIN && "N".equals(statusVO.getDloginAlowYn())) {
				throw new KSysException(KMessage.E6102);
			} else if (ssStcdType == TSsStcdType.EXPIRED
					|| ssStcdType == TSsStcdType.LOGOUT) {
				throw new KSysException(KMessage.E6101);
			} else {
				throw new KSysException(KMessage.E6109, statusVO.getSsStcd());
			}
		} catch(Exception e) {
			throw e;
		}
	}

	public void verifyDuplLogin(Map<String, Object> claims) throws Exception {
		cmmSessionStatusMng.updateDupl(claims);
	}
}
