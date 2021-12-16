package mgkim.proto.www.cmm;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.core.cmm.CmmPrivacy;
import mgkim.core.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.core.cmm.vo.privacy.CmmPrivacyMngVO;
import mgkim.proto.www.cmm.mapper.CmmPrivacyMapper;

@Service
public class CmmPrivacyImpl implements CmmPrivacy {

	@Autowired
	private CmmPrivacyMapper cmmPrivacyMapper;

	@Override
	public void init() throws BeansException {

	}

	@Override
	public List<CmmPrivacyMngVO> selectMngAll(CmmPrivacyMngVO vo) throws Exception {
		return cmmPrivacyMapper.selectMngAll(vo);
	}

	@Override
	public void insertLog(CmmPrivacyLogVO vo) throws Exception {
		cmmPrivacyMapper.insertLog(vo);
	}

}
