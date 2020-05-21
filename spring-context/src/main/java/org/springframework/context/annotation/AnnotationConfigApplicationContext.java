/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.function.Supplier;


/**
 * Standalone application context, accepting annotated classes as input - in particular
 * {@link Configuration @Configuration}-annotated classes, but also plain
 * {@link org.springframework.stereotype.Component @Component} types and JSR-330 compliant
 * classes using {@code javax.inject} annotations. Allows for registering classes one by
 * one using {@link #register(Class...)} as well as for classpath scanning using
 * {@link #scan(String...)}.
 *
 * <p>In case of multiple {@code @Configuration} classes, @{@link Bean} methods defined in
 * later classes will override those defined in earlier classes. This can be leveraged to
 * deliberately override certain bean definitions via an extra {@code @Configuration}
 * class.
 *
 * <p>See @{@link Configuration}'s javadoc for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 * @since 3.0
 * <p>
 * Spring中用来解析注解bean的定义有两个：
 * AnnotationConfigApplicationContext和
 * AnnotationConfigWebApplicationContext。
 * AnnotationConfigWebApplicationContext是AnnotationConfigApplicationContext的web版本
 * 两者的用法和对注解的处理方法几乎没有差别
 * 通过分析这个类，可以知道注册一个bean到Spring容器中有两种方法：
 * 一、直接将注解bean注册到容器中（参考public void register(Class<?>... annotatedClasses)）
 * 注册一个注解bean到容器中也分两种方法：
 * 1、在初始化容器时注册并解析
 * 2、在容器创建以后手动调用注册 方法向容器中注册，然后手动调用刷新方法refresh刷新容器，使得容器对注册的注解bean进行处理
 * ---->@Profile 注解是第二种方法
 * ---->两种方法的优缺点和使用场景
 * 二、通过扫描指定包及其子包下所有类的方式
 * 扫描方式注册同上，分为初始化扫描和初始化以后扫描
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	//注解的bean定义扫描器
	private final AnnotatedBeanDefinitionReader reader;
	//类路径下的bean定义扫描器
	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * 初始化bean的读取和扫描器
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AnnotationConfigApplicationContext() {
		/**
		 * 调用父类构造器，把BeanFactory赋值为DefaultListableBeanFactory
		 * 初始化注解模式下的bean定义扫描器
		 * 作用：BeanDefinition读取器
		 * ---->在初始化过程中，注册了6个BeanDefinition(包含ConfigurationClassPostProcessor)
		 */
		this.reader = new AnnotatedBeanDefinitionReader( this );
		/**
		 * 作用：BeanDefinition扫描器，扫描包或者类转化为BeanDefinition
		 *	但是实际上我们在Spring容器启动中扫描包用的并不是这个scanner来完成的
		 *	而是Spring自己new出来的一个ClassPathBeanDefinitionScanner对象
		 *
		 *	因此，这里的scanner只是为了程序员自己手动调用AnnotationConfigApplicationContext的scanner的scan方法手动扫描
		 *
		 *
		 */
		this.scanner = new ClassPathBeanDefinitionScanner( this );
	}

	/**
	 * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
	 *
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		super( beanFactory );
		this.reader = new AnnotatedBeanDefinitionReader( this );
		this.scanner = new ClassPathBeanDefinitionScanner( this );
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, deriving bean definitions
	 * from the given annotated classes and automatically refreshing the context.
	 *
	 * @param annotatedClasses one or more annotated classes,
	 *                         e.g. {@link Configuration @Configuration} classes
	 *                         这个构造方法需要传入一个 java config注解了的配置类，这个配置类被
	 */
	public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
		/*
		 * Spring的前提环境准备
		 * 1、准备Spring的Bean工厂：实例化DefaultListableBeanFactory
		 * 		--->org.springframework.context.support.GenericApplicationContext->GenericApplicationContext()
		 * 2、初始化一个读取器 AnnotatedBeanDefinitionReader
		 * 3、初始化一个扫描器 ClassPathBeanDefinitionScanner
		 */
		this();
		//注册配置类(带有@Configuration注解的配置类)
		//处理注解内容，注解生成BeanDefinition存放到beanDefinitionMap中
		/**
		 * 把一个配置类转为BeanDefinition，put 到beanDefinitionMap中
		 * beanDefinitionMap是DefaultListableBeanFactory的一个属性
		 */
		register( annotatedClasses );
		//初始化Spring环境
		refresh();
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, scanning for bean definitions
	 * in the given packages and automatically refreshing the context.
	 *
	 * @param basePackages the packages to check for annotated classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		scan( basePackages );
		refresh();
	}


	/**
	 * Propagates the given custom {@code Environment} to the underlying
	 * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment( environment );
		this.reader.setEnvironment( environment );
		this.scanner.setEnvironment( environment );
	}

	/**
	 * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 * <p>Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 *
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator( beanNameGenerator );
		this.scanner.setBeanNameGenerator( beanNameGenerator );
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator );
	}

	/**
	 * Set the {@link ScopeMetadataResolver} to use for detected bean classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver( scopeMetadataResolver );
		this.scanner.setScopeMetadataResolver( scopeMetadataResolver );
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry
	//---------------------------------------------------------------------

	/**
	 * Register one or more annotated classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 *
	 * @param annotatedClasses one or more annotated classes,
	 *                         e.g. {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	@Override
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty( annotatedClasses, "At least one annotated class must be specified" );
		this.reader.register( annotatedClasses );
	}

	/**
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 *
	 * @param basePackages the packages to check for annotated classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	@Override
	public void scan(String... basePackages) {
		Assert.notEmpty( basePackages, "At least one base package must be specified" );
		this.scanner.scan( basePackages );
	}


	//---------------------------------------------------------------------
	// Convenient methods for registering individual beans
	//---------------------------------------------------------------------

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, and optionally providing explicit constructor
	 * arguments for consideration in the autowiring process.
	 * <p>The bean name will be generated according to annotated component rules.
	 *
	 * @param annotatedClass       the class of the bean
	 * @param constructorArguments argument values to be fed into Spring's
	 *                             constructor resolution algorithm, resolving either all arguments or just
	 *                             specific ones, with the rest to be resolved through regular autowiring
	 *                             (may be {@code null} or empty)
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> annotatedClass, Object... constructorArguments) {
		registerBean( null, annotatedClass, constructorArguments );
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, and optionally providing explicit constructor
	 * arguments for consideration in the autowiring process.
	 *
	 * @param beanName             the name of the bean (may be {@code null})
	 * @param annotatedClass       the class of the bean
	 * @param constructorArguments argument values to be fed into Spring's
	 *                             constructor resolution algorithm, resolving either all arguments or just
	 *                             specific ones, with the rest to be resolved through regular autowiring
	 *                             (may be {@code null} or empty)
	 * @since 5.0
	 */
	public <T> void registerBean(@Nullable String beanName, Class<T> annotatedClass, Object... constructorArguments) {
		this.reader.doRegisterBean( annotatedClass, null, beanName, null,
				bd -> {
					for (Object arg : constructorArguments) {
						bd.getConstructorArgumentValues().addGenericArgumentValue( arg );
					}
				} );
	}

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass, @Nullable Supplier<T> supplier,
								 BeanDefinitionCustomizer... customizers) {

		this.reader.doRegisterBean( beanClass, supplier, beanName, null, customizers );
	}

}
