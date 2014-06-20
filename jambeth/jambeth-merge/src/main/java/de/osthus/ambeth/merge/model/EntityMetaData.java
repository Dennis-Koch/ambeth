package de.osthus.ambeth.merge.model;

import java.util.Arrays;
import java.util.Set;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.SmartCopySet;
import de.osthus.ambeth.compositeid.CompositeIdTypeInfoItem;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public class EntityMetaData implements IEntityMetaData
{
	public static final String DEFAULT_NAME_ID = "Id";

	public static final String DEFAULT_NAME_VERSION = "Version";

	public static final String DEFAULT_NAME_CREATED_BY = "CreatedBy";

	public static final String DEFAULT_NAME_CREATED_ON = "CreatedOn";

	public static final String DEFAULT_NAME_UPDATED_BY = "UpdatedBy";

	public static final String DEFAULT_NAME_UPDATED_ON = "UpdatedOn";

	public static final int[][] emptyShortArray = new int[0][0];

	public static final Class<?>[] emptyTypes = new Class<?>[0];

	public static final ITypeInfoItem[] emptyTypeInfoItems = new ITypeInfoItem[0];

	public static final IRelationInfoItem[] emptyRelationInfoItems = new IRelationInfoItem[0];

	public static final IEntityLifecycleExtension[] emptyEntityLifecycleExtensions = new IEntityLifecycleExtension[0];

	protected Class<?> entityType;

	protected Class<?> realType;

	protected Class<?> enhancedType;

	protected boolean localEntity = true;

	protected Class<?>[] typesRelatingToThis = emptyTypes;

	protected final HashSet<Class<?>> typesRelatingToThisSet = new HashSet<Class<?>>(0.5f);

	protected final HashSet<Class<?>> cascadeDeleteTypes = new HashSet<Class<?>>(0.5f);

	protected IEntityLifecycleExtension[] entityLifecycleExtensions = emptyEntityLifecycleExtensions;

	protected IRelationInfoItem[] relationMembers = emptyRelationInfoItems;

	protected ITypeInfoItem[] primitiveMembers = emptyTypeInfoItems;

	protected ITypeInfoItem[] alternateIdMembers = emptyTypeInfoItems;

	protected ITypeInfoItem[] fulltextMembers = emptyTypeInfoItems;

	protected ITypeInfoItem versionMember;

	protected ITypeInfoItem idMember;

	protected ITypeInfoItem createdOn;

	protected ITypeInfoItem createdBy;

	protected ITypeInfoItem updatedOn;

	protected ITypeInfoItem updatedBy;

	protected int[][] alternateIdMemberIndicesInPrimitives = emptyShortArray;

	protected final HashSet<ITypeInfoItem> fulltextMemberSet = new HashSet<ITypeInfoItem>(0.5f);

	protected final HashSet<ITypeInfoItem> alternateIdMemberSet = new HashSet<ITypeInfoItem>(0.5f);

	protected final SmartCopySet<ITypeInfoItem> interningMemberSet = new SmartCopySet<ITypeInfoItem>(0.5f);

	protected final HashMap<String, ITypeInfoItem> nameToMemberDict = new HashMap<String, ITypeInfoItem>(0.5f);

	protected final HashMap<ITypeInfoItem, Boolean> memberToMergeRelevanceDict = new HashMap<ITypeInfoItem, Boolean>(0.5f);

	protected final HashMap<String, Byte> memberNameToIdIndexDict = new HashMap<String, Byte>(0.5f);

	protected final HashMap<String, Integer> relMemberNameToIndexDict = new HashMap<String, Integer>(0.5f);

	protected final HashMap<String, Integer> primMemberNameToIndexDict = new HashMap<String, Integer>(0.5f);

	protected final IdentityHashMap<IRelationInfoItem, Integer> relMemberToIndexDict = new IdentityHashMap<IRelationInfoItem, Integer>(0.5f);

	protected final IdentityHashMap<ITypeInfoItem, Integer> primMemberToIndexDict = new IdentityHashMap<ITypeInfoItem, Integer>(0.5f);

	protected IEntityFactory entityFactory;

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setRealType(Class<?> realType)
	{
		this.realType = realType;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	public void setEnhancedType(Class<?> enhancedType)
	{
		this.enhancedType = enhancedType;
	}

	@Override
	public Class<?> getEnhancedType()
	{
		return enhancedType;
	}

	@Override
	public boolean isLocalEntity()
	{
		return localEntity;
	}

	public void setLocalEntity(boolean localEntity)
	{
		this.localEntity = localEntity;
	}

	@Override
	public ITypeInfoItem getIdMember()
	{
		return idMember;
	}

	public void setIdMember(ITypeInfoItem idMember)
	{
		this.idMember = idMember;
	}

	@Override
	public ITypeInfoItem getIdMemberByIdIndex(byte idIndex)
	{
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			return idMember;
		}
		return getAlternateIdMembers()[idIndex];
	}

	@Override
	public ITypeInfoItem getVersionMember()
	{
		return versionMember;
	}

	public void setVersionMember(ITypeInfoItem versionMember)
	{
		this.versionMember = versionMember;
	}

	@Override
	public ITypeInfoItem[] getAlternateIdMembers()
	{
		return alternateIdMembers;
	}

	public void setAlternateIdMembers(ITypeInfoItem[] alternateIdMembers)
	{
		this.alternateIdMembers = alternateIdMembers;
		alternateIdMemberSet.clear();
		if (alternateIdMembers != null)
		{
			alternateIdMemberSet.addAll(alternateIdMembers);
		}
	}

	@Override
	public int[][] getAlternateIdMemberIndicesInPrimitives()
	{
		return alternateIdMemberIndicesInPrimitives;
	}

	@Override
	public int getAlternateIdCount()
	{
		return alternateIdMembers.length;
	}

	@Override
	public boolean isAlternateId(ITypeInfoItem primitiveMember)
	{
		return alternateIdMemberSet.contains(primitiveMember);
	}

	@Override
	public byte getIdIndexByMemberName(String memberName)
	{
		Byte value = memberNameToIdIndexDict.get(memberName);
		if (value == null)
		{
			throw new IllegalArgumentException("No alternate id index found for member name '" + memberName + "'");
		}
		return value.byteValue();
	}

	@Override
	public boolean hasInterningBehavior(ITypeInfoItem primitiveMember)
	{
		return interningMemberSet.contains(primitiveMember);
	}

	@Override
	public void changeInterningBehavior(ITypeInfoItem primitiveMember, boolean state)
	{
		if (state)
		{
			interningMemberSet.add(primitiveMember);
		}
		else
		{
			interningMemberSet.remove(primitiveMember);
		}
	}

	@Override
	public ITypeInfoItem getCreatedOnMember()
	{
		return createdOn;
	}

	public void setCreatedOnMember(ITypeInfoItem createdOn)
	{
		this.createdOn = createdOn;
	}

	@Override
	public ITypeInfoItem getCreatedByMember()
	{
		return createdBy;
	}

	public void setCreatedByMember(ITypeInfoItem createdBy)
	{
		this.createdBy = createdBy;
	}

	@Override
	public ITypeInfoItem getUpdatedOnMember()
	{
		return updatedOn;
	}

	public void setUpdatedOnMember(ITypeInfoItem updatedOn)
	{
		this.updatedOn = updatedOn;
	}

	@Override
	public ITypeInfoItem getUpdatedByMember()
	{
		return updatedBy;
	}

	public void setUpdatedByMember(ITypeInfoItem updatedBy)
	{
		this.updatedBy = updatedBy;
	}

	@Override
	public ITypeInfoItem[] getFulltextMembers()
	{
		return fulltextMembers;
	}

	public void setFulltextMembers(ITypeInfoItem[] fulltextMembers)
	{
		this.fulltextMembers = fulltextMembers;
	}

	@Override
	public ITypeInfoItem[] getPrimitiveMembers()
	{
		return primitiveMembers;
	}

	public void setPrimitiveMembers(ITypeInfoItem[] primitiveMembers)
	{
		this.primitiveMembers = primitiveMembers;
	}

	@Override
	public IRelationInfoItem[] getRelationMembers()
	{
		return relationMembers;
	}

	public void setRelationMembers(IRelationInfoItem[] relationMembers)
	{
		this.relationMembers = relationMembers;
	}

	public Set<Class<?>> getCascadeDeleteTypes()
	{
		return cascadeDeleteTypes;
	}

	@Override
	public boolean isFulltextRelevant(ITypeInfoItem member)
	{
		return fulltextMemberSet.contains(member);
	}

	@Override
	public boolean isMergeRelevant(ITypeInfoItem member)
	{
		Boolean relevance = memberToMergeRelevanceDict.get(member);
		return relevance == null || relevance.booleanValue();
	}

	public void setMergeRelevant(ITypeInfoItem member, boolean relevant)
	{
		memberToMergeRelevanceDict.put(member, Boolean.valueOf(relevant));
	}

	@Override
	public ITypeInfoItem getMemberByName(String memberName)
	{
		return nameToMemberDict.get(memberName);
	}

	@Override
	public int getIndexByRelationName(String relationMemberName)
	{
		Integer index = relMemberNameToIndexDict.get(relationMemberName);
		if (index == null)
		{
			throw new IllegalArgumentException("No index found for relation member: " + relationMemberName);
		}
		return index.intValue();
	}

	@Override
	public int getIndexByRelation(IRelationInfoItem relationMember)
	{
		Integer index = relMemberToIndexDict.get(relationMember);
		if (index == null)
		{
			throw new IllegalArgumentException("No index found for relation member: " + relationMember);
		}
		return index.intValue();
	}

	@Override
	public int getIndexByPrimitiveName(String primitiveMemberName)
	{
		Integer index = primMemberNameToIndexDict.get(primitiveMemberName);
		if (index == null)
		{
			throw new IllegalArgumentException("No index found for primitive member: " + primitiveMemberName);
		}
		return index.intValue();
	}

	@Override
	public int getIndexByPrimitive(ITypeInfoItem primitiveMember)
	{
		Integer index = primMemberToIndexDict.get(primitiveMember);
		if (index == null)
		{
			throw new IllegalArgumentException("No index found for primitive member: " + primitiveMember);
		}
		return index.intValue();
	}

	@Override
	public Class<?>[] getTypesRelatingToThis()
	{
		return typesRelatingToThis;
	}

	@Override
	public boolean isRelatingToThis(Class<?> childType)
	{
		return typesRelatingToThisSet.contains(childType);
	}

	public void setTypesRelatingToThis(Class<?>[] typesRelatingToThis)
	{
		this.typesRelatingToThis = typesRelatingToThis;
	}

	@Override
	public boolean isCascadeDelete(Class<?> other)
	{
		return cascadeDeleteTypes.contains(other);
	}

	public void addCascadeDeleteType(Class<?> type)
	{
		cascadeDeleteTypes.add(type);
	}

	@Override
	public void postLoad(Object entity)
	{
		for (IEntityLifecycleExtension entityLifecycleExtension : entityLifecycleExtensions)
		{
			entityLifecycleExtension.postLoad(entity);
		}
	}

	@Override
	public void prePersist(Object entity)
	{
		for (IEntityLifecycleExtension entityLifecycleExtension : entityLifecycleExtensions)
		{
			entityLifecycleExtension.prePersist(entity);
		}
	}

	public IEntityLifecycleExtension[] getEntityLifecycleExtensions()
	{
		return entityLifecycleExtensions;
	}

	public void setEntityLifecycleExtensions(IEntityLifecycleExtension[] entityLifecycleExtensions)
	{
		if (entityLifecycleExtensions == null || entityLifecycleExtensions.length == 0)
		{
			entityLifecycleExtensions = emptyEntityLifecycleExtensions;
		}
		this.entityLifecycleExtensions = entityLifecycleExtensions;
	}

	@SuppressWarnings("unchecked")
	public void initialize(IEntityFactory entityFactory)
	{
		this.entityFactory = entityFactory;
		if (primitiveMembers == null)
		{
			primitiveMembers = emptyTypeInfoItems;
		}
		else
		{
			// Arrays.sort(primitiveMembers, typeInfoItemComparator);
		}

		if (relationMembers == null)
		{
			relationMembers = emptyRelationInfoItems;
		}
		else
		{
			// Arrays.sort(relationMembers, typeInfoItemComparator);
		}

		if (alternateIdMembers == null)
		{
			alternateIdMembers = emptyTypeInfoItems;
		}
		else
		{
			// Arrays.sort(alternateIdMembers, typeInfoItemComparator);
		}

		if (fulltextMembers == null)
		{
			fulltextMembers = emptyTypeInfoItems;
		}

		fulltextMemberSet.clear();
		for (int a = fulltextMembers.length; a-- > 0;)
		{
			fulltextMemberSet.add(fulltextMembers[a]);
		}
		nameToMemberDict.clear();
		relMemberToIndexDict.clear();
		relMemberNameToIndexDict.clear();
		primMemberToIndexDict.clear();
		primMemberNameToIndexDict.clear();
		if (getIdMember() != null)
		{
			nameToMemberDict.put(getIdMember().getName(), idMember);
			idMember.setTechnicalMember(true);
		}
		if (versionMember != null)
		{
			nameToMemberDict.put(versionMember.getName(), versionMember);
			versionMember.setTechnicalMember(true);
		}
		if (createdBy != null)
		{
			createdBy.setTechnicalMember(true);
		}
		if (createdOn != null)
		{
			createdOn.setTechnicalMember(true);
		}
		if (updatedBy != null)
		{
			updatedBy.setTechnicalMember(true);
		}
		if (updatedOn != null)
		{
			updatedOn.setTechnicalMember(true);
		}
		for (int a = primitiveMembers.length; a-- > 0;)
		{
			ITypeInfoItem member = primitiveMembers[a];
			if (nameToMemberDict.put(member.getName(), member) != null)
			{
				throw new IllegalArgumentException("Duplicate property: " + entityType.getName() + "." + member.getName());
			}
			primMemberNameToIndexDict.put(member.getName(), Integer.valueOf(a));
			primMemberToIndexDict.put(member, Integer.valueOf(a));

			if (member == getIdMember() || member == getVersionMember() || member == getUpdatedByMember() || member == getUpdatedOnMember()
					|| member == getCreatedByMember() || member == getCreatedOnMember())
			{
				// technical members must never be merge relevant
				setMergeRelevant(member, false);
			}
		}
		for (int a = relationMembers.length; a-- > 0;)
		{
			IRelationInfoItem member = relationMembers[a];
			if (nameToMemberDict.put(member.getName(), member) != null)
			{
				throw new IllegalArgumentException("Duplicate relation property: " + entityType.getName() + "." + member.getName());
			}
			relMemberNameToIndexDict.put(member.getName(), Integer.valueOf(a));
			relMemberToIndexDict.put(member, Integer.valueOf(a));
		}
		memberNameToIdIndexDict.clear();
		if (getIdMember() != null)
		{
			memberNameToIdIndexDict.put(getIdMember().getName(), Byte.valueOf(ObjRef.PRIMARY_KEY_INDEX));
		}
		alternateIdMemberIndicesInPrimitives = new int[alternateIdMembers.length][];

		for (int idIndex = alternateIdMembers.length; idIndex-- > 0;)
		{
			int[] compositeIndex = null;
			ITypeInfoItem alternateIdMember = alternateIdMembers[idIndex];
			ITypeInfoItem[] memberItems;
			if (alternateIdMember instanceof CompositeIdTypeInfoItem)
			{
				memberItems = ((CompositeIdTypeInfoItem) alternateIdMember).getMembers();
			}
			else
			{
				memberItems = new ITypeInfoItem[] { alternateIdMember };
			}
			compositeIndex = new int[memberItems.length];

			for (int compositePosition = compositeIndex.length; compositePosition-- > 0;)
			{
				ITypeInfoItem memberItem = memberItems[compositePosition];
				for (int primitiveIndex = primitiveMembers.length; primitiveIndex-- > 0;)
				{
					if (memberItem == primitiveMembers[primitiveIndex])
					{
						compositeIndex[compositePosition] = primitiveIndex;
						break;
					}
				}
				if (compositeIndex[compositePosition] == -1)
				{
					throw new IllegalStateException("AlternateId is not a primitive: " + memberItem);
				}
			}
			alternateIdMemberIndicesInPrimitives[idIndex] = compositeIndex;
			memberNameToIdIndexDict.put(alternateIdMember.getName(), Byte.valueOf((byte) idIndex));
		}
		if (typesRelatingToThis != null && typesRelatingToThis.length > 0)
		{
			typesRelatingToThisSet.addAll(Arrays.asList(typesRelatingToThis));
		}
		if (getCreatedByMember() != null)
		{
			changeInterningBehavior(getCreatedByMember(), true);
		}
		if (getUpdatedByMember() != null)
		{
			changeInterningBehavior(getUpdatedByMember(), true);
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + getEntityType().getName();
	}

	@Override
	public Object newInstance()
	{
		return entityFactory.createEntity(this);
	}
}
