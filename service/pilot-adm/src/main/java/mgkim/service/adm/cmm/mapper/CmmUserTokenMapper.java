package mgkim.service.adm.cmm.mapper;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.cmm.vo.token.CmmOpenapiTokenVO;

@Mapper
public interface CmmUserTokenMapper {

	/**
	 * openapi 토큰 조회: select
	 */
	public CmmOpenapiTokenVO selectOpenapiToken(String apikey) throws Exception;

}
