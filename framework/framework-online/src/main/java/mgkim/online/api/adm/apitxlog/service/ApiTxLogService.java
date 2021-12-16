package mgkim.online.api.adm.apitxlog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.core.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.core.com.dto.KCmmVO;
import mgkim.online.api.adm.apitxlog.mapper.ApiTxLogMapper;

@Service
public class ApiTxLogService {

	@Autowired
	private ApiTxLogMapper apiTxLogMapper;

	public List<CmmApiTxLogVO> selectLogList(KCmmVO vo) throws Exception {
		List<CmmApiTxLogVO> list = apiTxLogMapper.selectLogList(vo);
		return list;
	}

}
