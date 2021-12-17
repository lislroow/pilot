package mgkim.proto.www.cmm.mapper;

import org.apache.ibatis.annotations.Mapper;

import mgkim.online.cmm.vo.fieldcryptor.CmmFieldCryptoVO;
import mgkim.online.com.session.KToken;

@Mapper
public interface CmmFieldCryptorMapper {

	public int saveRsaKey(CmmFieldCryptoVO vo) throws Exception;

	public int saveSymKey(CmmFieldCryptoVO vo) throws Exception;

	public CmmFieldCryptoVO selectFieldCryptoKey(KToken token) throws Exception;

}

