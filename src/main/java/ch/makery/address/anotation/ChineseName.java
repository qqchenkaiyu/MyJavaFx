package ch.makery.address.anotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD})
@Documented
@Inherited  //可以继承
public @interface ChineseName {
    String value();
}
