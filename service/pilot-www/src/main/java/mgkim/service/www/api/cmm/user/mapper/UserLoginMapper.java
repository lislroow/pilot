package mgkim.service.www.api.cmm.user.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.service.cmm.online.vo.CmmUserLoginPolicyVO;

@Mapper
public interface UserLoginMapper {

	public int selectUserExist(Map<String, Object> map) throws Exception;

	public CmmUserLoginPolicyVO selectUserLoginPolicy(Map<String, Object> map) throws Exception;


}
