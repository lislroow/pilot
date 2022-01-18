package mgkim.framework.online.cmm.aop;

import mgkim.framework.core.annotation.KModule;

@KModule(name = "매퍼 AOP", required = true)
public interface CmmMapperAspect {

	public void preProcess(String clazzName, String methodName, Object[] args) throws Throwable;

	public void postProcess(String clazzName, String methodName, Object ret) throws Throwable;
}