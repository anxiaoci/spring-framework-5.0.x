package com.juan.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author admin
 * @date 2020/5/12
 * @decription:
 */
@Component
@Aspect
public class MyAspect {
	@Pointcut("execution(* com.juan.service..*.*(..))")
	public void pointCut() {
	}

	@Before("pointCut()")
	public void beforePrint() {
		System.out.println( "--------before--------" );
	}

	@After( "pointCut()" )
	public void after(){
		System.out.println("---------after-------------");
	}
}
