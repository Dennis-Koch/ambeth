package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.model.ClobObject;

public interface IClobObjectService
{

	List<ClobObject> getClobObjects(Integer... id);

	void updateClobObject(ClobObject clobObject);

	void deleteClobObject(ClobObject clobObject);
}