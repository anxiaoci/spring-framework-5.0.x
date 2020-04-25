package com.juan.service;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component("myTestBean")
public class MyFactoryBean implements FactoryBean {
	@Override
	public Object getObject() throws Exception {
		return new MyTestBean();
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}
}
