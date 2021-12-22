package mgkim.service.www.cmm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.type.TSsStcdType;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmSessionStatusMng;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;
import mgkim.service.www.cmm.mapper.CmmSessionStatusMngMapper;

@Service
public class CmmSessionStatusMngImpl implements CmmSessionStatusMng {

	@Autowired
	private CmmSessionStatusMngMapper cmmSessionStatusMngMapper;


	@Override
	public int insertNewStatus(Map<String, Object> claims) throws Exception {
		/*
		세션 상태 등록에 대한 정책

		- userId, 인증방식, ssid 가 같고, 세션 상태가 00(로그인), 20(세션만료) 상태만 가능함
		  : 20(세션만료) 상태는 세션 이력에 저장되지 않은 상태
		  : ip가 같을 경우 아래 MERGE 문으로 update 실행

		- ssid, userId, 인증방식이 같은 세션 상태가 있을 경우
		  : 기존 세션 상태를 로그인으로 변경하고
		  : 세션 만료 시각, 로그인 시각을 갱신함
		  : 로그인 이력을 남기지 않게됨
		  : 암호화키 필드는 모두 null 로 초기화
		  : 브라우저 정보는 갱신함
		*/
		int result = cmmSessionStatusMngMapper.insertNewStatus(claims);
		return result;
	}

	@Override
	public CmmSessionStatusVO selectStatusForIsLogin(Map<String, Object> claims) throws Exception {
		/*
		userId, 인증방식, ssid 가 같은 세션 상태 조회

		- 세션 상태의 유효성 체크는 해당 개별코드에서 처리
		*/
		CmmSessionStatusVO result = cmmSessionStatusMngMapper.selectStatusForIsLogin(claims);
		return result;
	}

	@Override
	public void updateDupl(Map<String, Object> claims) throws Exception {
		/*
		- userId 가 같은 세션이 있을 경우 다중 세션에 대한 처리 정책

		[allow]
			: ip 가 같을 경우 허용
			: ip 가 다르고, 로그인 상태이고, 인증방식이 다를 경우 허용
		[deny]
			: ip 가 다르고, 로그인 상태이고, 인증방식이 같을 경우 거부
		*/


		List<CmmSessionStatusVO> list = cmmSessionStatusMngMapper.selectStatusListForDupl(claims);
		List<String> duplList = new ArrayList<String>();
		for (int i=0; i<list.size(); i++) {
			CmmSessionStatusVO vo = list.get(i);
			// `신규 session` 과 `기존 session` 의 ip 비교
			String ip = KStringUtil.nvl(vo.getIp());
			if (ip.equals(claims.get("ip"))) { // 같을 경우에는 `다중 session 상태` 허용
				continue;
			}
			// `중복로그인허용여부` 확인
			String dloginAlowYn = KStringUtil.nvl(vo.getDloginAlowYn());
			if ("Y".equals(dloginAlowYn)) { // "Y"일 경우 허용
				continue;
			}
			

			// `기존 session` 의 `세션 상태` 확인
			TSsStcdType ssStcdType = TSsStcdType.get(list.get(i).getSsStcd());

			if (ssStcdType == TSsStcdType.LOGIN) { // LOGIN 인 경우에는 DUPL_LOGIN 으로 변경
				String aumthTpcd = KStringUtil.nvl(list.get(i).getAumthTpcd());
				// `기존 session` 의 `인증 방식` 확인
				if (!aumthTpcd.equals(claims.get("aumthTpcd"))) {
					continue; // 다를 경우 `다중 session 상태` 허용 않음
				}

				duplList.add(list.get(i).getSsid());
			}
		}
		CmmSessionMngListVO vo = new CmmSessionMngListVO();
		vo.setSessionList(duplList);
		cmmSessionStatusMngMapper.updateDupl(vo);
	}

	@Override
	public int updateRefresh(CmmSessionMngListVO vo) throws Exception {
		int cnt = cmmSessionStatusMngMapper.updateRefresh(vo);
		return cnt;
	}

	@Override
	public void updateInvalidStatus() throws Exception {
		int cnt;
		cnt = cmmSessionStatusMngMapper.updateLoginToExpire();
		if (cnt > 0) KLogSys.info("`{}` sessions expired", cnt);
		cnt = cmmSessionStatusMngMapper.updateExpireToLogout();
		if (cnt > 0) KLogSys.info("`{}` sessions logouted", cnt);
		cnt = cmmSessionStatusMngMapper.insertMoveToHistory();
		if (cnt > 0) KLogSys.info("`{}` sessions saved history table", cnt);
		cnt = cmmSessionStatusMngMapper.deleteLogout();
		if (cnt > 0) KLogSys.info("`{}` sessions delete from status table", cnt);
	}
}
