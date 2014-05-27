package de.osthus.ambeth.cache.model;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IPrintable;

@XmlType
public interface IObjRelation extends IPrintable
{
	String getMemberName();

	IObjRef[] getObjRefs();

	Class<?> getRealType();

	Object getVersion();
}
