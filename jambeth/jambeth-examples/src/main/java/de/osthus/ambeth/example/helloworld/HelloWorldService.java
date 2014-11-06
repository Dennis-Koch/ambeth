package de.osthus.ambeth.example.helloworld;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class HelloWorldService {
	@LogInstance
	private ILogger log;

	public void speak() {
		log.info("Hello World!");
	}
}
