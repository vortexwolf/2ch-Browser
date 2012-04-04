package com.vortexwolf.dvach.api;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class ObjectMapperFactory {

	/** Создает маппер, который будет игнорировать неизвестные свойства */
	public static ObjectMapper createObjectMapper()
	{
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return om;
	}	
}
