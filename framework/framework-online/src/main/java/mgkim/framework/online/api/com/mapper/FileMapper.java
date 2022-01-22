package mgkim.framework.online.api.com.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.api.com.vo.FileVO;

@Mapper
public interface FileMapper {

	public int insertFile(FileVO vo) throws Exception;

	public Map<String, Object> selectFile(Map<String, Object> inMap) throws Exception;

	public List<FileVO> selectFilegroup(FileVO vo) throws Exception;

	public int insertFilegroup(FileVO vo) throws Exception;
}
