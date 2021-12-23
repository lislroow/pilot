package mgkim.framework.online.api.cmm.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KInPageDTO;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.online.api.cmm.mapper.SqlcheckMapper;
import mgkim.framework.online.api.cmm.vo.Sqlcheck1VO;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class SqlcheckController {

	@Autowired(required = false)
	private SqlcheckMapper sqlcheckMapper;
	
	@ApiOperation(value = "(sqlcheck) selectDumpNoParam")
	@RequestMapping(value = "/public/cmm/sqlcheck/selectDumpNoParam", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Map> selectDumpList(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<Map> outDTO = new KOutDTO<Map>();
		Map outMap = sqlcheckMapper.selectDumpNoParam();
		outDTO.setBody(outMap);
		return outDTO;
	}
	
	@ApiOperation(value = "(sqlcheck) selectDumpList: foreach")
	@RequestMapping(value = "/public/cmm/sqlcheck/selectDumpList", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<List<Map>> selectDumpList(@RequestBody KInPageDTO<Sqlcheck1VO> inDTO) throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();
		Sqlcheck1VO inVO = inDTO.getBody();
		List<Map> outMap = sqlcheckMapper.selectDumpList(inVO);
		outDTO.setBody(outMap);
		return outDTO;
	}

}
