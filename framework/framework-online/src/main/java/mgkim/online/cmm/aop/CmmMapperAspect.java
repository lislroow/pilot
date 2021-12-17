package mgkim.online.cmm.aop;

import mgkim.online.com.annotation.KModule;

@KModule(name = "매퍼 AOP", required = true)
public interface CmmMapperAspect {

	public void preProcess(Object[] args) throws Throwable;

	public void postProcess(Object ret) throws Throwable;
}