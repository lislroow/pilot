package mgkim.framework.online.api.com.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;
import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.online.api.com.mapper.ApiTxLogMapper;

@Service
public class ApiTxLogService {

	@Autowired
	private ApiTxLogMapper apiTxLogMapper;
	
	public List<CmmApiTxLogVO> selectLogList(KCmmVO vo) throws Exception {
		List<CmmApiTxLogVO> list = apiTxLogMapper.selectLogList(vo);
		return list;
	}
	
	public List<CmmApiTxLogVO> selectLogList(Map<String, String> inMap) throws Exception {
		List<CmmApiTxLogVO> list = apiTxLogMapper.selectLogListByMap(inMap);
		return list;
	}

}
