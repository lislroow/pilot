package mgkim.framework.online.api.adm.initdata.mapper;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.api.adm.initdata.vo.InitLoadMockVO;
import mgkim.framework.online.cmm.vo.uri.CmmUriVO;

@Mapper
public interface InitDataLoadMapper {
	// ##############
	// 1) 권한
	// ##############

	// 1.1) 권한
	public int deleteRoleAll(InitLoadMockVO vo) throws Exception;
	public int insertRole(InitLoadMockVO vo) throws Exception;

	// 1.2) 권한그룹
	public int deleteRgrpAll(InitLoadMockVO vo) throws Exception;
	public int insertRgrp(InitLoadMockVO vo) throws Exception;

	// 1.3) 권한그룹설정
	public int deleteRoleRpgrAll(InitLoadMockVO vo) throws Exception;
	public int insertRoleRpgr(InitLoadMockVO vo) throws Exception;


	// ##############
	// 2) 자원
	// ##############
	// 2.1) uri(raw)
	public int deleteUriRawAll(CmmUriVO vo) throws Exception;
	public int insertUriRaw(CmmUriVO vo) throws Exception;

	// 2.2) uri
	public int deleteUriAll(InitLoadMockVO vo) throws Exception;
	public int insertUri(InitLoadMockVO vo) throws Exception;

	// 2.3) uri권한
	public int deleteUriRpgrAll(InitLoadMockVO vo) throws Exception;
	public int insertUriRpgr(InitLoadMockVO vo) throws Exception;



	// ##############
	// 3) 사용자
	// ##############
	// 3.1) 사용자(raw)
	public int deleteUserRawAll(InitLoadMockVO vo) throws Exception;
	public int insertUserRaw(InitLoadMockVO vo) throws Exception;

	// 3.2) 사용자
	public int deleteUserAll(InitLoadMockVO vo) throws Exception;
	public int insertUser(InitLoadMockVO vo) throws Exception;
	public int insertLoginPolicy(InitLoadMockVO vo) throws Exception;

	// 3.3) 사용자권한
	public int deleteUserRpgrAll(InitLoadMockVO vo) throws Exception;
	public int insertUserRpgr(InitLoadMockVO vo) throws Exception;
}
