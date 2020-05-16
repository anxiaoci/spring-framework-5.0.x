package com.juan.config;

import com.juan.entity.Color;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
@Configuration
@ComponentScan(value = "com.juan")
@EnableAspectJAutoProxy
@EnableDbUtils
public class AppConfig {
	@Bean
	public Color color() {
		return new Color();
	}

	@Bean()
	public UserFactory userFactory(){
		return new UserFactory();
	}

}
