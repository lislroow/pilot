package mgkim.framework.online.api.com.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.api.com.vo.SqlcheckVO;

@Mapper
public interface SqlcheckMapper {
	
	public Map selectNoParam() throws Exception;
	
	public List<Map> selectListByCountSql2(SqlcheckVO vo) throws Exception;
	
	public List<Map> selectForeachListByVO(SqlcheckVO vo) throws Exception;
	
	public List<Map> selectForeachListByMap(Map map) throws Exception;
	
}
