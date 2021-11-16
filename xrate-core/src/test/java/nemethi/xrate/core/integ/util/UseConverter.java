package nemethi.xrate.core.integ.util;

import nemethi.xrate.api.CurrencyConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseConverter {

    Class<? extends CurrencyConverter> value();
}
