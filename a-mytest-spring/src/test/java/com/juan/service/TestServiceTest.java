package com.juan.service;


import org.junit.Test;

import java.util.Arrays;

public class TestServiceTest {

	@Test
	public void testConstructor(){
		Class cls = TestService.class;
		Arrays.stream( cls.getConstructors() )
				.map( x->x.getName() )
				.forEach( System.out::println );
		System.out.println("==============================");
		Arrays.stream( cls.getDeclaredConstructors() )
				.map( x->x.getName() )
				.forEach( System.out::println );
	}

}