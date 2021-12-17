package mgkim.proto.www.cmm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.online.cmm.CmmSessionStatusMng;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;
import mgkim.framework.online.com.logging.KLogSys;
import mgkim.framework.online.com.session.KToken;
import mgkim.framework.online.com.type.TSsStcdType;
import mgkim.framework.online.com.util.KStringUtil;
import mgkim.proto.www.cmm.mapper.CmmSessionStatusMngMapper;

@Service
public class CmmSessionStatusMngImpl implements CmmSessionStatusMng {

	@Autowired
	private CmmSessionStatusMngMapper cmmSessionStatusMngMapper;


	@Override
	public int insertNewStatus(KToken token) throws Exception {
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
		int result = cmmSessionStatusMngMapper.insertNewStatus(token);
		return result;
	}

	@Override
	public CmmSessionStatusVO selectStatusForIsLogin(KToken token) throws Exception {
		/*
		userId, 인증방식, ssid 가 같은 세션 상태 조회

		- 세션 상태의 유효성 체크는 해당 개별코드에서 처리
		*/
		CmmSessionStatusVO result = cmmSessionStatusMngMapper.selectStatusForIsLogin(token);
		return result;
	}

	@Override
	public void updateDupl(KToken token) throws Exception {
		/*
		- userId 가 같은 세션이 있을 경우 다중 세션에 대한 처리 정책

		[allow]
			: ip 가 같을 경우 허용
			: ip 가 다르고, 로그인 상태이고, 인증방식이 다를 경우 허용
		[deny]
			: ip 가 다르고, 로그인 상태이고, 인증방식이 같을 경우 거부
		*/


		List<CmmSessionStatusVO> list = cmmSessionStatusMngMapper.selectStatusListForDupl(token);
		List<KToken> duplList = new ArrayList<KToken>();
		for(int i=0; i<list.size(); i++) {
			// `신규 session` 과 `기존 session` 의 ip 비교
			String ip = KStringUtil.nvl(list.get(i).getIp());
			if(ip.equals(token.getIp())) { // 같을 경우에는 `다중 session 상태` 허용
				continue;
			}

			// `기존 session` 의 `세션 상태` 확인
			TSsStcdType ssStcdType = TSsStcdType.get(list.get(i).getSsStcd());

			if(ssStcdType == TSsStcdType.LOGIN) { // LOGIN 인 경우에는 DUPL_LOGIN 으로 변경
				String aumthTpcd = KStringUtil.nvl(list.get(i).getAumthTpcd());
				// `기존 session` 의 `인증 방식` 확인
				if(!aumthTpcd.equals(token.getAumthTpcd())) {
					continue; // 다를 경우 `다중 session 상태` 허용 않음
				}

				duplList.add(token);
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
		KLogSys.info("`{}` sessions expired", cnt);
		cnt = cmmSessionStatusMngMapper.updateExpireToLogout();
		KLogSys.info("`{}` sessions logouted", cnt);
		cnt = cmmSessionStatusMngMapper.insertMoveToHistory();
		KLogSys.info("`{}` sessions saved history table", cnt);
		cnt = cmmSessionStatusMngMapper.deleteLogout();
		KLogSys.info("`{}` sessions delete from status table", cnt);
	}
}
