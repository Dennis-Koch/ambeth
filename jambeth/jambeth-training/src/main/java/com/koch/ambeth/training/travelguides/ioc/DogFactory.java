package com.koch.ambeth.training.travelguides.ioc;

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.guides.MyDog;

public class DogFactory implements IFactoryBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	int counter = 0;;

	@Override
	public Object getObject() throws Throwable
	{

		// create my dog
		// give it a name
		counter++;
		MyDog dog = new MyDog(counter);
		return dog;
	}
}
