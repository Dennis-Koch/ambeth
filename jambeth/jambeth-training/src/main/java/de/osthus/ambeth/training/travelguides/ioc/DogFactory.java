package de.osthus.ambeth.training.travelguides.ioc;

import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.guides.MyDog;

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
