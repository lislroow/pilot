package mgkim.framework.online.cmm;

import java.util.Map;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.online.cmm.vo.fieldcryptor.CmmFieldCryptoVO;

@KModule(name = "field 암호화키 관리", required = false)
public interface CmmFieldCryptor {

	public void saveRsaKey(CmmFieldCryptoVO vo) throws Exception;

	public void saveSymKey(CmmFieldCryptoVO vo) throws Exception;

	public CmmFieldCryptoVO selectFieldCryptoKey(Map<String, Object> claims) throws Exception;

}
