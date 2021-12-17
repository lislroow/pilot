package mgkim.framework.online.cmm;

import java.util.List;

import mgkim.framework.online.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.framework.online.com.annotation.KModule;

@KModule(name = "api처리로그", required = false)
public interface CmmApiTxLog {

	public void insertLog(List<CmmApiTxLogVO> list) throws Exception;

	public int archive(String currDttm) throws Exception;

}
