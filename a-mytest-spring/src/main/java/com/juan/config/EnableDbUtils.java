package com.juan.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import( DbImportAware.class )
public @interface EnableDbUtils {

	String uname() default "root";

}
