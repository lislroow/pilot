package mgkim.service.www.cmm.mapper;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;

@Mapper
public interface CmmApiTxLogMapper {

	public void insertLog(CmmApiTxLogVO vo) throws Exception;

	public int insertLogForArchive(String archiveDttm) throws Exception;

	public int deleteLogForArchive(String archiveDttm) throws Exception;

}
