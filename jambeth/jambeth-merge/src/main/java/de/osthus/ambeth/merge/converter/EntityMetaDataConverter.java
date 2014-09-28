package de.osthus.ambeth.merge.converter;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.transfer.EntityMetaDataTransfer;
import de.osthus.ambeth.metadata.IntermediateEmbeddedPrimitiveMember;
import de.osthus.ambeth.metadata.IntermediateEmbeddedRelationMember;
import de.osthus.ambeth.metadata.IntermediatePrimitiveMember;
import de.osthus.ambeth.metadata.IntermediateRelationMember;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.IDedicatedConverter;
import de.osthus.ambeth.util.ListUtil;

public class EntityMetaDataConverter implements IDedicatedConverter
{
	private static final String[] EMPTY_STRINGS = new String[0];

	protected static final Pattern memberPathSplitPattern = Pattern.compile("\\.");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (sourceType.isAssignableFrom(EntityMetaData.class))
		{
			EntityMetaData source = (EntityMetaData) value;

			EntityMetaDataTransfer target = new EntityMetaDataTransfer();
			target.setEntityType(source.getEntityType());
			target.setIdMemberName(getNameOfMember(source.getIdMember()));
			target.setVersionMemberName(getNameOfMember(source.getVersionMember()));
			target.setCreatedByMemberName(getNameOfMember(source.getCreatedByMember()));
			target.setCreatedOnMemberName(getNameOfMember(source.getCreatedOnMember()));
			target.setUpdatedByMemberName(getNameOfMember(source.getUpdatedByMember()));
			target.setUpdatedOnMemberName(getNameOfMember(source.getUpdatedOnMember()));
			target.setAlternateIdMemberNames(getNamesOfMembers(source.getAlternateIdMembers()));
			target.setPrimitiveMemberNames(getNamesOfMembers(source.getPrimitiveMembers()));
			target.setRelationMemberNames(getNamesOfMembers(source.getRelationMembers()));
			target.setTypesRelatingToThis(source.getTypesRelatingToThis());
			target.setTypesToCascadeDelete(source.getCascadeDeleteTypes().toArray(new Class<?>[source.getCascadeDeleteTypes().size()]));

			Member[] primitiveMembers = source.getPrimitiveMembers();
			RelationMember[] relationMembers = source.getRelationMembers();
			List<String> mergeRelevantNames = new ArrayList<String>();
			for (int a = primitiveMembers.length; a-- > 0;)
			{
				Member member = primitiveMembers[a];
				if (source.isMergeRelevant(member))
				{
					mergeRelevantNames.add(getNameOfMember(member));
				}
			}
			for (int a = relationMembers.length; a-- > 0;)
			{
				RelationMember member = relationMembers[a];
				if (source.isMergeRelevant(member))
				{
					mergeRelevantNames.add(getNameOfMember(member));
				}
			}
			target.setMergeRelevantNames(ListUtil.toArray(String.class, mergeRelevantNames));
			return target;
		}
		else if (sourceType.isAssignableFrom(EntityMetaDataTransfer.class))
		{
			EntityMetaDataTransfer source = (EntityMetaDataTransfer) value;

			HashMap<String, Member> nameToMemberDict = new HashMap<String, Member>();

			EntityMetaData target = new EntityMetaData();
			Class<?> entityType = source.getEntityType();
			Class<?> realType = proxyHelper.getRealType(entityType);
			target.setEntityType(entityType);
			target.setRealType(realType);
			target.setIdMember(getPrimitiveMember(entityType, source.getIdMemberName(), nameToMemberDict));
			target.setVersionMember(getPrimitiveMember(entityType, source.getVersionMemberName(), nameToMemberDict));
			target.setCreatedByMember(getPrimitiveMember(entityType, source.getCreatedByMemberName(), nameToMemberDict));
			target.setCreatedOnMember(getPrimitiveMember(entityType, source.getCreatedOnMemberName(), nameToMemberDict));
			target.setUpdatedByMember(getPrimitiveMember(entityType, source.getUpdatedByMemberName(), nameToMemberDict));
			target.setUpdatedOnMember(getPrimitiveMember(entityType, source.getUpdatedOnMemberName(), nameToMemberDict));
			target.setPrimitiveMembers(getPrimitiveMembers(entityType, source.getPrimitiveMemberNames(), nameToMemberDict));
			target.setAlternateIdMembers(getPrimitiveMembers(entityType, source.getAlternateIdMemberNames(), nameToMemberDict));
			target.setRelationMembers(getRelationMembers(entityType, source.getRelationMemberNames(), nameToMemberDict));
			target.setTypesRelatingToThis(source.getTypesRelatingToThis());
			Class<?>[] typesToCascadeDelete = source.getTypesToCascadeDelete();
			for (int a = 0, size = typesToCascadeDelete.length; a < size; a++)
			{
				target.getCascadeDeleteTypes().add(typesToCascadeDelete[a]);
			}
			String[] mergeRelevantNames = source.getMergeRelevantNames();
			if (mergeRelevantNames != null)
			{
				for (int a = mergeRelevantNames.length; a-- > 0;)
				{
					Member resolvedMember = nameToMemberDict.get(mergeRelevantNames[a]);
					target.setMergeRelevant(resolvedMember, true);
				}
			}
			setMergeRelevant(target, target.getCreatedByMember(), false);
			setMergeRelevant(target, target.getCreatedOnMember(), false);
			setMergeRelevant(target, target.getUpdatedByMember(), false);
			setMergeRelevant(target, target.getUpdatedOnMember(), false);
			setMergeRelevant(target, target.getIdMember(), false);
			setMergeRelevant(target, target.getVersionMember(), false);
			return target;
		}
		throw new IllegalStateException("Source of type " + sourceType.getName() + " not supported");
	}

	protected void setMergeRelevant(EntityMetaData metaData, Member member, boolean value)
	{
		if (member != null)
		{
			metaData.setMergeRelevant(member, value);
		}
	}

	protected IntermediatePrimitiveMember getPrimitiveMember(Class<?> entityType, String memberName, Map<String, Member> nameToMemberDict)
	{
		if (memberName == null)
		{
			return null;
		}
		IntermediatePrimitiveMember member = (IntermediatePrimitiveMember) nameToMemberDict.get(memberName);
		if (member != null)
		{
			return member;
		}
		String[] memberNameSplit = memberName.split(Pattern.quote("."));
		Class<?> currentType = entityType;
		Member[] memberSplit = new Member[memberNameSplit.length];
		for (int a = 0, size = memberNameSplit.length; a < size; a++)
		{
			IPropertyInfo property = propertyInfoProvider.getProperty(currentType, memberNameSplit[a]);
			if (property == null)
			{
				throw new IllegalStateException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
			}
			memberSplit[a] = new IntermediatePrimitiveMember(currentType, property.getPropertyType(), property.getElementType(), memberNameSplit[a]);
			currentType = property.getPropertyType();
		}
		if (memberSplit.length == 1)
		{
			member = (IntermediatePrimitiveMember) memberSplit[0];
		}
		else
		{
			Member[] memberPath = new Member[memberSplit.length - 1];
			System.arraycopy(memberSplit, 0, memberPath, 0, memberPath.length);
			Member childMember = memberSplit[memberPath.length];
			member = new IntermediateEmbeddedPrimitiveMember(entityType, childMember.getRealType(), childMember.getElementType(), memberName, memberPath,
					childMember);
		}
		if (member == null)
		{
			throw new RuntimeException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
		}
		nameToMemberDict.put(memberName, member);
		return member;
	}

	protected RelationMember getRelationMember(Class<?> entityType, String memberName, Map<String, Member> nameToMemberDict)
	{
		RelationMember member = (RelationMember) nameToMemberDict.get(memberName);
		if (member != null)
		{
			return member;
		}
		String[] memberNameSplit = memberName.split(Pattern.quote("."));
		Class<?> currentType = entityType;
		Member[] memberSplit = new Member[memberNameSplit.length];
		for (int a = 0, size = memberNameSplit.length; a < size; a++)
		{
			IPropertyInfo property = propertyInfoProvider.getProperty(currentType, memberNameSplit[a]);
			if (property == null)
			{
				throw new IllegalStateException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
			}
			memberSplit[a] = new IntermediateRelationMember(currentType, property.getPropertyType(), property.getElementType(), memberNameSplit[a]);
			currentType = property.getPropertyType();
		}
		if (memberSplit.length == 1)
		{
			member = (IntermediateRelationMember) memberSplit[0];
		}
		else
		{
			Member[] memberPath = new Member[memberSplit.length - 1];
			System.arraycopy(memberSplit, 0, memberPath, 0, memberPath.length);
			Member childMember = memberSplit[memberPath.length];
			member = new IntermediateEmbeddedRelationMember(entityType, childMember.getRealType(), childMember.getElementType(), memberName, memberPath,
					childMember);
		}
		if (member == null)
		{
			throw new RuntimeException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
		}
		nameToMemberDict.put(memberName, member);
		return member;
	}

	protected PrimitiveMember[] getPrimitiveMembers(Class<?> entityType, String[] memberNames, Map<String, Member> nameToMemberDict)
	{
		if (memberNames == null)
		{
			return EntityMetaData.emptyPrimitiveMembers;
		}
		PrimitiveMember[] members = new PrimitiveMember[memberNames.length];
		for (int a = memberNames.length; a-- > 0;)
		{
			members[a] = getPrimitiveMember(entityType, memberNames[a], nameToMemberDict);
		}
		return members;
	}

	protected RelationMember[] getRelationMembers(Class<?> entityType, String[] memberNames, Map<String, Member> nameToMemberDict)
	{
		if (memberNames == null)
		{
			return EntityMetaData.emptyRelationMembers;
		}
		RelationMember[] members = new RelationMember[memberNames.length];
		for (int a = memberNames.length; a-- > 0;)
		{
			members[a] = getRelationMember(entityType, memberNames[a], nameToMemberDict);
		}
		return members;
	}

	protected String getNameOfMember(Member member)
	{
		if (member == null)
		{
			return null;
		}
		return member.getName();
	}

	protected String[] getNamesOfMembers(Member[] members)
	{
		if (members == null)
		{
			return EMPTY_STRINGS;
		}
		String[] names = new String[members.length];
		for (int a = members.length; a-- > 0;)
		{
			names[a] = getNameOfMember(members[a]);
		}
		return names;
	}
}
