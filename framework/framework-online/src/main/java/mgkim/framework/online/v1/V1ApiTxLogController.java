package mgkim.framework.online.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.annotation.KPageIdx;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.annotation.KRowUnit;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.dto.KOutPageDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.online.api.com.service.ApiTxLogService;

@Api( tags = { KConstant.SWG_V1 } )
@RestController
public class V1ApiTxLogController {
	
	private static final Logger log = LoggerFactory.getLogger(V1ApiTxLogController.class);

	@Autowired(required = false)
	private ApiTxLogService apiTxLogService;
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 목록 조회")
	@RequestMapping(value = "/v1/apitxlog", method = RequestMethod.GET)
	public @ResponseBody KOutPageDTO<List<Map<String, Object>>> apitxlog(
			@KRequestMap HashMap<String, Object> inMap,
			@KPageIdx @RequestParam(required = false) Integer pageidx,
			@KRowUnit @RequestParam(required = false) Integer rowunit) throws Exception {
		KOutPageDTO<List<Map<String, Object>>> outDTO = new KOutPageDTO<List<Map<String, Object>>>();
		List<Map<String, Object>> outData = apiTxLogService.selectLogList_map(inMap);
		outDTO.setBody(outData);
		return outDTO;
	}
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 조회")
	@RequestMapping(value = "/v1/apitxlog/{txid}", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<Map<String, Object>> apitxlogByTxid(
			@KRequestMap HashMap<String, Object> inMap,
			@PathVariable(name = "txid") String txid) throws Exception {
		KOutDTO<Map<String, Object>> outDTO = new KOutDTO<Map<String, Object>>();
		//int a = 1/0;
		//log.info("test");
		Map<String, Object> outData = apiTxLogService.selectLogByTxid_map(inMap);
		outDTO.setBody(outData);
		return outDTO;
	}
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 보관")
	@RequestMapping(value = "/v1/apitxlog/archive", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Integer> archive(
			@KRequestMap HashMap<String, Object> inMap,
			@RequestParam(required = true) Long secondsAgo) throws Exception {
		KOutDTO<Integer> outDTO = new KOutDTO<Integer>();
		int cnt = apiTxLogService.archive_map(inMap);
		outDTO.setBody(cnt);
		return outDTO;
	}
}

