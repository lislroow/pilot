package mgkim.service.www.api.cmm.userlogin.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.service.www.api.cmm.userlogin.mapper.UserLoginMapper;
import mgkim.service.www.cmm.vo.CmmUserLoginPolicyVO;

@Service
public class UserLoginService {

	@Autowired
	private UserLoginMapper userLoginMapper;
	
	public boolean selectUserExist(Map<String, Object> claims) throws Exception {
		int cnt = userLoginMapper.selectUserExist(claims);
		if (cnt > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public CmmUserLoginPolicyVO selectUserLoginPolicy(Map<String, Object> claims) throws Exception {
		return userLoginMapper.selectUserLoginPolicy(claims);
	}
}
