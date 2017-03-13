package com.koch.ambeth.training.travelguides.setup;

import java.util.Collection;

import com.koch.ambeth.training.travelguides.model.City;
import com.koch.ambeth.training.travelguides.model.GuideBook;
import com.koch.ambeth.training.travelguides.model.Image;
import com.koch.ambeth.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.util.setup.IDatasetBuilder;

public class CoreDatasetBuilder extends AbstractDatasetBuilder
{

	public City city;
	public GuideBook guideBook;
	public Image image;

	@Override
	protected void buildDatasetInternal()
	{
		city = createEntity(City.class);
		guideBook = createEntity(GuideBook.class);
		image = createEntity(Image.class);
	}

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn()
	{
		return null;
	}

}
