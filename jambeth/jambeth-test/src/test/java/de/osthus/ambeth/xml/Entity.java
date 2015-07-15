package de.osthus.ambeth.xml;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.model.IAbstractEntity;

@XmlType
public interface Entity extends IAbstractEntity
{
	String getName();

	String getName2();
}
