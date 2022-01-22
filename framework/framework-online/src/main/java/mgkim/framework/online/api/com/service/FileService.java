package mgkim.framework.online.api.com.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.online.api.com.mapper.FileMapper;
import mgkim.framework.online.api.com.vo.FileVO;

@Service
public class FileService {

	@Autowired
	private FileMapper fileMapper;

	public int insertFile(FileVO vo) throws Exception {
		int cnt = fileMapper.insertFile(vo);
		return cnt;
	}

	public Map<String, Object> selectFile(Map<String, Object> inMap) throws Exception {
		return fileMapper.selectFile(inMap);
	}

	public List<FileVO> selectFilegroup(FileVO vo) throws Exception {
		return fileMapper.selectFilegroup(vo);
	}

	public int insertFilegroup(List<FileVO> list) throws Exception {
		int cnt = 0;
		for (FileVO vo : list) {
			cnt += fileMapper.insertFilegroup(vo);
		}
		return cnt;
	}

}
