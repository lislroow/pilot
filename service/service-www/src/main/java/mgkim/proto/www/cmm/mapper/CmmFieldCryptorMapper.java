package mgkim.proto.www.cmm.mapper;

import org.apache.ibatis.annotations.Mapper;

import mgkim.core.cmm.vo.fieldcryptor.CmmFieldCryptoVO;
import mgkim.core.com.session.KToken;

@Mapper
public interface CmmFieldCryptorMapper {

	public int saveRsaKey(CmmFieldCryptoVO vo) throws Exception;

	public int saveSymKey(CmmFieldCryptoVO vo) throws Exception;

	public CmmFieldCryptoVO selectFieldCryptoKey(KToken token) throws Exception;

}

