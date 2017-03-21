package com.koch.ambeth.merge.server;

/*-
 * #%L
 * jambeth-merge-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IEntityMetaDataExtendable;
import com.koch.ambeth.merge.IMergeServiceExtension;
import com.koch.ambeth.merge.IMergeServiceExtensionExtendable;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.persistence.DirectedLinkMetaData;
import com.koch.ambeth.persistence.FieldMetaData;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.database.IDatabaseMappedListener;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataRefresher;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;

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
	protected IEntityMetaDataRefresher entityMetaDataRefresher;

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
	public synchronized void databaseMapped(IDatabaseMetaData database)
	{
		if (!firstMapping)
		{
			return;
		}
		firstMapping = false;
		HashSet<IFieldMetaData> alreadyHandledFields = new HashSet<IFieldMetaData>();
		List<IEntityMetaData> newMetaDatas = new ArrayList<IEntityMetaData>();
		List<IEntityMetaData> newRegisteredMetaDatas = new ArrayList<IEntityMetaData>();
		for (ITableMetaData table : database.getTables())
		{
			mapTable(table, newMetaDatas, newRegisteredMetaDatas, alreadyHandledFields);
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

	protected void mapTable(ITableMetaData table, List<IEntityMetaData> newMetaDatas, List<IEntityMetaData> newRegisteredMetaDatas,
			HashSet<IFieldMetaData> alreadyHandledFields)
	{
		Class<?> entityType = table.getEntityType();
		if (entityType == null || table.isArchive())
		{
			return;
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
		IFieldMetaData[] idFields = table.getIdFields();
		if (idFields.length > 1)
		{
			PrimitiveMember idMember = metaData.getIdMember();
			if (!(idMember instanceof CompositeIdMember))
			{
				throw new IllegalStateException("Not yet handled");
			}
			PrimitiveMember[] members = ((CompositeIdMember) idMember).getMembers();
			for (int a = members.length; a-- > 0;)
			{
				if (isMemberOnFieldBetter(entityType, members[a], idFields[a]))
				{
					metaData.setIdMember((PrimitiveMember) idFields[a].getMember());
				}
			}
		}
		else if (isMemberOnFieldBetter(entityType, metaData.getIdMember(), table.getIdField()))
		{
			metaData.setIdMember((PrimitiveMember) table.getIdField().getMember());
		}
		alreadyHandledFields.add(table.getIdField());
		IFieldMetaData versionField = table.getVersionField();
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

		IFieldMetaData[] alternateIdFields = table.getAlternateIdFields();
		PrimitiveMember[] alternateIdMembers = new PrimitiveMember[alternateIdFields.length];
		for (int b = alternateIdFields.length; b-- > 0;)
		{
			IFieldMetaData alternateIdField = alternateIdFields[b];
			alternateIdMembers[b] = (PrimitiveMember) alternateIdField.getMember();
		}
		ArrayList<Member> fulltextMembers = new ArrayList<Member>();
		HashSet<PrimitiveMember> primitiveMembers = new HashSet<PrimitiveMember>();
		HashSet<RelationMember> relationMembers = new HashSet<RelationMember>();
		HashMap<Member, Object> memberToFieldOrLinkMap = new HashMap<Member, Object>();

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
		List<IFieldMetaData> fulltextFields = table.getFulltextFields();
		for (int a = 0; a < fulltextFields.size(); a++)
		{
			IFieldMetaData field = fulltextFields.get(a);
			Member member = field.getMember();
			if (member != null)
			{
				fulltextMembers.add(member);
				memberToFieldOrLinkMap.put(member, field);
			}
		}

		List<IFieldMetaData> fields = table.getPrimitiveFields();
		for (int a = 0; a < fields.size(); a++)
		{
			IFieldMetaData field = fields.get(a);

			Member member = field.getMember();
			if (member == null)
			{
				continue;
			}
			memberToFieldOrLinkMap.put(member, field);
			if (!alreadyHandledFields.contains(field))
			{
				primitiveMembers.add((PrimitiveMember) member);
			}
		}

		List<IDirectedLinkMetaData> links = table.getLinks();
		for (int a = 0; a < links.size(); a++)
		{
			IDirectedLinkMetaData link = links.get(a);
			RelationMember member = link.getMember();
			if (member == null)
			{
				continue;
			}
			memberToFieldOrLinkMap.put(member, link);
			Class<?> otherType = link.getToEntityType();
			relationMembers.add(member);
			if (link.getReverseLink().isCascadeDelete())
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

		{
			PrimitiveMember[] existingPrimitiveMembers = metaData.getPrimitiveMembers();
			if (existingPrimitiveMembers != null)
			{
				for (PrimitiveMember primitiveMember : existingPrimitiveMembers)
				{
					if (primitiveMember.isTransient())
					{
						// ensure that transient members are not dropped because no persisted column has been found in the database table
						primitiveMembers.add(primitiveMember);
					}
				}
			}
		}

		// Order of setter calls is important
		metaData.setCreatedByMember(findPrimitiveMember(metaData.getCreatedByMember(), primitiveMembers));
		metaData.setCreatedOnMember(findPrimitiveMember(metaData.getCreatedOnMember(), primitiveMembers));
		metaData.setUpdatedByMember(findPrimitiveMember(metaData.getUpdatedByMember(), primitiveMembers));
		metaData.setUpdatedOnMember(findPrimitiveMember(metaData.getUpdatedOnMember(), primitiveMembers));

		for (RelationMember existingRelationMember : metaData.getRelationMembers())
		{
			relationMembers.add(existingRelationMember);
		}
		PrimitiveMember[] primitives = primitiveMembers.toArray(PrimitiveMember.class);
		PrimitiveMember[] fulltexts = fulltextMembers.toArray(PrimitiveMember.class);
		RelationMember[] relations = relationMembers.toArray(RelationMember.class);
		Arrays.sort(primitives);
		Arrays.sort(fulltexts);
		Arrays.sort(relations);
		metaData.setPrimitiveMembers(primitives);
		metaData.setFulltextMembers(fulltexts);
		metaData.setAlternateIdMembers(alternateIdMembers);
		// FIXME To many tests fail with this line
		// checkRelationMember(metaData, relations);
		metaData.setRelationMembers(relations);
		metaData.setEnhancedType(null);

		entityMetaDataRefresher.refreshMembers(metaData);
		HashSet<Member> allRefreshedMembers = new HashSet<Member>();
		addValidMember(allRefreshedMembers, metaData.getIdMember());
		addValidMember(allRefreshedMembers, metaData.getVersionMember());
		allRefreshedMembers.addAll(metaData.getPrimitiveMembers());
		allRefreshedMembers.addAll(metaData.getRelationMembers());

		for (Entry<Member, Object> entry : memberToFieldOrLinkMap)
		{
			Member member = entry.getKey();
			Member refreshedMember = allRefreshedMembers.get(member);
			if (refreshedMember == null)
			{
				throw new IllegalStateException("Member '" + member.getName() + "' has not been refreshed");
			}
			Object value = entry.getValue();
			if (value instanceof FieldMetaData)
			{
				((FieldMetaData) value).setMember(refreshedMember);
			}
			else if (value instanceof DirectedLinkMetaData)
			{
				((DirectedLinkMetaData) value).setMember((RelationMember) refreshedMember);
			}
		}
		newMetaDatas.add(metaData);
	}

	protected void checkRelationMember(EntityMetaData metaData, RelationMember[] relationsNew)
	{
		RelationMember[] relationsOld = metaData.getRelationMembers();
		if (!Arrays.equals(relationsOld, relationsNew))
		{
			throw new IllegalStateException("Relation member arrays for entity '" + metaData.getEntityType().getName() + "' do not match. From object model: "
					+ Arrays.deepToString(relationsOld) + ", from database model: " + Arrays.deepToString(relationsNew));
		}
	}

	protected void addValidMember(ISet<Member> set, Member member)
	{
		if (member == null)
		{
			return;
		}
		set.add(member);
	}

	protected PrimitiveMember findPrimitiveMember(PrimitiveMember memberToFind, ISet<PrimitiveMember> members)
	{
		if (memberToFind == null)
		{
			return null;
		}
		for (Member member : members)
		{
			if (memberToFind.getName().equals(member.getName()))
			{
				return (PrimitiveMember) member;
			}
		}
		return memberToFind;
	}

	protected boolean isMemberOnFieldBetter(Class<?> entityType, Member existingMember, IFieldMetaData newMemberField)
	{
		if (newMemberField == null)
		{
			return false;
		}
		Member member = newMemberField.getMember();
		if (member == null)
		{
			((FieldMetaData) newMemberField).setMember(existingMember);
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

	protected boolean isMemberOnMetaDataBetter(Class<?> entityType, Member existingMember, IFieldMetaData newMemberField)
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

	@Override
	public void newTableMetaData(ITableMetaData newTable)
	{

		HashSet<IFieldMetaData> alreadyHandledFields = new HashSet<IFieldMetaData>();
		List<IEntityMetaData> newMetaDatas = new ArrayList<IEntityMetaData>();
		List<IEntityMetaData> newRegisteredMetaDatas = new ArrayList<IEntityMetaData>();

		mapTable(newTable, newMetaDatas, newRegisteredMetaDatas, alreadyHandledFields);

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
}
