package de.osthus.ambeth.training.travelguides.setup;

import java.util.Collection;

import de.osthus.ambeth.training.travelguides.model.City;
import de.osthus.ambeth.training.travelguides.model.GuideBook;
import de.osthus.ambeth.training.travelguides.model.Image;
import de.osthus.ambeth.util.setup.AbstractDatasetBuilder;
import de.osthus.ambeth.util.setup.IDatasetBuilder;

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
