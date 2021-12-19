package mgkim.proto.www.cmm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.core.session.KSession;

@Mapper
public interface CmmUserSessionMapper {

	public KSession selectUserSession(Map<String, Object> claims) throws Exception;

	public List<String> selectUserAuthority(Map<String, Object> claims) throws Exception;

}
