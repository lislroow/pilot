package mgkim.proto.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.online.cmm.vo.debug.CmmDebugVO;

@Mapper
public interface CmmDebugMapper {

	public int updateStartDebug(CmmDebugVO vo) throws Exception;

	public int updateStopDebug(CmmDebugVO vo) throws Exception;

	public CmmDebugVO selectDebuggingSession(CmmDebugVO vo) throws Exception;

	public List<CmmDebugVO> selectDebuggingList() throws Exception;

}

