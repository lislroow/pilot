package mgkim.framework.cmm.online;

import mgkim.framework.core.annotation.KModule;

@KModule(name = "서비스 AOP", required = true)
public interface CmmServiceAspect {

	public void preProcess(String clazzName, String methodName, Object[] args) throws Throwable;

	public void postProcess(String clazzName, String methodName, Object ret) throws Throwable;

}