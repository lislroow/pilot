package mgkim.framework.online.api.com.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;
import mgkim.framework.core.dto.KCmmVO;

@Mapper
public interface ApiTxLogMapper {
	
	public List<CmmApiTxLogVO> selectLogList(KCmmVO vo) throws Exception;
	
	public List<CmmApiTxLogVO> selectLogListByMap(Map<String, String> inMap) throws Exception;
	
}
