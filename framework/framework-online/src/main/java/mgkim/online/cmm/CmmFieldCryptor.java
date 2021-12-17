package mgkim.online.cmm;

import mgkim.online.cmm.vo.fieldcryptor.CmmFieldCryptoVO;
import mgkim.online.com.annotation.KModule;
import mgkim.online.com.session.KToken;

@KModule(name = "field 암호화키 관리", required = false)
public interface CmmFieldCryptor {

	public void saveRsaKey(CmmFieldCryptoVO vo) throws Exception;

	public void saveSymKey(CmmFieldCryptoVO vo) throws Exception;

	public CmmFieldCryptoVO selectFieldCryptoKey(KToken token) throws Exception;

}
