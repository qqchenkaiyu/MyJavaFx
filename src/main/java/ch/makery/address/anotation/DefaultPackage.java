package ch.makery.address.anotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Documented
@Inherited  //可以继承
public @interface DefaultPackage {
    String value();
}
