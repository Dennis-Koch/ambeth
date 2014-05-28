package de.osthus.ambeth.persistence.transfer;

import java.util.Date;

public interface IEntity
{

	int getRecId();

	void setRecId(int recId);

	String getCreatedBy();

	void setCreatedBy(String createdBy);

	Date getCreatedOn();

	void setCreatedOn(Date createdOn);

	String getUpdatedBy();

	void setUpdatedBy(String updatedBy);

	Date getUpdatedOn();

	void setUpdatedOn(Date updatedOn);

	int getVersion();

	int setVersion(int version);

}
