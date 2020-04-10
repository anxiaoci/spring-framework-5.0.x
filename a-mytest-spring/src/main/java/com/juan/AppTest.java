package com.juan;/**
 * @author admin
 * @date 2020/4/1
 */

import com.juan.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */

public class AppTest {
	public static void main(String[] args) {
		//初始化Spring容器
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext( AppConfig.class );
		ClassPathXmlApplicationContext xmlApplicationContext
				= new ClassPathXmlApplicationContext( "spring-config.xml" );
//		TestService bean = applicationContext.getBean(TestService.class);
//		System.out.println(bean);
	}
}
