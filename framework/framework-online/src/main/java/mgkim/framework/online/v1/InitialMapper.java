package mgkim.framework.online.v1;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.cmm.online.vo.CmmUriVO;

@Mapper
public interface InitialMapper {
	// ##############
	// 1) 권한
	// ##############

	// 1.1) 권한
	public int deleteRoleAll(InitialVO vo) throws Exception;
	public int insertRole(InitialVO vo) throws Exception;

	// 1.2) 권한그룹
	public int deleteRgrpAll(InitialVO vo) throws Exception;
	public int insertRgrp(InitialVO vo) throws Exception;

	// 1.3) 권한그룹설정
	public int deleteRoleRpgrAll(InitialVO vo) throws Exception;
	public int insertRoleRpgr(InitialVO vo) throws Exception;


	// ##############
	// 2) 자원
	// ##############
	// 2.1) uri(raw)
	public int deleteUriRawAll(CmmUriVO vo) throws Exception;
	public int insertUriRaw(CmmUriVO vo) throws Exception;

	// 2.2) uri
	public int deleteUriAll(InitialVO vo) throws Exception;
	public int insertUri(InitialVO vo) throws Exception;

	// 2.3) uri권한
	public int deleteUriRpgrAll(InitialVO vo) throws Exception;
	public int insertUriRpgr(InitialVO vo) throws Exception;



	// ##############
	// 3) 사용자
	// ##############
	// 3.1) 사용자(raw)
	public int deleteUserRawAll(InitialVO vo) throws Exception;
	public int insertUserRaw(InitialVO vo) throws Exception;

	// 3.2) 사용자
	public int deleteUserAll(InitialVO vo) throws Exception;
	public int insertUser(InitialVO vo) throws Exception;
	public int insertLoginPolicy(InitialVO vo) throws Exception;

	// 3.3) 사용자권한
	public int deleteUserRpgrAll(InitialVO vo) throws Exception;
	public int insertUserRpgr(InitialVO vo) throws Exception;
}
