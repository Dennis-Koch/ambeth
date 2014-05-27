package de.osthus.ambeth.persistence.xml.model;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.annotation.Remove;
import de.osthus.ambeth.cache.Cached;

public interface IEmployeeService
{
	List<Employee> getAll();

	Employee getByName(String name);

	@Cached(type = Employee.class, alternateIdName = "Name")
	List<Employee> retrieve(List<String> names);

	void save(Employee employee);

	void save(List<Employee> employees);

	void delete(Employee employee);

	void delete(List<Employee> employee);

	void delete(Set<Employee> employees);

	void delete(Employee[] employees);

	@Remove(entityType = Employee.class, idName = "Name")
	void delete(String name);

	List<Employee> retrieveOrderedByName(boolean reverse);

	Boat saveBoat(Boat boat);
}