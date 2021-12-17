package mgkim.online.cmm;

import java.util.List;

import mgkim.online.cmm.vo.debug.CmmDebugVO;
import mgkim.online.com.annotation.KModule;

@KModule(name = "사용자 디버깅 관리", required = false)
public interface CmmDebug {

	public CmmDebugVO startDebug(CmmDebugVO vo) throws Exception;

	public int stopDebug(CmmDebugVO vo) throws Exception;

	public List<CmmDebugVO> selectDebuggingList() throws Exception;

}
