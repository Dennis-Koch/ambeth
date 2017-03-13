package com.koch.ambeth.example.helloworld;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class HelloWorldService {
	@LogInstance
	private ILogger log;

	public void speak() {
		log.info("Hello World!");
	}
}
