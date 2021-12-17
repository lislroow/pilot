package mgkim.proto.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.online.com.session.KSession;
import mgkim.framework.online.com.session.KToken;

@Mapper
public interface CmmUserSessionMapper {

	public KSession selectUserSession(KToken token) throws Exception;

	public List<String> selectUserAuthority(KToken token) throws Exception;

}
