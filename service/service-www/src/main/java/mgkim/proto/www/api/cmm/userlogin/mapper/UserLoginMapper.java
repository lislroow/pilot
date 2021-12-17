package mgkim.proto.www.api.cmm.userlogin.mapper;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.core.session.KToken;
import mgkim.proto.www.cmm.vo.CmmUserLoginPolicyVO;

@Mapper
public interface UserLoginMapper {

	public int selectUserExist(KToken token) throws Exception;

	public CmmUserLoginPolicyVO selectUserLoginPolicy(KToken token) throws Exception;


}
