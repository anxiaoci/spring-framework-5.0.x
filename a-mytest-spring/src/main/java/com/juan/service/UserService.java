package com.juan.service;/**
 * @author admin
 * @date 2020/4/1
 */

import com.juan.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author admin
 * @date 2020/4/1
 * @decription:
 */
@Component
public class UserService {
	@Autowired
	private UserDao userDao;

	public void getUser(){
		userDao.get();
		System.out.println("------service getUser ----");
	}

}
