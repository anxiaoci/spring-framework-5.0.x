package com.juan;/**
 * @author admin
 * @date 2020/4/1
 */

import com.juan.config.AppConfig;
import com.juan.service.TestService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */

public class AppTest {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext(AppConfig.class);
		TestService bean = applicationContext.getBean( TestService.class );
		System.out.println( bean );
	}
}
