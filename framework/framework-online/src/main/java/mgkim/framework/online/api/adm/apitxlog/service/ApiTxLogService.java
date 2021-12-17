package mgkim.framework.online.api.adm.apitxlog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.online.api.adm.apitxlog.mapper.ApiTxLogMapper;
import mgkim.framework.online.cmm.vo.apitxlog.CmmApiTxLogVO;

@Service
public class ApiTxLogService {

	@Autowired
	private ApiTxLogMapper apiTxLogMapper;

	public List<CmmApiTxLogVO> selectLogList(KCmmVO vo) throws Exception {
		List<CmmApiTxLogVO> list = apiTxLogMapper.selectLogList(vo);
		return list;
	}

}
