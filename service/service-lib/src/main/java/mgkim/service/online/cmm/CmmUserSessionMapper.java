package mgkim.service.online.cmm;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.core.session.KSession;

@Mapper
public interface CmmUserSessionMapper {

	public KSession selectUserSession(Map<String, Object> map) throws Exception;

	public List<String> selectUserAuthority(Map<String, Object> map) throws Exception;

}
