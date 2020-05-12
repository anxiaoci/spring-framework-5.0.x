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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {
	/**
	 * 主要在Spring的BeanFactory初始化过程中做一些事情，通过一个实现了BeanDefinitionRegistryPostProcessor接口的类来实现做这些事情
	 *
	 * @param beanFactory
	 * @param beanFactoryPostProcessors
	 */
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		// Spring容器内部的后置处理器名字 的set集合
		Set<String> processedBeans = new HashSet<>();

		//处理beanFactory作为BeanDefinitionRegistry bd注册器部分
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			//普通beanFactory后置处理器
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			//注册BeanFactory后置处理器
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/**
			 * Spring框架中自定义BeanFactoryPostProcessor注入的方式：手动add添加，@Component等注解扫描
			 * 此处处理的是使用add手动添加的后置处理器
			 */
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				/**
				 * Spring容器把实现的接口分成两种：实现 BeanDefinitionRegistryPostProcessor 和实现 BeanFactoryPostProcessor
				 * BeanDefinitionRegistryPostProcessor 扩展了 BeanFactoryPostProcessor
				 * BeanDefinitionRegistryPostProcessor有两个接口：
				 * 	（1）postProcessBeanDefinitionRegistry  当前先执行这个方法
				 * 	（2）postProcessBeanFactory
				 */
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					//如果有自定义，则执行自定义注册bean工厂后置处理器中的postProcessBeanDefinitionRegistry方法
					registryProcessor.postProcessBeanDefinitionRegistry( registry );
					//添加到注册后置处理器
					registryProcessors.add( registryProcessor );
				} else {
					//普通bean工厂后置处理器
					regularPostProcessors.add( postProcessor );
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			/**
			 * currentRegistryProcessors 放的是Spring内部自己实现了BeanDefinitionRegistryPostProcessor接口的实现类
			 */
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			/**
			 * BeanDefinitionRegistry的后置处理器执行也有先后顺序：
			 * 1、执行实现了 	PriorityOrdered 的BeanDefinitionRegistryPostProcessors
			 * 2、执行实现了 	Ordered         的BeanDefinitionRegistryPostProcessors
			 * 3、执行		其他所有				BeanDefinitionRegistryPostProcessors
			 * ------------------------------------------------------------------------------------------------
			 * 从BeanDefinitionNames中获取postprocessor的名称，这些类是在BeanFactory准备阶段已经填充的
			 */
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType( BeanDefinitionRegistryPostProcessor.class, true, false );
			// 当前 beanFactory 中，实现了 PriorityOrdered 接口的后置处理器
			// ConfigurationClassPostProcessor 实现了 PriorityOrdered 接口
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch( ppName, PriorityOrdered.class )) {
					currentRegistryProcessors.add( beanFactory.getBean( ppName, BeanDefinitionRegistryPostProcessor.class ) );
					processedBeans.add( ppName );
				}
			}
			//对list中的后置处理器排序
			sortPostProcessors( currentRegistryProcessors, beanFactory );
			//添加到后置处理器集合registryProcessors中，合并list
			registryProcessors.addAll( currentRegistryProcessors );
			/**
			 * 注册spring的beanfactory初始化时拿到的bd，并把import的bd、普通bd扫描出来
			 * 执行当前Spring容器内部的bean定义注册后置处理器BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry
			 * 注意：这里只是执行 postProcessBeanDefinitionRegistry 方法，并没有执行BeanFactoryPostProcessor的方法
			 * 普通bean，扫描出来以后直接注册
			 * import的内容，又分三种情况：先把类信息放到map中，后面注册
			 * 1、ImportSelector
			 * 2、ImportBeanDefinitionRegistar
			 * 3、普通类
			 */
			invokeBeanDefinitionRegistryPostProcessors( currentRegistryProcessors, registry );
			//到这里，Spring就执行完所有的BeanDefinitionPostProcessor，包括Spring内部定义的和程序员自定义的
			//使用完成后，清除，让jvm在合适的时候进行垃圾回收
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			//2、当前beanFactory中，实现了 Ordered 接口的后置处理器
			postProcessorNames = beanFactory.getBeanNamesForType( BeanDefinitionRegistryPostProcessor.class, true, false );
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains( ppName ) && beanFactory.isTypeMatch( ppName, Ordered.class )) {
					currentRegistryProcessors.add( beanFactory.getBean( ppName, BeanDefinitionRegistryPostProcessor.class ) );
					processedBeans.add( ppName );
				}
			}
			sortPostProcessors( currentRegistryProcessors, beanFactory );
			registryProcessors.addAll( currentRegistryProcessors );
			invokeBeanDefinitionRegistryPostProcessors( currentRegistryProcessors, registry );
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			//3、执行其他所有BeanDefinitionRegistryPostProcessors，可能在上面的执行过程中又添加了一些新的，就在这里执行
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType( BeanDefinitionRegistryPostProcessor.class, true, false );
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains( ppName )) {
						currentRegistryProcessors.add( beanFactory.getBean( ppName, BeanDefinitionRegistryPostProcessor.class ) );
						processedBeans.add( ppName );
						reiterate = true;
					}
				}
				sortPostProcessors( currentRegistryProcessors, beanFactory );
				registryProcessors.addAll( currentRegistryProcessors );
				invokeBeanDefinitionRegistryPostProcessors( currentRegistryProcessors, registry );
				currentRegistryProcessors.clear();
			}

			// Now, invoke the BeanFactoryPostProcessor callback of all processors handled so far.
			//执行 实现 BeanDefinitionRegistryPostProcessor 的 BeanFactoryPostPeocessor的方法
			invokeBeanFactoryPostProcessors( registryProcessors, beanFactory );
			//执行自定义的 BeanFactoryPostProcessor
			invokeBeanFactoryPostProcessors( regularPostProcessors, beanFactory );
		} else {
			// Invoke factory processors registered with the context instance.
			//执行由上下文实例注册的工厂processor
			invokeBeanFactoryPostProcessors( beanFactoryPostProcessors, beanFactory );
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		//这里不实例化工厂Bean，把所有普通bean不初始化，让bean工厂post processor申请
		//postProcessor名称数组
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType( BeanFactoryPostProcessor.class, true, false );

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains( ppName )) {
				// skip - already processed in first phase above
			} else if (beanFactory.isTypeMatch( ppName, PriorityOrdered.class )) {
				//这里添加的是实现priorityOrdered接口的PostProcessor
				priorityOrderedPostProcessors.add( beanFactory.getBean( ppName, BeanFactoryPostProcessor.class ) );
			} else if (beanFactory.isTypeMatch( ppName, Ordered.class )) {
				//对于实现Ordered接口的PostProcessor，只是获取其名字，后续处理？？？为什么要这样设计
				orderedPostProcessorNames.add( ppName );
			} else {
				nonOrderedPostProcessorNames.add( ppName );
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		//执行Bean排序方法，改变Bean的初始化顺序
		sortPostProcessors( priorityOrderedPostProcessors, beanFactory );
		invokeBeanFactoryPostProcessors( priorityOrderedPostProcessors, beanFactory );

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		//再执行实现了Ordered接口的对象
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add( beanFactory.getBean( postProcessorName, BeanFactoryPostProcessor.class ) );
		}
		sortPostProcessors( orderedPostProcessors, beanFactory );
		invokeBeanFactoryPostProcessors( orderedPostProcessors, beanFactory );

		// Finally, invoke all other BeanFactoryPostProcessors.
		//执行其他BeanFactoryPostProcessors
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add( beanFactory.getBean( postProcessorName, BeanFactoryPostProcessor.class ) );
		}
		invokeBeanFactoryPostProcessors( nonOrderedPostProcessors, beanFactory );

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	/**
	 * 注册 BeanPostProcessor
	 *
	 * @param beanFactory
	 * @param applicationContext
	 */
	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {


		String[] postProcessorNames = beanFactory.getBeanNamesForType( BeanPostProcessor.class, true, false );

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		//BeanPostProcessorChecker  用于检测类的PostProcessor是否执行
		beanFactory.addBeanPostProcessor( new BeanPostProcessorChecker( beanFactory, beanProcessorTargetCount ) );

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch( ppName, PriorityOrdered.class )) {
				BeanPostProcessor pp = beanFactory.getBean( ppName, BeanPostProcessor.class );
				priorityOrderedPostProcessors.add( pp );
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add( pp );
				}
			} else if (beanFactory.isTypeMatch( ppName, Ordered.class )) {
				orderedPostProcessorNames.add( ppName );
			} else {
				nonOrderedPostProcessorNames.add( ppName );
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors( priorityOrderedPostProcessors, beanFactory );
		registerBeanPostProcessors( beanFactory, priorityOrderedPostProcessors );

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean( ppName, BeanPostProcessor.class );
			orderedPostProcessors.add( pp );
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add( pp );
			}
		}
		sortPostProcessors( orderedPostProcessors, beanFactory );
		registerBeanPostProcessors( beanFactory, orderedPostProcessors );

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean( ppName, BeanPostProcessor.class );
			nonOrderedPostProcessors.add( pp );
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add( pp );
			}
		}
		registerBeanPostProcessors( beanFactory, nonOrderedPostProcessors );

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors( internalPostProcessors, beanFactory );
		registerBeanPostProcessors( beanFactory, internalPostProcessors );

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor( new ApplicationListenerDetector( applicationContext ) );
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort( comparatorToUse );
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 * 执行 Spring容器内部 BeanDefinitionRegistryPostProcessor
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry( registry );
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 * 执行
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory( beanFactory );
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor( postProcessor );
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog( BeanPostProcessorChecker.class );

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean( beanName ) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info( "Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)" );
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition( beanName )) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition( beanName );
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
