package mgkim.framework.online.api.com.web;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;
import mgkim.framework.core.annotation.KPageIdx;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KInPageDTO;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.dto.KOutPageDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.online.api.com.service.ApiTxLogService;
import mgkim.framework.online.com.scheduler.ComApiTxLogScheduler;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class ApiTxLogController {
	
	private static final Logger log = LoggerFactory.getLogger(ApiTxLogController.class);
	
	@Autowired(required = false)
	private ComApiTxLogScheduler comApiTxLogScheduler;
	
	@Autowired(required = false)
	private ApiTxLogService apiTxLogService;
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 이관")
	@RequestMapping(value = "/api/com/apitxlog/archive", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<String> archive(@RequestBody KInDTO<Long> inDTO) throws Exception {
		KOutDTO<String> outDTO = new KOutDTO<String>();
		Long inData = inDTO.getBody();
		if (inData == null) {
			inData = 10L;
			log.warn(KMessage.get(KMessage.E3001, "로그 데이터 보관 시점(초)", inData));
		}
		int cnt = comApiTxLogScheduler.archive(inData);
		String outData = KMessage.get(KMessage.E1001, cnt);
		outDTO.setBody(outData);
		return outDTO;
	}
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 조회: selectList")
	@RequestMapping(value = "/api/com/apitxlog/selectLogList", method = RequestMethod.POST)
	public @ResponseBody KOutPageDTO<List<CmmApiTxLogVO>> selectLogList(@RequestBody KInPageDTO<KCmmVO> inDTO) throws Exception {
		KOutPageDTO<List<CmmApiTxLogVO>> outDTO = new KOutPageDTO<List<CmmApiTxLogVO>>();
		KCmmVO inVO = inDTO.getBody();
		List<CmmApiTxLogVO> outData = apiTxLogService.selectLogList(inVO);
		outDTO.setBody(outData);
		return outDTO;
	}
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 목록 조회")
	@RequestMapping(value = "/api/com/apitxlog", method = RequestMethod.GET)
	public @ResponseBody KOutPageDTO<List<CmmApiTxLogVO>> apitxlog(
			@KPageIdx Integer pageidx,
			@KRequestMap HashMap<String, String> inMap) throws Exception {
		KOutPageDTO<List<CmmApiTxLogVO>> outDTO = new KOutPageDTO<List<CmmApiTxLogVO>>();
		List<CmmApiTxLogVO> outData = apiTxLogService.selectLogList(inMap);
		outDTO.setBody(outData);
		return outDTO;
	}
}

