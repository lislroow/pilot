package mgkim.framework.cmm.online;

import java.util.List;

import mgkim.framework.cmm.online.vo.CmmDebugVO;
import mgkim.framework.core.annotation.KModule;

@KModule(name = "사용자 디버깅 관리", required = false)
public interface CmmDebug {

	public CmmDebugVO startDebug(CmmDebugVO vo) throws Exception;

	public int stopDebug(CmmDebugVO vo) throws Exception;

	public List<CmmDebugVO> selectDebuggingList() throws Exception;

}
