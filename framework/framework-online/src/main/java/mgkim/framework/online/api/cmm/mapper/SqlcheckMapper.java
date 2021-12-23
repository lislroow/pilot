package mgkim.framework.online.api.cmm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.api.cmm.vo.SqlcheckVO;

@Mapper
public interface SqlcheckMapper {
	
	public Map selectNoParam() throws Exception;
	
	public List<Map> selectForeachList(SqlcheckVO vo) throws Exception;
	
	public List<Map> selectForeachListByMap(Map map) throws Exception;
	
}
