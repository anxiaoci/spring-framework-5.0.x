package com.juan.service;

import org.springframework.stereotype.Component;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
@Component
public class TestService {
	private String name;
	private UserService userService;

	private TestService() {
	}

	public TestService(String name) {
		this.name = name;
	}

	public TestService(UserService userService) {
		this.userService = userService;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
