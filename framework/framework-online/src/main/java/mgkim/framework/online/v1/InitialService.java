package mgkim.framework.online.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.cmm.online.vo.CmmUriVO;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.online.com.mgr.ComUriListMgr;

@Service
public class InitialService {

	@Autowired
	private InitialMapper initialMapper;

	@Autowired
	private ComUriListMgr comUriListMgr;

	// ##############
	// 1) 권한
	// ##############
	// 1.1) 권한
	public void loadRole() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteRoleAll(vo);

			// 적재
			List<InitialVO> list = new ArrayList<InitialVO>();
			list.add(new InitialVO.Builder().roleId("R100").build());
			list.add(new InitialVO.Builder().roleId("R110").build());
			list.add(new InitialVO.Builder().roleId("R200").build());
			int cnt = list.size();
			for (int i=0; i<cnt; i++) {
				InitialVO item = list.get(i);
				initialMapper.insertRole(item);
			}

		}
	}
	// 1.2) 권한그룹
	public void loadRgrp() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteRgrpAll(vo);

			// 적재
			List<InitialVO> list = new ArrayList<InitialVO>();
			list.add(new InitialVO.Builder().rgrpId("RG100").build());
			list.add(new InitialVO.Builder().rgrpId("RG110").build());
			list.add(new InitialVO.Builder().rgrpId("RG200").build());
			int cnt = list.size();
			for (int i=0; i<cnt; i++) {
				InitialVO item = list.get(i);
				initialMapper.insertRgrp(item);
			}
		}
	}
	// 1.3) 권한그룹설정
	public void loadRoleRgrp() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteRoleRpgrAll(vo);

			// 적재
			List<InitialVO> list = new ArrayList<InitialVO>();
			list.add(new InitialVO.Builder().roleId("R100").rgrpId("RG100").build());
			list.add(new InitialVO.Builder().roleId("R100").rgrpId("RG110").build());
			list.add(new InitialVO.Builder().roleId("R110").rgrpId("RG110").build());
			list.add(new InitialVO.Builder().roleId("R200").rgrpId("RG200").build());
			int cnt = list.size();
			for (int i=0; i<cnt; i++) {
				InitialVO item = list.get(i);
				initialMapper.insertRoleRpgr(item);
			}
		}
	}

	// ##############
	// 2) 자원
	// ##############
	// 2.1) uri(raw)
	public void loadUriRaw() throws Exception {
		{
			List<CmmUriVO> list = new ArrayList<CmmUriVO>();
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("N").uriVal("/api/cmm/user/logout").uriNm("로그아웃").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/openapi/**").uriNm("openapi 전체").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/orgapi/**").uriNm("orgapi 전체").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/interapi/**").uriNm("interapi 전체").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/public/**").uriNm("public 전체").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/api/cmm/file/**").uriNm("파일 uri").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/api/**").uriNm("api 전체").build());
			list.add(new CmmUriVO.Builder().uriRespTpcd("01").uriPtrnYn("Y").uriVal("/**").uriNm("모든 uri 전체").build());
			list.addAll(comUriListMgr.getUriList());

			// 기존 데이터 삭제
			initialMapper.deleteUriRawAll(new CmmUriVO.Builder().build());

			// 적재
			int cnt = list.size();
			for (int i=0; i<cnt; i++) {
				CmmUriVO item = list.get(i);
				initialMapper.insertUriRaw(item);
			}
		}
	}
	// 2.2) uri
	public void loadUri() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteUriAll(vo);

			// 적재
			vo = new InitialVO.Builder().build();
			initialMapper.insertUri(vo);
		}
	}
	// 2.3) uri권한
	public void loadUriRgrp() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteUriRpgrAll(vo);

			// 적재
			vo = new InitialVO.Builder().rgrpId("RG100").build();
			initialMapper.insertUriRpgr(vo);
		}
	}

	// ##############
	// 3) 사용자
	// ##############
	// 3.1) 사용자(raw)
	public void loadUserRaw() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteUserRawAll(vo);

			// 적재
			List<InitialVO> list = new ArrayList<InitialVO>();
			list.add(new InitialVO.Builder().userId("1000000001").userNm("나사원").email("nasa@daum.net").aumthTpcdList("01,90").dloginAlowYn("Y").inactvYn("N").lockYn("N").loginFailCnt(0).pwchgDt(KDateUtil.today()).ssvaldSec(10000000).build());
			list.add(new InitialVO.Builder().userId("1000000002").userNm("오팀장").email("vteam@daum.net").aumthTpcdList("01,90").dloginAlowYn("N").inactvYn("Y").lockYn("N").loginFailCnt(4).pwchgDt("20210101").ssvaldSec(60).build());
			list.add(new InitialVO.Builder().userId("2000000001").userNm("김고객").email("kims@daum.net").aumthTpcdList("01,90").dloginAlowYn("N").inactvYn("N").lockYn("N").loginFailCnt(0).pwchgDt(KDateUtil.today()).ssvaldSec(600).build());
			int cnt = list.size();
			for (int i=0; i<cnt; i++) {
				InitialVO item = list.get(i);
				initialMapper.insertUserRaw(item);
			}

		}
	}
	// 3.2) 사용자
	public void loadUser() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteUserAll(vo);

			// 적재
			vo = new InitialVO.Builder().build();
			initialMapper.insertUser(vo);

			// 적재
			vo = new InitialVO.Builder().build();
			initialMapper.insertLoginPolicy(vo);
		}
	}
	// 3.3) 사용자권한
	public void loadUserRpgr() throws Exception {
		{
			InitialVO vo;

			// 기존 데이터 삭제
			vo = new InitialVO.Builder().build();
			initialMapper.deleteUserRpgrAll(vo);

			// 적재
			List<InitialVO> list = new ArrayList<InitialVO>();
			list.add(new InitialVO.Builder().userId("1000000001").rgrpId("RG100").build());
			list.add(new InitialVO.Builder().userId("1000000002").rgrpId("RG110").build());
			list.add(new InitialVO.Builder().userId("2000000001").rgrpId("RG200").build());
			int cnt = list.size();
			for (int i=0; i<cnt; i++) {
				InitialVO item = list.get(i);
				initialMapper.insertUserRpgr(item);
			}
		}
	}
}
