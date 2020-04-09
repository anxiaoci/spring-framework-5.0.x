package com.juan.service;/**
 * @author admin
 * @date 2020/4/1
 */

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
@Component
public class TestService {
	private UserService userService;

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public TestService() {
		System.out.println( "TestService id Created" );
	}

	public static TestService getStaticInstance() {
		return new TestService();
	}

	TestService getNoneStatic() {
		return new TestService();
	}


}
