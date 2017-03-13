package com.koch.ambeth.training.travelguides.guides;

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.annotation.Merge;
import com.koch.ambeth.training.travelguides.model.City;

public interface ICityService
{

	List<City> retrieveSops(int... ids);

	void saveCity(Collection<City> city);

	@Merge
	void saveCity(City... citys);

}
