package mgkim.service.www.api.cmm.file.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.service.www.api.cmm.file.mapper.FileMapper;
import mgkim.service.www.api.cmm.file.vo.FileVO;

@Service
public class FileService {

	@Autowired
	private FileMapper fileMapper;

	public int insertFile(FileVO vo) throws Exception {
		int cnt = fileMapper.insertFile(vo);
		return cnt;
	}

	public FileVO selectFile(FileVO vo) throws Exception {
		return fileMapper.selectFile(vo);
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
