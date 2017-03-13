package com.koch.ambeth.xml.namehandler;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.pending.ObjRefFuture;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class ObjRefWrapperElementHandler extends AbstractHandler implements INameBasedHandler
{
	@LogInstance
	private ILogger log;

	protected static final String idNameIndex = "ix";

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type, true);
		if (metaData == null)
		{
			return false;
		}

		int idValue = writer.getIdOfObject(obj);
		if (idValue != 0)
		{
			writer.writeStartElement(xmlDictionary.getRefElement());
			writer.writeAttribute(xmlDictionary.getIdAttribute(), idValue);
			writer.writeEndElement();
		}
		else
		{
			writer.addSubstitutedEntity(obj);
			IObjRef ori = oriHelper.entityToObjRef(obj, true);
			writeOpenElement(ori, obj, writer);
			writer.writeObject(ori.getRealType());
			writer.writeObject(ori.getId());
			writer.writeObject(ori.getVersion());
			writer.writeCloseElement(xmlDictionary.getOriWrapperElement());
		}

		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!xmlDictionary.getOriWrapperElement().equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}

		String idIndexValue = reader.getAttributeValue(idNameIndex);
		byte idIndex = idIndexValue != null ? Byte.parseByte(idIndexValue) : ObjRef.PRIMARY_KEY_INDEX;
		reader.nextTag();
		Class<?> realType = (Class<?>) reader.readObject();
		Object objId = reader.readObject();
		Object version = reader.readObject();

		if (objId != null || version != null)
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(realType);
			if (metaData != null)
			{
				if (objId != null)
				{
					PrimitiveMember idMember = metaData.getIdMemberByIdIndex(idIndex);
					if (objId.equals(idMember.getNullEquivalentValue()))
					{
						objId = null;
					}
				}
				if (version != null)
				{
					PrimitiveMember versionMember = metaData.getVersionMember();
					if (versionMember != null)
					{
						if (version.equals(versionMember.getNullEquivalentValue()))
						{
							version = null;
						}
					}
				}
			}
		}

		IObjRef ori = objRefFactory.createObjRef(realType, idIndex, objId, version);

		Object obj = new ObjRefFuture(ori);

		return obj;
	}

	protected void writeOpenElement(IObjRef ori, Object obj, IWriter writer)
	{
		writer.writeStartElement(xmlDictionary.getOriWrapperElement());
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		byte idIndex = ori.getIdNameIndex();
		if (idIndex != ObjRef.PRIMARY_KEY_INDEX)
		{
			writer.writeAttribute(idNameIndex, Byte.toString(ori.getIdNameIndex()));
		}
		writer.writeStartElementEnd();
	}
}
