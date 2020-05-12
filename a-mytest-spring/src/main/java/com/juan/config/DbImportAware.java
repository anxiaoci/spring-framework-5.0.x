package com.juan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author admin
 * @date 2020/5/12
 * @decription:
 */
@Configuration
public class DbImportAware implements ImportAware {
	/**
	 * Set the annotation metadata of the importing @{@code Configuration} class.
	 *
	 * @param importMetadata
	 */
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		Map<String, Object> map = importMetadata.getAnnotationAttributes(EnableDbUtils.class.getName());

		System.out.println("bbbbbbbbbbbbbbbbbbbbb===>"+map);
	}
}
