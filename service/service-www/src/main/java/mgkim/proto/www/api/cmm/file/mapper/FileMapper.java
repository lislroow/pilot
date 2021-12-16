package mgkim.proto.www.api.cmm.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.proto.www.api.cmm.file.vo.FileVO;

@Mapper
public interface FileMapper {

	public int insertFile(FileVO vo) throws Exception;

	public FileVO selectFile(FileVO vo) throws Exception;

	public List<FileVO> selectFilegroup(FileVO vo) throws Exception;

	public int insertFilegroup(FileVO vo) throws Exception;
}
