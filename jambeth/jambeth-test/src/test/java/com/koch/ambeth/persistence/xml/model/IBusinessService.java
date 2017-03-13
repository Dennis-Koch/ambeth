package com.koch.ambeth.persistence.xml.model;

import java.util.List;

public interface IBusinessService
{
	List<Employee> retrieve(List<String> names);
}
