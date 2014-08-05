package de.osthus.ambeth.model;

public interface IAbstractEntity extends IAbstractBusinessObject
{
	Short getVersion();

	String getUpdatedBy();

	String getCreatedBy();

	Long getUpdatedOn();

	Long getCreatedOn();
}
