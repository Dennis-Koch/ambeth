package com.koch.ambeth.xml;

import com.koch.ambeth.model.IAbstractEntity;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface Entity extends IAbstractEntity
{
	String getName();

	String getName2();
}
