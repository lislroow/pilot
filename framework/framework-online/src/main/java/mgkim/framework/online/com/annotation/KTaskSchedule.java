package mgkim.framework.online.com.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KTaskSchedule {

	String name() default "";
	int interval() default 1000;
	boolean manage() default false;

}
