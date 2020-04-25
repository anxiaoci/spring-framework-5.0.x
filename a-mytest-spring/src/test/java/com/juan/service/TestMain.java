package com.juan.service;

public class TestMain {
	public static void main(String[] args) {
		User user = new User();
		System.out.println(user.getAge());
		user.test(user);
		System.out.println(user.getAge());
	}
}

class User{
	public void test(User user){
		user.setAge(212);
	}

	private Integer age = 10;

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
}