package mgkim.core.cmm;

import mgkim.core.cmm.vo.fieldcryptor.CmmFieldCryptoVO;
import mgkim.core.com.annotation.KModule;
import mgkim.core.com.session.KToken;

@KModule(name = "field 암호화키 관리", required = false)
public interface CmmFieldCryptor {

	public void saveRsaKey(CmmFieldCryptoVO vo) throws Exception;

	public void saveSymKey(CmmFieldCryptoVO vo) throws Exception;

	public CmmFieldCryptoVO selectFieldCryptoKey(KToken token) throws Exception;

}
