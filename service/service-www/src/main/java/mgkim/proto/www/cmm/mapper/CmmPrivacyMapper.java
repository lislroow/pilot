package mgkim.proto.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyMngVO;

@Mapper
public interface CmmPrivacyMapper {

	public List<CmmPrivacyMngVO> selectMngAll(CmmPrivacyMngVO vo) throws Exception;

	public int insertLog(CmmPrivacyLogVO vo) throws Exception;
}

