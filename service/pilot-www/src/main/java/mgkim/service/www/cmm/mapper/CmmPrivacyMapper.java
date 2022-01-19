package mgkim.service.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.cmm.online.vo.CmmPrivacyLogVO;
import mgkim.framework.cmm.online.vo.CmmPrivacyMngVO;

@Mapper
public interface CmmPrivacyMapper {

	public List<CmmPrivacyMngVO> selectMngAll(CmmPrivacyMngVO vo) throws Exception;

	public int insertLog(CmmPrivacyLogVO vo) throws Exception;
}

