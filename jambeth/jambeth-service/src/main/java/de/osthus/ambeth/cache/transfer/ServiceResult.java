package de.osthus.ambeth.cache.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlRootElement(name = "ServiceResult", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceResult implements IServiceResult
{
	@XmlElement
	protected List<IObjRef> objRefs;

	@XmlElement
	protected Object additionalInformation;

	public ServiceResult()
	{
		// Intended blank
	}

	public ServiceResult(List<IObjRef> objRefs)
	{
		this.objRefs = objRefs;
	}

	@Override
	public List<IObjRef> getObjRefs()
	{
		return objRefs;
	}

	public void setObjRefs(List<IObjRef> objRefs)
	{
		this.objRefs = objRefs;
	}

	@Override
	public Object getAdditionalInformation()
	{
		return additionalInformation;
	}

	public void setAdditionalInformation(Object additionalInformation)
	{
		this.additionalInformation = additionalInformation;
	}
}
