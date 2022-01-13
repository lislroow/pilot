package mgkim.service.adm.cmm;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.online.cmm.CmmPrivacy;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyMngVO;
import mgkim.service.adm.cmm.mapper.CmmPrivacyMapper;

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
