package mgkim.online.api.adm.scheduler.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.online.api.adm.scheduler.vo.KSchedulerStatusVO;
import mgkim.online.com.dto.KInDTO;
import mgkim.online.com.dto.KOutDTO;
import mgkim.online.com.env.KConstant;
import mgkim.online.com.mgr.ComScheduleMgr;
import mgkim.online.com.stereo.KScheduler;
import mgkim.online.com.util.KObjectUtil;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class SchedulerController {

	@Autowired(required = false)
	private ComScheduleMgr comScheduleMgr;

	@ApiOperation(value = "(scheduler) 스케줄러 현황")
	@RequestMapping(value = "/api/adm/scheduler/status", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<List<KSchedulerStatusVO>> stopAllScheduler(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<List<KSchedulerStatusVO>> outDTO = new KOutDTO<List<KSchedulerStatusVO>>();
		List<KScheduler> list = comScheduleMgr.scheduleList;

		List<KSchedulerStatusVO> outData = new ArrayList<KSchedulerStatusVO>();
		list.forEach(item -> {
			outData.add(new KSchedulerStatusVO.Builder()
					.clazz(item.getClass().getTypeName())
					.name(KObjectUtil.name(item.getClass()))
					.interval(KObjectUtil.interval(item.getClass()))
					.managed(KObjectUtil.manage(item.getClass()))
					.enabled(item.enabled)
					.running(item.isRunning())
					.uptime(item.uptime())
					.lastStartedTime(item.getLastStartedTime())
					.lastStoppedTime(item.getLastStoppedTime())
					.lastExecutedTime(item.getLastExecutedTime())
					.build());
		});

		outDTO.setBody(outData);
		return outDTO;
	}
}

