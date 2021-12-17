package mgkim.online.cmm;

import java.util.List;

import mgkim.online.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.online.com.annotation.KModule;

@KModule(name = "api처리로그", required = false)
public interface CmmApiTxLog {

	public void insertLog(List<CmmApiTxLogVO> list) throws Exception;

	public int archive(String currDttm) throws Exception;

}
