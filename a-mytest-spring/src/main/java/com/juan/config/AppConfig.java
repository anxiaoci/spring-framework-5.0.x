package com.juan.config;/**
 * @author admin
 * @date 2020/4/1
 */

import com.juan.entity.Color;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
@Configuration
@ComponentScan("com.juan")
public class AppConfig {

	@Bean
	public Color color() {
		return new Color();
	}
}
