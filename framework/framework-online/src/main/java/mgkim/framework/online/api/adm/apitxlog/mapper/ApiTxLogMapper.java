package mgkim.framework.online.api.adm.apitxlog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.online.cmm.vo.apitxlog.CmmApiTxLogVO;

@Mapper
public interface ApiTxLogMapper {

	public List<CmmApiTxLogVO> selectLogList(KCmmVO vo) throws Exception;

}
