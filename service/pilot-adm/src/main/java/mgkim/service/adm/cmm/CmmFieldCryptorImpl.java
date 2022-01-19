package mgkim.service.adm.cmm;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.cmm.online.CmmFieldCryptor;
import mgkim.framework.cmm.online.vo.CmmFieldCryptoVO;
import mgkim.framework.core.annotation.KBean;
import mgkim.service.adm.cmm.mapper.CmmFieldCryptorMapper;

@KBean
public class CmmFieldCryptorImpl implements CmmFieldCryptor {

	@Autowired
	private CmmFieldCryptorMapper cmmFieldCryptorMapper;

	@Override
	public void saveRsaKey(CmmFieldCryptoVO vo) throws Exception {
		cmmFieldCryptorMapper.saveRsaKey(vo);
	}

	@Override
	public void saveSymKey(CmmFieldCryptoVO vo) throws Exception {
		cmmFieldCryptorMapper.saveSymKey(vo);
	}

	@Override
	public CmmFieldCryptoVO selectFieldCryptoKey(Map<String, Object> claims) throws Exception {
		CmmFieldCryptoVO result = null;
		result = cmmFieldCryptorMapper.selectFieldCryptoKey(claims);
		return result;
	}

}
