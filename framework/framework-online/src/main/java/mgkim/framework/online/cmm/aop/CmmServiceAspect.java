package mgkim.framework.online.cmm.aop;

import mgkim.framework.online.com.annotation.KModule;

@KModule(name = "서비스 AOP", required = true)
public interface CmmServiceAspect {

	public void preProcess(Object[] args) throws Throwable;

	public void postProcess(Object ret) throws Throwable;

}