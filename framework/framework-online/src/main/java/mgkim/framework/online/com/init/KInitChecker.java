package mgkim.framework.online.com.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.util.KStringUtil;

@KBean
public class KInitChecker {

	//@Profile({"dev", "test", "prod"})
	@EventListener
	public void started(ContextStartedEvent event) {
		ApplicationContext ctx = event.getApplicationContext();
		List<String> beanNameList = Arrays.asList(ctx.getBeanDefinitionNames());
		KLogSys.info("spring {} = {} {} spring bean {}개가 로드 되었습니다.",
				ctx.getId(), KStringUtil.toJson(beanNameList), KLogLayout.LINE, beanNameList.size());
		List<String> beanClassList = new ArrayList<String>();
		if(beanNameList != null) {
			beanNameList.forEach(item -> {
				String className = ctx.getBean(item).getClass().getName();
				if(!className.startsWith("com.sun.proxy.")) {
					Matcher m = Pattern.compile("(^[^\\${2}]*)\\${2}.*").matcher(className);
					if(m.find()) {
						beanClassList.add(m.replaceFirst("$1"));
					}
				}
			});
		}
		Collections.sort(beanClassList);
		KLogSys.info("{} spring bean = {} {} ", ctx.getId(), KLogLayout.LINE, KStringUtil.toJson(beanClassList).replaceAll(",", KLogLayout.LINE).replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", ""));
		KLogSys.info("spring bean {}개가 로드 되었습니다. (실제: 전체 {}개 중 {}개 로그 출력에서 생략))", beanNameList.size(), beanNameList.size(), (beanNameList.size()-beanClassList.size()));
		KLogSys.info("spring ["+event.getApplicationContext().getId() + "] started ");
	}

	//@Profile({"dev", "test", "prod"})
	@EventListener
	public void refreshed(ContextRefreshedEvent event) {
		ApplicationContext ctx = event.getApplicationContext();
		if(ctx.getParent() != null) {
			KLogSys.info("spring ["+event.getApplicationContext().getId() + "] refreshed ");
		}
	}
}