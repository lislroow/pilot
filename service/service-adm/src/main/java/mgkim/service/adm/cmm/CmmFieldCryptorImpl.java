package mgkim.service.adm.cmm;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.online.cmm.CmmFieldCryptor;
import mgkim.framework.online.cmm.vo.fieldcryptor.CmmFieldCryptoVO;
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
