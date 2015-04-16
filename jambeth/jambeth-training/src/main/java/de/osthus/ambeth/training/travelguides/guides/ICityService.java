package de.osthus.ambeth.training.travelguides.guides;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.training.travelguides.model.City;

public interface ICityService
{

	List<City> retrieveSops(int... ids);

	void saveCity(Collection<City> city);

	@Merge
	void saveCity(City... citys);

}
