package mgkim.proto.www.cmm;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.core.cmm.CmmFieldCryptor;
import mgkim.core.cmm.vo.fieldcryptor.CmmFieldCryptoVO;
import mgkim.core.com.annotation.KBean;
import mgkim.core.com.session.KToken;
import mgkim.proto.www.cmm.mapper.CmmFieldCryptorMapper;

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
	public CmmFieldCryptoVO selectFieldCryptoKey(KToken token) throws Exception {
		CmmFieldCryptoVO result = null;
		result = cmmFieldCryptorMapper.selectFieldCryptoKey(token);
		return result;
	}

}
