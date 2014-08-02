package de.osthus.ambeth.xml.namehandler;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.pending.ObjRefFuture;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class OriWrapperElementHandler extends AbstractHandler implements INameBasedHandler
{
	@LogInstance
	private ILogger log;

	protected static final String idNameIndex = "ix";

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

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
					Member idMember = metaData.getIdMemberByIdIndex(idIndex);
					if (objId.equals(idMember.getNullEquivalentValue()))
					{
						objId = null;
					}
				}
				if (version != null)
				{
					Member versionMember = metaData.getVersionMember();
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

		ObjRef ori = new ObjRef(realType, idIndex, objId, version);

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
