package mgkim.framework.cmm.online;

import java.util.List;

import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;
import mgkim.framework.core.annotation.KModule;

@KModule(name = "api처리로그", required = false)
public interface CmmApiTxLog {

	public void insertLog(List<CmmApiTxLogVO> list) throws Exception;

	public int archive(String currDttm) throws Exception;

}
