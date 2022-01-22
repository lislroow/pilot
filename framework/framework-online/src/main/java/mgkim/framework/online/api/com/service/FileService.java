package mgkim.framework.online.api.com.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.online.api.com.mapper.FileMapper;

@Service
public class FileService {

	@Autowired
	private FileMapper fileMapper;

	public int insertFile(Map<String, Object> map) throws Exception {
		int cnt = fileMapper.insertFile(map);
		return cnt;
	}

	public Map<String, Object> selectFile(Map<String, Object> inMap) throws Exception {
		return fileMapper.selectFile(inMap);
	}

	public List<Map<String, Object>> selectFilegroup(Map<String, Object> map) throws Exception {
		return fileMapper.selectFilegroup(map);
	}

	public int insertFilegroup(List<Map<String, Object>> list) throws Exception {
		int cnt = 0;
		for (Map<String, Object> map : list) {
			cnt += fileMapper.insertFilegroup(map);
		}
		return cnt;
	}

}
