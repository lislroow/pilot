package mgkim.proto.www.api.cmm.userlogin.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.proto.www.cmm.vo.CmmUserLoginPolicyVO;

@Mapper
public interface UserLoginMapper {

	public int selectUserExist(Map<String, Object> claims) throws Exception;

	public CmmUserLoginPolicyVO selectUserLoginPolicy(Map<String, Object> claims) throws Exception;


}
