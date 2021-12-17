package mgkim.framework.online.com.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mgkim.framework.online.com.type.TCryptoType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KEncrypt {

	TCryptoType type() default TCryptoType.RSA;

}
