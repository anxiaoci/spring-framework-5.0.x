package com.juan.service;/**
 * @author admin
 * @date 2020/4/1
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
//@Component()
public class UserService {
	@Autowired
	private TestService testService;

	public UserService(){
		System.out.println("UserService is created");

	}

	public void init(){
		System.out.println("userService init----------");
	}

}
