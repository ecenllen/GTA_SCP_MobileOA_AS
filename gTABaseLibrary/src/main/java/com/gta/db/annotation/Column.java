package com.gta.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD })
public @interface Column {
	int getLength() default 0;

	String name();

	String type() default "";

	String defaultValue() default "";// 暂时不支持
}
