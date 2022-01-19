package mgkim.service.adm.cmm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.cmm.online.CmmApiTxLog;
import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;
import mgkim.framework.core.annotation.KBean;
import mgkim.service.adm.cmm.mapper.CmmApiTxLogMapper;

@KBean
public class CmmApiTxLogImpl implements CmmApiTxLog {

	@Autowired
	private CmmApiTxLogMapper cmmApiTxLogMapper;

	@Override
	public void insertLog(List<CmmApiTxLogVO> list) throws Exception {
		int cnt = list.size();
		for (int i=0; i<cnt; i++) {
			cmmApiTxLogMapper.insertLog(list.get(i));
		}
	}

	@Override
	public int archive(String archiveDttm) throws Exception {
		int cnt = cmmApiTxLogMapper.insertLogForArchive(archiveDttm);
		cmmApiTxLogMapper.deleteLogForArchive(archiveDttm);
		return cnt;
	}
}
