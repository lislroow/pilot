package mgkim.core.cmm;

import java.util.List;

import mgkim.core.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.core.com.annotation.KModule;

@KModule(name = "api처리로그", required = false)
public interface CmmApiTxLog {

	public void insertLog(List<CmmApiTxLogVO> list) throws Exception;

	public int archive(String currDttm) throws Exception;

}
