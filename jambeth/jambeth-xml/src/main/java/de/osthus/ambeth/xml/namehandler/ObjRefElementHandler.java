package de.osthus.ambeth.xml.namehandler;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class ObjRefElementHandler extends AbstractHandler implements INameBasedHandler
{
	protected static final String idNameIndex = "ix";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!IObjRef.class.isAssignableFrom(type) || IDirectObjRef.class.isAssignableFrom(type))
		{
			return false;
		}
		IObjRef ori = (IObjRef) obj;
		writeOpenElement(ori, writer);
		writer.writeObject(ori.getRealType());
		writer.writeObject(ori.getId());
		writer.writeObject(ori.getVersion());
		writer.writeCloseElement(xmlDictionary.getEntityRefElement());
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!xmlDictionary.getEntityRefElement().equals(elementName))
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
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(realType, true);
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

		IObjRef obj = objRefFactory.createObjRef(realType, idIndex, objId, version);

		return obj;
	}

	protected void writeOpenElement(IObjRef ori, IWriter writer)
	{
		writer.writeStartElement(xmlDictionary.getEntityRefElement());
		int id = writer.acquireIdForObject(ori);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		byte idIndex = ori.getIdNameIndex();
		if (idIndex != ObjRef.PRIMARY_KEY_INDEX)
		{
			writer.writeAttribute(idNameIndex, Byte.toString(ori.getIdNameIndex()));
		}
		writer.writeStartElementEnd();
	}
}
