package com.juan;/**
 * @author admin
 * @date 2020/4/1
 */

import com.juan.config.AppConfig;
import com.juan.config.MyBeanFactoryPostProcessor;
import com.juan.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */

/**
 * 解析@Import时，@ImportSource
 */

public class AppTest {
	public static void main(String[] args) {
		//初始化Spring容器
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext();
		applicationContext.register(AppConfig.class);
//		applicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor());
		applicationContext.refresh();
		applicationContext.getBean(UserService.class);
		System.out.println("00000000000000000000");

//		System.out.println(Import.class.getName());
//		applicationContext.getBean(TestService.class).hashCode();


	}
}
