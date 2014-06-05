package de.osthus.ambeth.merge;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.database.IDatabaseMappedListener;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public class EntityMetaDataServer extends EntityMetaDataProvider implements IDatabaseMappedListener
{
	@LogInstance
	private ILogger log;

	private static final Class<?>[] EMPTY_TYPES = new Class[0];

	protected boolean firstMapping = true;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Override
	public synchronized void databaseMapped(IDatabase database)
	{
		if (!firstMapping)
		{
			return;
		}
		firstMapping = false;
		HashSet<IField> alreadyHandledFields = new HashSet<IField>();
		List<Class<?>> newEntityTypes = new ArrayList<Class<?>>();
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (ITable table : database.getTables())
			{
				Class<?> entityType = table.getEntityType();
				if (entityType == null || table.isArchive())
				{
					continue;
				}
				EntityMetaData metaData = new EntityMetaData();
				metaData.setEntityType(entityType);
				metaData.setRealType(null);
				metaData.setIdMember(table.getIdField().getMember());
				alreadyHandledFields.add(table.getIdField());
				IField versionField = table.getVersionField();
				if (versionField != null)
				{
					alreadyHandledFields.add(table.getVersionField());
					metaData.setVersionMember(versionField.getMember());
				}

				if (table.getCreatedOnField() != null)
				{
					metaData.setCreatedOnMember(table.getCreatedOnField().getMember());
				}
				if (table.getCreatedByField() != null)
				{
					metaData.setCreatedByMember(table.getCreatedByField().getMember());
				}
				if (table.getUpdatedOnField() != null)
				{
					metaData.setUpdatedOnMember(table.getUpdatedOnField().getMember());
				}
				if (table.getUpdatedByField() != null)
				{
					metaData.setUpdatedByMember(table.getUpdatedByField().getMember());
				}

				IField[] alternateIdFields = table.getAlternateIdFields();
				ITypeInfoItem[] alternateIdMembers = new ITypeInfoItem[alternateIdFields.length];
				for (int b = alternateIdFields.length; b-- > 0;)
				{
					IField alternateIdField = alternateIdFields[b];
					alternateIdMembers[b] = alternateIdField.getMember();
				}
				ArrayList<ITypeInfoItem> fulltextMembers = new ArrayList<ITypeInfoItem>();
				ArrayList<ITypeInfoItem> primitiveMembers = new ArrayList<ITypeInfoItem>();
				HashSet<IRelationInfoItem> relationMembers = new HashSet<IRelationInfoItem>();

				// IField[] fulltextFieldsContains = table.getFulltextFieldsContains();
				// for (int a = 0; a < fulltextFieldsContains.length; a++)
				// {
				// IField field = fulltextFieldsContains[a];
				// ITypeInfoItem member = field.getMember();
				// if (member != null)
				// {
				// fulltextMembers.add(member);
				// }
				// }
				// IField[] fulltextFieldsCatsearch = table.getFulltextFieldsCatsearch();
				// for (int a = 0; a < fulltextFieldsCatsearch.length; a++)
				// {
				// IField field = fulltextFieldsCatsearch[a];
				List<IField> fulltextFields = table.getFulltextFields();
				for (int a = 0; a < fulltextFields.size(); a++)
				{
					IField field = fulltextFields.get(a);
					ITypeInfoItem member = field.getMember();
					if (member != null)
					{
						fulltextMembers.add(member);
					}
				}

				List<IField> fields = table.getPrimitiveFields();
				for (int a = 0; a < fields.size(); a++)
				{
					IField field = fields.get(a);

					ITypeInfoItem member = field.getMember();
					if (member == null)
					{
						continue;
					}

					if (!alreadyHandledFields.contains(field))
					{
						primitiveMembers.add(member);
					}
				}

				List<IDirectedLink> links = table.getLinks();
				for (int a = 0; a < links.size(); a++)
				{
					IDirectedLink link = links.get(a);
					if (link.getMember() == null)
					{
						continue;
					}
					Class<?> otherType = link.getToEntityType();
					relationMembers.add(link.getMember());
					if (link.getReverse().isCascadeDelete())
					{
						metaData.addCascadeDeleteType(otherType);
					}
				}

				IList<IRelationInfoItem> relationMembersList = relationMembers.toList();
				Collections.sort(relationMembersList, new Comparator<IRelationInfoItem>()
				{

					@Override
					public int compare(IRelationInfoItem o1, IRelationInfoItem o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
				});

				// Order of setter calls is important
				metaData.setPrimitiveMembers(primitiveMembers.toArray(ITypeInfoItem.class));
				metaData.setFulltextMembers(fulltextMembers.toArray(ITypeInfoItem.class));
				metaData.setAlternateIdMembers(alternateIdMembers);
				metaData.setRelationMembers(relationMembersList.toArray(IRelationInfoItem.class));

				register(metaData, metaData.getEntityType());
				newEntityTypes.add(metaData.getEntityType());
			}
			initialize();
		}
		finally
		{
			writeLock.unlock();
		}
		if (newEntityTypes.size() > 0)
		{
			eventDispatcher.dispatchEvent(new EntityMetaDataAddedEvent(newEntityTypes.toArray(new Class<?>[newEntityTypes.size()])));
		}
	}

	@Override
	public Class<?>[] getEntityPersistOrder()
	{
		return EMPTY_TYPES;
	}
}
