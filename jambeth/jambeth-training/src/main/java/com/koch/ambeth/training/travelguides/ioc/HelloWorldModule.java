package com.koch.ambeth.training.travelguides.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.training.travelguides.TravelGuideService;
import com.koch.ambeth.training.travelguides.guides.BeijingGuideBook;
import com.koch.ambeth.training.travelguides.guides.BookService;
import com.koch.ambeth.training.travelguides.guides.CityService;
import com.koch.ambeth.training.travelguides.guides.DalianGuideBook;
import com.koch.ambeth.training.travelguides.guides.GuideBookExtendable;
import com.koch.ambeth.training.travelguides.guides.IBook;
import com.koch.ambeth.training.travelguides.guides.IBookService;
import com.koch.ambeth.training.travelguides.guides.ICityService;
import com.koch.ambeth.training.travelguides.guides.IGuideBookExtendable;
import com.koch.ambeth.training.travelguides.guides.IMyOwnDog;
import com.koch.ambeth.training.travelguides.places.Cantine;

public class HelloWorldModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{

		bcf.registerBean("helloWorldService", TravelGuideService.class);

		bcf.registerBean(Cantine.class);
		IBeanConfiguration extendable = bcf.registerBean(GuideBookExtendable.class).autowireable(IGuideBookExtendable.class);
		IBeanConfiguration dalian = bcf.registerBean("DalianGuideBook", DalianGuideBook.class);
		IBeanConfiguration beijing = bcf.registerBean("BeijingGuideBook", BeijingGuideBook.class);
		bcf.link(dalian).to(IGuideBookExtendable.class).with("Dalian");
		bcf.link(beijing).to(IGuideBookExtendable.class).with("Beijing");

		bcf.registerBean(CityService.class).autowireable(ICityService.class);

		bcf.registerBean("bookBean", BookFactory.class).autowireable(IBook.class);

		IBeanConfiguration defaultBook = bcf.registerBean(DefaultBook.class);
		// extendable);
		bcf.registerBean(BookService.class).autowireable(IBookService.class).propertyRef("defaultBook", defaultBook);

		bcf.registerBean(DogFactory.class).autowireable(IMyOwnDog.class);

	}
}
