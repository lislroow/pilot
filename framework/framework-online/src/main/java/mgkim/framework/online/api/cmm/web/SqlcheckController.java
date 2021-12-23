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
import mgkim.framework.online.api.cmm.vo.SqlcheckVO;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class SqlcheckController {

	@Autowired(required = false)
	private SqlcheckMapper sqlcheckMapper;
	
	@ApiOperation(value = "(sqlcheck) selectDumpNoParam")
	@RequestMapping(value = "/public/cmm/sqlcheck/selectNoParam", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Map> selectNoParam(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<Map> outDTO = new KOutDTO<Map>();
		Map outMap = sqlcheckMapper.selectNoParam();
		outDTO.setBody(outMap);
		return outDTO;
	}
	
	@ApiOperation(value = "(sqlcheck) selectForeachListByVO")
	@RequestMapping(value = "/public/cmm/sqlcheck/selectForeachListByVO", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<List<Map>> selectForeachListByVO(@RequestBody KInPageDTO<SqlcheckVO> inDTO) throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();
		SqlcheckVO inVO = inDTO.getBody();
		List<Map> outMap = sqlcheckMapper.selectForeachListByVO(inVO);
		outDTO.setBody(outMap);
		return outDTO;
	}
	
	@ApiOperation(value = "(sqlcheck) selectForeachListByMap")
	@RequestMapping(value = "/public/cmm/sqlcheck/selectForeachListByMap", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<List<Map>> selectForeachListByMap(@RequestBody KInPageDTO<Map> inDTO) throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();
		Map inMap = inDTO.getBody();
		List<Map> outMap = sqlcheckMapper.selectForeachListByMap(inMap);
		outDTO.setBody(outMap);
		return outDTO;
	}

}
