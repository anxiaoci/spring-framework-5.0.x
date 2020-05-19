package com.juan.config;

import com.juan.entity.User;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author admin
 * @date 2020/5/16
 * @decription:
 */
@Component("factory")
public class MyFactoryBean implements FactoryBean {
	@Override
	public Object getObject() throws Exception {
		return new User();
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}
}
