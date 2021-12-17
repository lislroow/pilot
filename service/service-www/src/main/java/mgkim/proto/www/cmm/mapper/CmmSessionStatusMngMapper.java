package mgkim.proto.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.online.cmm.vo.sessionexpmng.CmmSessionStatusVO;
import mgkim.online.com.session.KToken;

@Mapper
public interface CmmSessionStatusMngMapper {

	public int insertNewStatus(KToken token) throws Exception;

	public CmmSessionStatusVO selectStatusForIsLogin(KToken token) throws Exception;

	public List<CmmSessionStatusVO> selectStatusListForDupl(KToken token) throws Exception;

	public int updateDupl(CmmSessionMngListVO vo) throws Exception;

	public int updateRefresh(CmmSessionMngListVO vo) throws Exception;

	public int updateLoginToExpire() throws Exception;

	public int updateExpireToLogout() throws Exception;

	public int insertMoveToHistory() throws Exception;

	public int deleteLogout() throws Exception;

}
