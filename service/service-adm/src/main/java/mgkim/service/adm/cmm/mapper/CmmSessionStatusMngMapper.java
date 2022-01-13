package mgkim.service.adm.cmm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;

@Mapper
public interface CmmSessionStatusMngMapper {

	public int insertNewStatus(Map<String, Object> map) throws Exception;

	public CmmSessionStatusVO selectStatusForIsLogin(Map<String, Object> map) throws Exception;

	public List<CmmSessionStatusVO> selectStatusListForDupl(Map<String, Object> map) throws Exception;

	public int updateDupl(CmmSessionMngListVO vo) throws Exception;

	public int updateRefresh(CmmSessionMngListVO vo) throws Exception;

	public int updateLoginToExpire() throws Exception;

	public int updateExpireToLogout() throws Exception;

	public int insertMoveToHistory() throws Exception;

	public int deleteLogout() throws Exception;

}
