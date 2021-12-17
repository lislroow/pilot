package mgkim.proto.www.api.cmm.userlogin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.core.session.KToken;
import mgkim.proto.www.api.cmm.userlogin.mapper.UserLoginMapper;
import mgkim.proto.www.cmm.vo.CmmUserLoginPolicyVO;

@Service
public class UserLoginService {

	@Autowired
	private UserLoginMapper userLoginMapper;

	public boolean selectUserExist(KToken token) throws Exception {
		int cnt = userLoginMapper.selectUserExist(token);
		if(cnt > 0) {
			return true;
		} else {
			return false;
		}
	}

	public CmmUserLoginPolicyVO selectUserLoginPolicy(KToken token) throws Exception {
		return userLoginMapper.selectUserLoginPolicy(token);
	}
}
