package com.koch.ambeth.persistence.jdbc.interf;

public interface IAbstractEntity
{
	int getId();

	void setId(int id);

	short getVersion();

	void setVersion(short version);

	String getUpdatedBy();

	void setUpdatedBy(String updatedBy);

	String getCreatedBy();

	void setCreatedBy(String createdBy);

	long getUpdatedOn();

	void setUpdatedOn(long updatedOn);

	long getCreatedOn();

	void setCreatedOn(long createdOn);
}
