package mgkim.proto.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.core.com.session.KSession;
import mgkim.core.com.session.KToken;

@Mapper
public interface CmmUserSessionMapper {

	public KSession selectUserSession(KToken token) throws Exception;

	public List<String> selectUserAuthority(KToken token) throws Exception;

}
