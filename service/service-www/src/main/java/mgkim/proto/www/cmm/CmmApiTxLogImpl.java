package mgkim.proto.www.cmm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.online.cmm.CmmApiTxLog;
import mgkim.framework.online.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.proto.www.cmm.mapper.CmmApiTxLogMapper;

@KBean
public class CmmApiTxLogImpl implements CmmApiTxLog {

	@Autowired
	private CmmApiTxLogMapper cmmApiTxLogMapper;

	@Override
	public void insertLog(List<CmmApiTxLogVO> list) throws Exception {
		int cnt = list.size();
		for(int i=0; i<cnt; i++) {
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
