package de.osthus.ambeth.persistence.jdbc.array;

import java.util.List;

public interface IArrayObjectService
{
	ArrayObject getArrayObject(Integer id);

	List<ArrayObject> getArrayObjects(Integer... id);

	List<ArrayObject> getAllArrayObjects();

	void updateArrayObject(ArrayObject arrayObject);

	void deleteArrayObject(ArrayObject arrayObject);
}