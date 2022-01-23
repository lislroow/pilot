package mgkim.framework.online.api.com.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.online.api.com.mapper.ApiTxLogMapper;

@Service
public class ApiTxLogService {
	
	private static final Logger log = LoggerFactory.getLogger(ApiTxLogService.class);

	@Autowired
	private ApiTxLogMapper apiTxLogMapper;
	
	public int insertLog_map(List<Map<String, Object>> list) throws Exception {
		int icnt = 0;
		for (Map<String, Object> map : list) {
			icnt += apiTxLogMapper.insertLog_map(map);
		}
		if (icnt != list.size()) {
			log.warn("등록할 건수({})와 등록된 건수가 다릅니다.", list.size(), icnt);
		}
		return icnt;
	}
	
	public List<Map<String, Object>> selectLogList_map(Map<String, Object> inMap) throws Exception {
		List<Map<String, Object>> list = apiTxLogMapper.selectLogList_map(inMap);
		return list;
	}
	
	public Map<String, Object> selectLogByTxid_map(Map<String, Object> inMap) throws Exception {
		Map<String, Object> map = apiTxLogMapper.selectLogByTxid_map(inMap);
		return map;
	}
	
	public int archive_map(Map<String, Object> inMap) throws Exception {
		int icnt = apiTxLogMapper.insertLogForArchive_map(inMap);
		int dcnt = apiTxLogMapper.deleteLogForArchive_map(inMap);
		if (icnt != dcnt) {
			log.warn("삭제 건수({})와 등록 건수({})가 다릅니다.", icnt, dcnt);
		} else {
			log.info(KMessage.get(KMessage.E1001, icnt));
		}
		return icnt;
	}
}
