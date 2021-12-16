package mgkim.core.cmm.aop;

import mgkim.core.com.annotation.KModule;

@KModule(name = "서비스 AOP", required = true)
public interface CmmServiceAspect {

	public void preProcess(Object[] args) throws Throwable;

	public void postProcess(Object ret) throws Throwable;

}