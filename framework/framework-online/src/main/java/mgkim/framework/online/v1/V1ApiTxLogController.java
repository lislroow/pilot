package mgkim.framework.online.v1;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.cmm.online.vo.CmmApiTxLogVO;
import mgkim.framework.core.annotation.KPageIdx;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.annotation.KRowUnit;
import mgkim.framework.core.dto.KOutPageDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.online.api.com.service.ApiTxLogService;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class V1ApiTxLogController {
	
	private static final Logger log = LoggerFactory.getLogger(V1ApiTxLogController.class);

	@Autowired(required = false)
	private ApiTxLogService apiTxLogService;
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 목록 조회")
	@RequestMapping(value = "/v1/apitxlog", method = RequestMethod.GET)
	public @ResponseBody KOutPageDTO<List<CmmApiTxLogVO>> apitxlog(
			@KRequestMap HashMap<String, Object> inMap,
			@KPageIdx Integer pageidx,
			@KRowUnit Integer rowunit) throws Exception {
		KOutPageDTO<List<CmmApiTxLogVO>> outDTO = new KOutPageDTO<List<CmmApiTxLogVO>>();
		List<CmmApiTxLogVO> outData = apiTxLogService.selectLogList_map(inMap);
		outDTO.setBody(outData);
		return outDTO;
	}
	
	@ApiOperation(value = "(apitxlog) 로그 데이터 조회")
	@RequestMapping(value = "/v1/apitxlog/{txid}", method = RequestMethod.GET)
	public @ResponseBody KOutPageDTO<CmmApiTxLogVO> apitxlog(
			@KRequestMap HashMap<String, Object> inMap,
			@PathVariable(name = "txid") String txid) throws Exception {
		KOutPageDTO<CmmApiTxLogVO> outDTO = new KOutPageDTO<CmmApiTxLogVO>();
		CmmApiTxLogVO outData = apiTxLogService.selectLogByTxid_map(inMap);
		outDTO.setBody(outData);
		return outDTO;
	}
}

