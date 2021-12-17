package mgkim.framework.online.api.adm.initdata.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.online.api.adm.initdata.mapper.InitDataLoadMapper;
import mgkim.framework.online.api.adm.initdata.vo.InitLoadMockVO;
import mgkim.framework.online.cmm.vo.uri.CmmUriVO;
import mgkim.framework.online.com.mgr.ComUriListMgr;
import mgkim.framework.online.com.util.KDateUtil;

@Service
public class InitDataLoadService {

	@Autowired
	private InitDataLoadMapper initDataLoadMapper;

	@Autowired
	private ComUriListMgr comUriListMgr;

	// ##############
	// 1) 권한
	// ##############
	// 1.1) 권한
	public void loadRole() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteRoleAll(vo);

			// 적재
			List<InitLoadMockVO> list = new ArrayList<InitLoadMockVO>();
			list.add(new InitLoadMockVO.Builder().roleId("R100").build());
			list.add(new InitLoadMockVO.Builder().roleId("R110").build());
			list.add(new InitLoadMockVO.Builder().roleId("R200").build());
			int cnt = list.size();
			for(int i=0; i<cnt; i++) {
				InitLoadMockVO item = list.get(i);
				initDataLoadMapper.insertRole(item);
			}

		}
	}
	// 1.2) 권한그룹
	public void loadRgrp() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteRgrpAll(vo);

			// 적재
			List<InitLoadMockVO> list = new ArrayList<InitLoadMockVO>();
			list.add(new InitLoadMockVO.Builder().rgrpId("RG100").build());
			list.add(new InitLoadMockVO.Builder().rgrpId("RG110").build());
			list.add(new InitLoadMockVO.Builder().rgrpId("RG200").build());
			int cnt = list.size();
			for(int i=0; i<cnt; i++) {
				InitLoadMockVO item = list.get(i);
				initDataLoadMapper.insertRgrp(item);
			}
		}
	}
	// 1.3) 권한그룹설정
	public void loadRoleRgrp() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteRoleRpgrAll(vo);

			// 적재
			List<InitLoadMockVO> list = new ArrayList<InitLoadMockVO>();
			list.add(new InitLoadMockVO.Builder().roleId("R100").rgrpId("RG100").build());
			list.add(new InitLoadMockVO.Builder().roleId("R100").rgrpId("RG110").build());
			list.add(new InitLoadMockVO.Builder().roleId("R110").rgrpId("RG110").build());
			list.add(new InitLoadMockVO.Builder().roleId("R200").rgrpId("RG200").build());
			int cnt = list.size();
			for(int i=0; i<cnt; i++) {
				InitLoadMockVO item = list.get(i);
				initDataLoadMapper.insertRoleRpgr(item);
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
			initDataLoadMapper.deleteUriRawAll(new CmmUriVO.Builder().build());

			// 적재
			int cnt = list.size();
			for(int i=0; i<cnt; i++) {
				CmmUriVO item = list.get(i);
				initDataLoadMapper.insertUriRaw(item);
			}
		}
	}
	// 2.2) uri
	public void loadUri() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteUriAll(vo);

			// 적재
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.insertUri(vo);
		}
	}
	// 2.3) uri권한
	public void loadUriRgrp() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteUriRpgrAll(vo);

			// 적재
			vo = new InitLoadMockVO.Builder().rgrpId("RG100").build();
			initDataLoadMapper.insertUriRpgr(vo);
		}
	}

	// ##############
	// 3) 사용자
	// ##############
	// 3.1) 사용자(raw)
	public void loadUserRaw() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteUserRawAll(vo);

			// 적재
			List<InitLoadMockVO> list = new ArrayList<InitLoadMockVO>();
			list.add(new InitLoadMockVO.Builder().userId("1000000001").userNm("나사원").email("nasa@daum.net").aumthTpcdList("01,90").inactvYn("N").lockYn("N").loginFailCnt(0).pwchgDt(KDateUtil.today()).ssvaldSec(86400).build());
			list.add(new InitLoadMockVO.Builder().userId("1000000002").userNm("오팀장").email("vteam@daum.net").aumthTpcdList("01,90").inactvYn("Y").lockYn("N").loginFailCnt(4).pwchgDt("20210101").ssvaldSec(60).build());
			list.add(new InitLoadMockVO.Builder().userId("2000000001").userNm("김고객").email("kims@daum.net").aumthTpcdList("01,90").inactvYn("N").lockYn("N").loginFailCnt(0).pwchgDt(KDateUtil.today()).ssvaldSec(600).build());
			int cnt = list.size();
			for(int i=0; i<cnt; i++) {
				InitLoadMockVO item = list.get(i);
				initDataLoadMapper.insertUserRaw(item);
			}

		}
	}
	// 3.2) 사용자
	public void loadUser() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteUserAll(vo);

			// 적재
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.insertUser(vo);

			// 적재
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.insertLoginPolicy(vo);
		}
	}
	// 3.3) 사용자권한
	public void loadUserRpgr() throws Exception {
		{
			InitLoadMockVO vo;

			// 기존 데이터 삭제
			vo = new InitLoadMockVO.Builder().build();
			initDataLoadMapper.deleteUserRpgrAll(vo);

			// 적재
			List<InitLoadMockVO> list = new ArrayList<InitLoadMockVO>();
			list.add(new InitLoadMockVO.Builder().userId("1000000001").rgrpId("RG100").build());
			list.add(new InitLoadMockVO.Builder().userId("1000000002").rgrpId("RG110").build());
			list.add(new InitLoadMockVO.Builder().userId("2000000001").rgrpId("RG200").build());
			int cnt = list.size();
			for(int i=0; i<cnt; i++) {
				InitLoadMockVO item = list.get(i);
				initDataLoadMapper.insertUserRpgr(item);
			}
		}
	}
}
