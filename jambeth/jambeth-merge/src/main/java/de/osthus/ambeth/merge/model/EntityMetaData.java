package de.osthus.ambeth.merge.model;

import java.util.Arrays;
import java.util.Set;

import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.SmartCopySet;
import de.osthus.ambeth.compositeid.CompositeIdMember;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.EmbeddedMember;
import de.osthus.ambeth.metadata.IPrimitiveMemberWrite;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.util.ListUtil;

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

	public static final PrimitiveMember[] emptyPrimitiveMembers = new PrimitiveMember[0];

	public static final RelationMember[] emptyRelationMembers = new RelationMember[0];

	public static final IEntityLifecycleExtension[] emptyEntityLifecycleExtensions = new IEntityLifecycleExtension[0];

	protected Class<?> entityType;

	protected Class<?> realType;

	protected Class<?> enhancedType;

	protected boolean localEntity = true;

	protected Class<?>[] typesRelatingToThis = emptyTypes;

	protected final HashSet<Class<?>> typesRelatingToThisSet = new HashSet<Class<?>>(0.5f);

	protected final HashSet<Class<?>> cascadeDeleteTypes = new HashSet<Class<?>>(0.5f);

	protected IEntityLifecycleExtension[] entityLifecycleExtensions = emptyEntityLifecycleExtensions;

	protected RelationMember[] relationMembers = emptyRelationMembers;

	protected PrimitiveMember[] primitiveMembers = emptyPrimitiveMembers;

	protected PrimitiveMember[] primitiveToManyMembers = emptyPrimitiveMembers;

	protected PrimitiveMember[] alternateIdMembers = emptyPrimitiveMembers;

	protected PrimitiveMember[] fulltextMembers = emptyPrimitiveMembers;

	protected PrimitiveMember versionMember;

	protected PrimitiveMember idMember;

	protected PrimitiveMember createdOn;

	protected PrimitiveMember createdBy;

	protected PrimitiveMember updatedOn;

	protected PrimitiveMember updatedBy;

	protected int[][] alternateIdMemberIndicesInPrimitives = emptyShortArray;

	protected final HashSet<Member> fulltextMemberSet = new HashSet<Member>(0.5f);

	protected final HashSet<Member> alternateIdMemberSet = new HashSet<Member>(0.5f);

	protected final SmartCopySet<Member> interningMemberSet = new SmartCopySet<Member>(0.5f);

	protected final HashMap<String, Member> nameToMemberDict = new HashMap<String, Member>(0.5f);

	protected final HashMap<Member, Boolean> memberToMergeRelevanceDict = new HashMap<Member, Boolean>(0.5f);

	protected final HashMap<String, Byte> memberNameToIdIndexDict = new HashMap<String, Byte>(0.5f);

	protected final HashMap<String, Integer> relMemberNameToIndexDict = new HashMap<String, Integer>(0.5f);

	protected final HashMap<String, Integer> primMemberNameToIndexDict = new HashMap<String, Integer>(0.5f);

	protected final IdentityHashMap<RelationMember, Integer> relMemberToIndexDict = new IdentityHashMap<RelationMember, Integer>(0.5f);

	protected final IdentityHashMap<Member, Integer> primMemberToIndexDict = new IdentityHashMap<Member, Integer>(0.5f);

	protected ICacheModification cacheModification;

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
	public PrimitiveMember getIdMember()
	{
		return idMember;
	}

	public void setIdMember(PrimitiveMember idMember)
	{
		this.idMember = idMember;
	}

	@Override
	public PrimitiveMember getIdMemberByIdIndex(int idIndex)
	{
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			return idMember;
		}
		return getAlternateIdMembers()[idIndex];
	}

	@Override
	public PrimitiveMember getVersionMember()
	{
		return versionMember;
	}

	public void setVersionMember(PrimitiveMember versionMember)
	{
		this.versionMember = versionMember;
	}

	@Override
	public PrimitiveMember[] getAlternateIdMembers()
	{
		return alternateIdMembers;
	}

	public void setAlternateIdMembers(PrimitiveMember[] alternateIdMembers)
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
	public boolean isAlternateId(Member primitiveMember)
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
	public boolean hasInterningBehavior(Member primitiveMember)
	{
		return interningMemberSet.contains(primitiveMember);
	}

	@Override
	public void changeInterningBehavior(Member primitiveMember, boolean state)
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
	public PrimitiveMember getCreatedOnMember()
	{
		return createdOn;
	}

	public void setCreatedOnMember(PrimitiveMember createdOn)
	{
		this.createdOn = createdOn;
	}

	@Override
	public PrimitiveMember getCreatedByMember()
	{
		return createdBy;
	}

	public void setCreatedByMember(PrimitiveMember createdBy)
	{
		this.createdBy = createdBy;
	}

	@Override
	public PrimitiveMember getUpdatedOnMember()
	{
		return updatedOn;
	}

	public void setUpdatedOnMember(PrimitiveMember updatedOn)
	{
		this.updatedOn = updatedOn;
	}

	@Override
	public PrimitiveMember getUpdatedByMember()
	{
		return updatedBy;
	}

	public void setUpdatedByMember(PrimitiveMember updatedBy)
	{
		this.updatedBy = updatedBy;
	}

	@Override
	public PrimitiveMember[] getFulltextMembers()
	{
		return fulltextMembers;
	}

	public void setFulltextMembers(PrimitiveMember[] fulltextMembers)
	{
		this.fulltextMembers = fulltextMembers;
	}

	@Override
	public PrimitiveMember[] getPrimitiveMembers()
	{
		return primitiveMembers;
	}

	public void setPrimitiveMembers(PrimitiveMember[] primitiveMembers)
	{
		this.primitiveMembers = primitiveMembers;
	}

	@Override
	public RelationMember[] getRelationMembers()
	{
		return relationMembers;
	}

	public void setRelationMembers(RelationMember[] relationMembers)
	{
		this.relationMembers = relationMembers;
	}

	public Set<Class<?>> getCascadeDeleteTypes()
	{
		return cascadeDeleteTypes;
	}

	@Override
	public boolean isFulltextRelevant(Member member)
	{
		return fulltextMemberSet.contains(member);
	}

	@Override
	public boolean isMergeRelevant(Member member)
	{
		Boolean relevance = memberToMergeRelevanceDict.get(member);
		return relevance == null || relevance.booleanValue();
	}

	public void setMergeRelevant(Member member, boolean relevant)
	{
		memberToMergeRelevanceDict.put(member, Boolean.valueOf(relevant));
	}

	@Override
	public Member getMemberByName(String memberName)
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
	public boolean isPrimitiveMember(String primitiveMemberName)
	{
		return primMemberNameToIndexDict.containsKey(primitiveMemberName);
	}

	@Override
	public boolean isRelationMember(String relationMemberName)
	{
		return relMemberNameToIndexDict.containsKey(relationMemberName);
	}

	@Override
	public int getIndexByRelation(Member relationMember)
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
	public int getIndexByPrimitive(Member primitiveMember)
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
	public void postProcessNewEntity(Object newEntity)
	{
		PrimitiveMember[] primitiveToManyMembers = this.primitiveToManyMembers;
		if (primitiveToManyMembers.length > 0)
		{
			boolean oldInternalUpdate = cacheModification.isInternalUpdate();
			if (!oldInternalUpdate)
			{
				cacheModification.setInternalUpdate(true);
			}
			try
			{
				for (PrimitiveMember primitiveMember : primitiveToManyMembers)
				{
					primitiveMember.setValue(newEntity, ListUtil.createObservableCollectionOfType(primitiveMember.getRealType()));
				}
			}
			finally
			{
				if (!oldInternalUpdate)
				{
					cacheModification.setInternalUpdate(false);
				}
			}
		}
		for (IEntityLifecycleExtension entityLifecycleExtension : entityLifecycleExtensions)
		{
			entityLifecycleExtension.postCreate(this, newEntity);
		}
	}

	@Override
	public void postLoad(Object entity)
	{
		for (IEntityLifecycleExtension entityLifecycleExtension : entityLifecycleExtensions)
		{
			entityLifecycleExtension.postLoad(this, entity);
		}
	}

	@Override
	public void prePersist(Object entity)
	{
		for (IEntityLifecycleExtension entityLifecycleExtension : entityLifecycleExtensions)
		{
			entityLifecycleExtension.prePersist(this, entity);
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

	public void initialize(ICacheModification cacheModification, IEntityFactory entityFactory)
	{
		this.cacheModification = cacheModification;
		this.entityFactory = entityFactory;
		if (primitiveMembers == null)
		{
			primitiveMembers = emptyPrimitiveMembers;
		}
		else
		{
			// Arrays.sort(primitiveMembers, typeInfoItemComparator);
		}
		ArrayList<PrimitiveMember> primitiveToManyMembers = new ArrayList<PrimitiveMember>();
		for (PrimitiveMember primitiveMember : getPrimitiveMembers())
		{
			if (primitiveMember.isToMany())
			{
				primitiveToManyMembers.add(primitiveMember);
			}
		}
		this.primitiveToManyMembers = primitiveToManyMembers.toArray(PrimitiveMember.class);

		if (relationMembers == null)
		{
			relationMembers = emptyRelationMembers;
		}
		else
		{
			// Arrays.sort(relationMembers, typeInfoItemComparator);
		}

		if (alternateIdMembers == null)
		{
			alternateIdMembers = emptyPrimitiveMembers;
		}
		else
		{
			// Arrays.sort(alternateIdMembers, typeInfoItemComparator);
		}

		if (fulltextMembers == null)
		{
			fulltextMembers = emptyPrimitiveMembers;
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
			nameToMemberDict.put(getIdMember().getName(), getIdMember());
		}
		if (getVersionMember() != null)
		{
			nameToMemberDict.put(getVersionMember().getName(), getVersionMember());
		}
		for (int a = primitiveMembers.length; a-- > 0;)
		{
			Member member = primitiveMembers[a];
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
			RelationMember member = relationMembers[a];
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
			Member alternateIdMember = alternateIdMembers[idIndex];
			Member[] memberItems;
			if (alternateIdMember instanceof CompositeIdMember)
			{
				memberItems = ((CompositeIdMember) alternateIdMember).getMembers();
			}
			else
			{
				memberItems = new Member[] { alternateIdMember };
			}
			compositeIndex = new int[memberItems.length];

			for (int compositePosition = compositeIndex.length; compositePosition-- > 0;)
			{
				compositeIndex[compositePosition] = -1;
				Member memberItem = memberItems[compositePosition];
				for (int primitiveIndex = primitiveMembers.length; primitiveIndex-- > 0;)
				{
					if (memberItem.equals(primitiveMembers[primitiveIndex]))
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
		setTechnicalMember(getIdMember());
		setTechnicalMember(getVersionMember());
		setTechnicalMember(getCreatedByMember());
		setTechnicalMember(getCreatedOnMember());
		setTechnicalMember(getUpdatedByMember());
		setTechnicalMember(getUpdatedOnMember());
	}

	protected void setTechnicalMember(PrimitiveMember member)
	{
		if (member == null)
		{
			return;
		}
		((IPrimitiveMemberWrite) member).setTechnicalMember(true);
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

	@Override
	public Member getWidenedMatchingMember(String memberPath)
	{
		Member member = getMemberByName(memberPath);
		if (member != null)
		{
			// fast case
			return member;
		}
		String[] memberPathSplit = EmbeddedMember.split(memberPath);
		int length = memberPathSplit.length - 1; // the full length has already been tested in the fast case
		StringBuilder sb = new StringBuilder();
		member = getMemberByName(buildMemberName(memberPathSplit, length, sb));
		while (member == null && length > 0)
		{
			length--;
			member = getMemberByName(buildMemberName(memberPathSplit, length, sb));
		}
		return member;
	}

	@Override
	public Member getWidenedMatchingMember(String[] memberPath)
	{
		int length = memberPath.length;
		StringBuilder sb = new StringBuilder();
		Member member = getMemberByName(buildMemberName(memberPath, length, sb));
		while (member == null && length > 0)
		{
			length--;
			member = getMemberByName(buildMemberName(memberPath, length, sb));
		}
		return member;
	}

	protected String buildMemberName(String[] memberNameTokens, int length, StringBuilder sb)
	{
		sb.setLength(0);
		for (int a = 0; a < length; a++)
		{
			String memberNameToken = memberNameTokens[a];
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(memberNameToken);
		}
		return sb.toString();
	}
}