package com.kingghost.pms.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
public @interface RunAnalyseAnno {
}
