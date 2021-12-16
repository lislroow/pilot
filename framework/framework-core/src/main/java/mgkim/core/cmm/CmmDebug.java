package mgkim.core.cmm;

import java.util.List;

import mgkim.core.cmm.vo.debug.CmmDebugVO;
import mgkim.core.com.annotation.KModule;

@KModule(name = "사용자 디버깅 관리", required = false)
public interface CmmDebug {

	public CmmDebugVO startDebug(CmmDebugVO vo) throws Exception;

	public int stopDebug(CmmDebugVO vo) throws Exception;

	public List<CmmDebugVO> selectDebuggingList() throws Exception;

}
