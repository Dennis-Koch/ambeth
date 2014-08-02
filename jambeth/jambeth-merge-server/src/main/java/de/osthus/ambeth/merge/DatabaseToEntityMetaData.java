package de.osthus.ambeth.merge;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.database.IDatabaseMappedListener;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;

public class DatabaseToEntityMetaData implements IDatabaseMappedListener, IDisposableBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheRetrieverExtendable cacheRetrieverExtendable;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	@Autowired
	protected IMergeServiceExtensionExtendable mergeServiceExtensionExtendable;

	@Autowired
	protected ICacheRetriever persistenceCacheRetriever;

	@Autowired
	protected IMergeServiceExtension persistenceMergeServiceExtension;

	protected final ArrayList<IEntityMetaData> registeredMetaDatas = new ArrayList<IEntityMetaData>();

	protected final ArrayList<IEntityMetaData> handledMetaDatas = new ArrayList<IEntityMetaData>();

	protected boolean firstMapping = true;

	@Override
	public void destroy() throws Throwable
	{
		for (int a = handledMetaDatas.size(); a-- > 0;)
		{
			IEntityMetaData handledMetaData = handledMetaDatas.get(a);
			cacheRetrieverExtendable.unregisterCacheRetriever(persistenceCacheRetriever, handledMetaData.getEntityType());
			mergeServiceExtensionExtendable.unregisterMergeServiceExtension(persistenceMergeServiceExtension, handledMetaData.getEntityType());
		}
		for (int a = registeredMetaDatas.size(); a-- > 0;)
		{
			IEntityMetaData registeredMetaData = registeredMetaDatas.remove(a);
			entityMetaDataExtendable.unregisterEntityMetaData(registeredMetaData);
		}
	}

	@Override
	public synchronized void databaseMapped(IDatabase database)
	{
		if (!firstMapping)
		{
			return;
		}
		firstMapping = false;
		HashSet<IField> alreadyHandledFields = new HashSet<IField>();
		List<IEntityMetaData> newMetaDatas = new ArrayList<IEntityMetaData>();
		List<IEntityMetaData> newRegisteredMetaDatas = new ArrayList<IEntityMetaData>();
		for (ITable table : database.getTables())
		{
			Class<?> entityType = table.getEntityType();
			if (entityType == null || table.isArchive())
			{
				continue;
			}
			EntityMetaData metaData = (EntityMetaData) entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null)
			{
				metaData = new EntityMetaData();
				metaData.setEntityType(entityType);
				newRegisteredMetaDatas.add(metaData);
			}
			// metaData.setEntityType(entityType);
			// metaData.setRealType(null);
			if (isMemberOnFieldBetter(entityType, metaData.getIdMember(), table.getIdField()))
			{
				metaData.setIdMember((PrimitiveMember) table.getIdField().getMember());
			}
			alreadyHandledFields.add(table.getIdField());
			IField versionField = table.getVersionField();
			if (versionField != null)
			{
				alreadyHandledFields.add(versionField);
				if (isMemberOnFieldBetter(entityType, metaData.getVersionMember(), versionField))
				{
					metaData.setVersionMember((PrimitiveMember) versionField.getMember());
				}
			}

			if (table.getCreatedOnField() != null)
			{
				if (isMemberOnFieldBetter(entityType, metaData.getCreatedOnMember(), table.getCreatedOnField()))
				{
					metaData.setCreatedOnMember((PrimitiveMember) table.getCreatedOnField().getMember());
				}
			}
			if (table.getCreatedByField() != null)
			{
				if (isMemberOnFieldBetter(entityType, metaData.getCreatedByMember(), table.getCreatedByField()))
				{
					metaData.setCreatedByMember((PrimitiveMember) table.getCreatedByField().getMember());
				}
			}
			if (table.getUpdatedOnField() != null)
			{
				if (isMemberOnFieldBetter(entityType, metaData.getUpdatedOnMember(), table.getUpdatedOnField()))
				{
					metaData.setUpdatedOnMember((PrimitiveMember) table.getUpdatedOnField().getMember());
				}
			}
			if (table.getUpdatedByField() != null)
			{
				if (isMemberOnFieldBetter(entityType, metaData.getUpdatedByMember(), table.getUpdatedByField()))
				{
					metaData.setUpdatedByMember((PrimitiveMember) table.getUpdatedByField().getMember());
				}
			}

			IField[] alternateIdFields = table.getAlternateIdFields();
			PrimitiveMember[] alternateIdMembers = new PrimitiveMember[alternateIdFields.length];
			for (int b = alternateIdFields.length; b-- > 0;)
			{
				IField alternateIdField = alternateIdFields[b];
				alternateIdMembers[b] = (PrimitiveMember) alternateIdField.getMember();
			}
			ArrayList<Member> fulltextMembers = new ArrayList<Member>();
			HashSet<Member> primitiveMembers = new HashSet<Member>();
			HashSet<RelationMember> relationMembers = new HashSet<RelationMember>();

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
				Member member = field.getMember();
				if (member != null)
				{
					fulltextMembers.add(member);
				}
			}

			List<IField> fields = table.getPrimitiveFields();
			for (int a = 0; a < fields.size(); a++)
			{
				IField field = fields.get(a);

				Member member = field.getMember();
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

			IList<RelationMember> relationMembersList = relationMembers.toList();
			Collections.sort(relationMembersList, new Comparator<RelationMember>()
			{

				@Override
				public int compare(RelationMember o1, RelationMember o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});

			// Order of setter calls is important
			// primitiveMembers.addAll(metaData.getPrimitiveMembers());
			// for (ITypeInfoItem primitiveMember : metaData.getPrimitiveMembers())
			// {
			// primitiveMember.
			// }
			if (metaData.getCreatedByMember() != null && !primitiveMembers.contains(metaData.getCreatedByMember()))
			{
				metaData.setCreatedByMember(null);
			}
			if (metaData.getCreatedOnMember() != null && !primitiveMembers.contains(metaData.getCreatedOnMember()))
			{
				metaData.setCreatedOnMember(null);
			}
			if (metaData.getUpdatedByMember() != null && !primitiveMembers.contains(metaData.getUpdatedByMember()))
			{
				metaData.setUpdatedByMember(null);
			}
			if (metaData.getUpdatedOnMember() != null && !primitiveMembers.contains(metaData.getUpdatedOnMember()))
			{
				metaData.setUpdatedOnMember(null);
			}
			metaData.setPrimitiveMembers(primitiveMembers.toArray(PrimitiveMember.class));
			metaData.setFulltextMembers(fulltextMembers.toArray(PrimitiveMember.class));
			metaData.setAlternateIdMembers(alternateIdMembers);
			metaData.setRelationMembers(relationMembersList.toArray(RelationMember.class));

			metaData.initialize(entityFactory);
			newMetaDatas.add(metaData);
		}
		for (int a = 0, size = newRegisteredMetaDatas.size(); a < size; a++)
		{
			IEntityMetaData newRegisteredMetaData = newRegisteredMetaDatas.get(a);
			entityMetaDataExtendable.registerEntityMetaData(newRegisteredMetaData);
			registeredMetaDatas.add(newRegisteredMetaData);
		}
		for (int a = 0, size = newMetaDatas.size(); a < size; a++)
		{
			IEntityMetaData newMetaData = newMetaDatas.get(a);
			Class<?> entityType = newMetaData.getEntityType();
			mergeServiceExtensionExtendable.registerMergeServiceExtension(persistenceMergeServiceExtension, entityType);
			cacheRetrieverExtendable.registerCacheRetriever(persistenceCacheRetriever, entityType);
			handledMetaDatas.add(newMetaData);
		}
	}

	protected boolean isMemberOnFieldBetter(Class<?> entityType, Member existingMember, IField newMemberField)
	{
		if (newMemberField == null)
		{
			return false;
		}
		Member member = newMemberField.getMember();
		if (member == null)
		{
			return false;
		}
		if (existingMember == null)
		{
			return true;
		}
		if (existingMember == member || existingMember.getName().equals(member.getName()))
		{
			return false;
		}
		throw new IllegalStateException("Inconsistent metadata configuration on member '" + existingMember.getName() + "' of entity '" + entityType.getName()
				+ "'");
	}

	protected boolean isMemberOnMetaDataBetter(Class<?> entityType, Member existingMember, IField newMemberField)
	{
		if (newMemberField == null)
		{
			return false;
		}
		Member member = newMemberField.getMember();
		if (member == null)
		{
			return false;
		}
		if (existingMember == null)
		{
			return false;
		}
		if (existingMember == member || existingMember.getName().equals(member.getName()))
		{
			return false;
		}
		throw new IllegalStateException("Inconsistent metadata configuration on member '" + existingMember.getName() + "' of entity '" + entityType.getName()
				+ "'");
	}
}
