package com.juan;/**
 * @author admin
 * @date 2020/4/1
 */

import com.juan.config.AppConfig;
import com.juan.config.MyBeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
public class AppTest {
	public static void main(String[] args) {
		//初始化Spring容器
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext();
		applicationContext.register(AppConfig.class);
		applicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor());
		applicationContext.refresh();
//		applicationContext.getBean(TestService.class).hashCode();


	}
}
