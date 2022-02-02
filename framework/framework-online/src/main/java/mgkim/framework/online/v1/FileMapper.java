package mgkim.framework.online.v1;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {

	public int insertFile(Map<String, Object> map) throws Exception;

	public Map<String, Object> selectFile(Map<String, Object> inMap) throws Exception;

	public List<Map<String, Object>> selectFilegroup(Map<String, Object> map) throws Exception;

	public int insertFilegroup(Map<String, Object> map) throws Exception;
}
