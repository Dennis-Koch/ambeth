package com.koch.ambeth.testutil.datagenerator;

import java.util.HashMap;
import java.util.Map;

import com.koch.ambeth.testutil.datagenerator.setter.StringTestSetter;

public class EntityData
{

	public static final String[] TECHNICAL_ATTRIBUTES = new String[] { "Id", "ID", "CreatedBy", "CreatedOn", "UpdatedBy", "UpdatedOn", "Version" };

	public static String simpleBuidExt(int id)
	{
		return String.format("%06d", id);
	}

	public static String simpleBuid(int id)
	{
		return "BUID" + simpleBuidExt(id);
	}

	/**
	 * Business Object buid is different from DService buid in its PropertyName, so two different methods.
	 * 
	 * @param id
	 * @return "Buid" + id in normalized form
	 */
	public static String simpleBOBuid(int id)
	{
		return "Buid" + simpleBuidExt(id);
	}

	public static Map<Object, Object> getSimpleAttributes(int index)
	{
		Map<Object, Object> attributes = new HashMap<Object, Object>();
		attributes.put(StringTestSetter.class, simpleBuidExt(index));
		return attributes;
	}
}
