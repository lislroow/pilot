package mgkim.online.com.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mgkim.online.com.type.TCryptoType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KEncrypt {

	TCryptoType type() default TCryptoType.RSA;

}
