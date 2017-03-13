package com.koch.ambeth.xml;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class CyclicXmlDictionary implements ICyclicXmlDictionary, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public String getArrayElement()
	{
		return "a";
	}

	@Override
	public String getBlobElement()
	{
		return "b";
	}

	@Override
	public String getClassElement()
	{
		return "c";
	}

	@Override
	public String getEnumElement()
	{
		return "e";
	}

	@Override
	public String getEntityRefElement()
	{
		return "or";
	}

	@Override
	public String getExceptionElement()
	{
		return "ex";
	}

	@Override
	public String getListElement()
	{
		return "l";
	}

	@Override
	public String getNullElement()
	{
		return "n";
	}

	@Override
	public String getUnspecifiedElement()
	{
		return "u";
	}

	@Override
	public String getNumberElement()
	{
		return "nu";
	}

	@Override
	public String getObjectElement()
	{
		return "o";
	}

	@Override
	public String getOriWrapperElement()
	{
		return "ow";
	}

	@Override
	public String getPostProcessElement()
	{
		return "pp";
	}

	@Override
	public String getPrimitiveElement()
	{
		return "p";
	}

	@Override
	public String getRefElement()
	{
		return "r";
	}

	@Override
	public String getRootElement()
	{
		return "root";
	}

	@Override
	public String getSetElement()
	{
		return "set";
	}

	@Override
	public String getSimpleObjectElement()
	{
		return "v";
	}

	@Override
	public String getStringElement()
	{
		return "s";
	}

	@Override
	public String getTypeElement()
	{
		return "type";
	}

	@Override
	public String getArrayDimensionAttribute()
	{
		return "dim";
	}

	@Override
	public String getClassNameAttribute()
	{
		return "n";
	}

	@Override
	public String getClassNamespaceAttribute()
	{
		return "ns";
	}

	@Override
	public String getClassMemberAttribute()
	{
		return "m";
	}

	@Override
	public String getEntityRefKeyAttribute()
	{
		return "key";
	}

	@Override
	public String getEntityRefKeyIndexAttribute()
	{
		return "index";
	}

	@Override
	public String getEntityRefVersionAttribute()
	{
		return "version";
	}

	@Override
	public String getClassIdAttribute()
	{
		return "ti";
	}

	@Override
	public String getIdAttribute()
	{
		return "i";
	}

	@Override
	public String getSizeAttribute()
	{
		return "s";
	}

	@Override
	public String getValueAttribute()
	{
		return "v";
	}
}
