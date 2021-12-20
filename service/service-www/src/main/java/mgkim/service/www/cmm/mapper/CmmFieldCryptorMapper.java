package mgkim.service.www.cmm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.cmm.vo.fieldcryptor.CmmFieldCryptoVO;

@Mapper
public interface CmmFieldCryptorMapper {

	public int saveRsaKey(CmmFieldCryptoVO vo) throws Exception;

	public int saveSymKey(CmmFieldCryptoVO vo) throws Exception;

	public CmmFieldCryptoVO selectFieldCryptoKey(Map<String, Object> claims) throws Exception;

}

