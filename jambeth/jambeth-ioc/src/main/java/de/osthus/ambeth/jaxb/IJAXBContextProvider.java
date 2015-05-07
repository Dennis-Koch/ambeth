package de.osthus.ambeth.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public interface IJAXBContextProvider
{
	JAXBContext acquireSharedContext(Class<?>... classesToBeBound) throws JAXBException;
}