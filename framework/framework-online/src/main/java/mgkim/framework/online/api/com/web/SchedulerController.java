package mgkim.framework.online.api.com.web;

import java.util.ArrayList;
import java.util.HashMap;
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
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.mgr.ComScheduleMgr;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.util.KObjectUtil;

@Api( tags = { KConstant.SWG_SYSTEM_COMMON } )
@RestController
public class SchedulerController {

	@Autowired(required = false)
	private ComScheduleMgr comScheduleMgr;

	@ApiOperation(value = "(scheduler) 스케줄러 현황")
	@RequestMapping(value = "/api/com/scheduler/status", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<List<Map<String, String>>> status(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<List<Map<String, String>>> outDTO = new KOutDTO<List<Map<String, String>>>();
		List<KScheduler> scheduleList = comScheduleMgr.getScheduleList();
		if (scheduleList == null) {
			return outDTO;
		}
		List<Map<String, String>> outBody = null;
		outBody = scheduleList.stream()
					.collect(ArrayList<Map<String, String>>::new, 
							(list, item) -> {
								Map map = new HashMap();
								map.put("clazz", item.getClass().getTypeName());
								map.put("name", KObjectUtil.name(item.getClass()));
								map.put("interval", KObjectUtil.interval(item.getClass()));
								map.put("managed", KObjectUtil.manage(item.getClass()));
								map.put("enabled", item.enabled);
								map.put("running", item.isRunning());
								map.put("uptime", item.uptime());
								map.put("lastStartedTime", item.getLastStartedTime());
								map.put("lastStoppedTime", item.getLastStoppedTime());
								map.put("lastExecutedTime", item.getLastExecutedTime());
								list.add(map);
							}, 
							ArrayList::addAll);
		outDTO.setBody(outBody);
		return outDTO;
	}
}

