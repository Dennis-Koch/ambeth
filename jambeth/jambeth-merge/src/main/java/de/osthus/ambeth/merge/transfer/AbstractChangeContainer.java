package de.osthus.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "AbstractChangeContainer", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ CreateContainer.class, UpdateContainer.class, DeleteContainer.class })
public abstract class AbstractChangeContainer implements IChangeContainer, IPrintable
{
	@XmlElement(required = true)
	protected IObjRef reference;

	@Override
	public IObjRef getReference()
	{
		return reference;
	}

	@Override
	public void setReference(IObjRef reference)
	{
		this.reference = reference;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getSimpleName()).append(": ");
		StringBuilderUtil.appendPrintable(sb, reference);
	}
}
