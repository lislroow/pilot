package mgkim.framework.online.api.adm.apitxlog.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInPageDTO;
import mgkim.framework.core.dto.KOutPageDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.online.api.adm.apitxlog.service.ApiTxLogService;
import mgkim.framework.online.cmm.vo.apitxlog.CmmApiTxLogVO;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class ApiTxLogController {

	@Autowired(required = false)
	private ApiTxLogService apiTxLogService;

	@ApiOperation(value = "(apitxlog) 로그 데이터 조회: selectList")
	@RequestMapping(value = "/api/adm/apitxlog/selectLogList", method = RequestMethod.POST)
	public @ResponseBody KOutPageDTO<List<CmmApiTxLogVO>> selectLogList(@RequestBody KInPageDTO<KCmmVO> inDTO) throws Exception {
		KOutPageDTO<List<CmmApiTxLogVO>> outDTO = new KOutPageDTO<List<CmmApiTxLogVO>>();
		KCmmVO inVO = inDTO.getBody();
		List<CmmApiTxLogVO> outData = apiTxLogService.selectLogList(inVO);
		outDTO.setBody(outData);
		return outDTO;
	}
}

