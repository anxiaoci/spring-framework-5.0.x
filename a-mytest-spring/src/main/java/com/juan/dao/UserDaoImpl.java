package com.juan.dao;

import com.juan.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @author admin
 * @date 2020/5/12
 * @decription:
 */
@Repository
public class UserDaoImpl implements UserDao {

	@Override
	public User get() {
		System.out.println("---------dao get-------");
		return new User();
	}
}
