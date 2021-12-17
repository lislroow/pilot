package mgkim.framework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mgkim.framework.core.type.TCryptoType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KEncrypt {

	TCryptoType type() default TCryptoType.RSA;

}
