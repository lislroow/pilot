package mgkim.framework.online.v1;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiTxLogMapper {
	
	public int insertLog_map(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectLogList_map(Map<String, Object> inMap) throws Exception;
	
	public Map<String, Object> selectLogByTxid_map(Map<String, Object> inMap) throws Exception;
	
	public int insertLogForArchive_map(Map<String, Object> inMap) throws Exception;
	
	public int deleteLogForArchive_map(Map<String, Object> inMap) throws Exception;
}
