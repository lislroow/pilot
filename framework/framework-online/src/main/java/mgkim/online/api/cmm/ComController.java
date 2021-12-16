package mgkim.online.api.cmm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.core.cmm.vo.debug.CmmDebugVO;
import mgkim.core.com.dto.KInDTO;
import mgkim.core.com.dto.KOutDTO;
import mgkim.core.com.env.KConstant;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.scheduler.CmmApiTxLogScheduler;
import mgkim.core.com.scheduler.ComDebugScheduler;
import mgkim.core.com.util.KDateUtil;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class ComController {

	@Autowired(required = false)
	private CmmApiTxLogScheduler cmmApiTxLogScheduler;

	@Autowired(required = false)
	private ComDebugScheduler comDebugScheduler;

	@ApiOperation(value = "(apitxlog) 로그 데이터 이관")
	@RequestMapping(value = "/api/cmm/apitxlog/archive", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<String> archive(@RequestBody KInDTO<Long> inDTO) throws Exception {
		KOutDTO<String> outDTO = new KOutDTO<String>();
		Long inData = inDTO.getBody();
		if(inData == null) {
			inData = 10L;
			KLogSys.warn(KMessage.get(KMessage.E3001, "로그 데이터 보관 시점(초)", inData));
		}
		int cnt = cmmApiTxLogScheduler.archive(inData);
		String outData = KMessage.get(KMessage.E1001, cnt);
		outDTO.setBody(outData);
		return outDTO;
	}

	@ApiOperation(value = "(session) 세션 trace")
	@RequestMapping(value = "/api/cmm/session/trace-start", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<CmmDebugVO> traceStart(@RequestBody KInDTO<CmmDebugVO> inDTO) throws Exception {
		KOutDTO<CmmDebugVO> outDTO = new KOutDTO<CmmDebugVO>();
		CmmDebugVO inVO = inDTO.getBody();
		CmmDebugVO outVO = comDebugScheduler.add(inVO);
		outDTO.setBody(outVO);
		return outDTO;
	}

	@ApiOperation(value = "(session) 세션 health-check")
	@RequestMapping(value = {"/api/cmm/session/health-check"}, method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> healthCheck() throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		return outDTO;
	}

	@ApiOperation(value = "(time) 시간 현재시각")
	@RequestMapping(value = "/public/cmm/time/systimestamp", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<String> systimestamp(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<String> outDTO = new KOutDTO<String>();
		outDTO.setBody(KDateUtil.now(KConstant.FMT_YYYY_MM_DD_HH_MM_SS));
		return outDTO;
	}
}
