package com.koch.ambeth.service;

import java.util.List;

import com.koch.ambeth.model.ClobObject;

public interface IClobObjectService
{

	List<ClobObject> getClobObjects(Integer... id);

	void updateClobObject(ClobObject clobObject);

	void deleteClobObject(ClobObject clobObject);
}