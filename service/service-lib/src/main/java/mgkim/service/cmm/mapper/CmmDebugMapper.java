package mgkim.service.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.cmm.online.vo.CmmDebugVO;

@Mapper
public interface CmmDebugMapper {

	public int updateStartDebug(CmmDebugVO vo) throws Exception;

	public int updateStopDebug(CmmDebugVO vo) throws Exception;

	public CmmDebugVO selectDebuggingSession(CmmDebugVO vo) throws Exception;

	public List<CmmDebugVO> selectDebuggingList() throws Exception;

}

