package mgkim.online.api.adm.apitxlog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.online.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.online.com.dto.KCmmVO;

@Mapper
public interface ApiTxLogMapper {

	public List<CmmApiTxLogVO> selectLogList(KCmmVO vo) throws Exception;

}
