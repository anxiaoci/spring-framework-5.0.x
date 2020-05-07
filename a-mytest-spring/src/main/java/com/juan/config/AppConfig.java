package com.juan.config;

import com.juan.entity.Color;
import com.juan.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
@Configuration
@ComponentScan(value = "com.juan")
@Import(MyImportSelector.class)
public class AppConfig {

	@Bean
	public Color color() {
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		System.out.println("color");
		System.out.println("++++++++++++++++++++++++++++++++++++++++");
		return new Color();
	}

	@Bean(initMethod = "init")
	public UserService userService() {
		color();
		return new UserService();
	}
}
