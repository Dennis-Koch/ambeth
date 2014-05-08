package de.osthus.ambeth.merge.converter;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.transfer.EntityMetaDataTransfer;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
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
	protected IEntityFactory entityFactory;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

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

			ITypeInfoItem[] primitiveMembers = source.getPrimitiveMembers();
			ITypeInfoItem[] relationMembers = source.getRelationMembers();
			List<String> mergeRelevantNames = new ArrayList<String>();
			for (int a = primitiveMembers.length; a-- > 0;)
			{
				ITypeInfoItem member = primitiveMembers[a];
				if (source.isMergeRelevant(member))
				{
					mergeRelevantNames.add(getNameOfMember(member));
				}
			}
			for (int a = relationMembers.length; a-- > 0;)
			{
				ITypeInfoItem member = relationMembers[a];
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

			HashMap<String, ITypeInfoItem> nameToMemberDict = new HashMap<String, ITypeInfoItem>();

			EntityMetaData target = new EntityMetaData();
			Class<?> entityType = source.getEntityType();
			Class<?> realType = proxyHelper.getRealType(entityType);
			target.setEntityType(entityType);
			target.setRealType(realType);
			target.setIdMember(getMember(entityType, source.getIdMemberName(), nameToMemberDict));
			target.setVersionMember(getMember(entityType, source.getVersionMemberName(), nameToMemberDict));
			target.setCreatedByMember(getMember(entityType, source.getCreatedByMemberName(), nameToMemberDict));
			target.setCreatedOnMember(getMember(entityType, source.getCreatedOnMemberName(), nameToMemberDict));
			target.setUpdatedByMember(getMember(entityType, source.getUpdatedByMemberName(), nameToMemberDict));
			target.setUpdatedOnMember(getMember(entityType, source.getUpdatedOnMemberName(), nameToMemberDict));
			target.setPrimitiveMembers(getMembers(entityType, source.getPrimitiveMemberNames(), nameToMemberDict));
			target.setAlternateIdMembers(getMembers(entityType, source.getAlternateIdMemberNames(), nameToMemberDict));
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
					ITypeInfoItem resolvedMember = getMember(entityType, mergeRelevantNames[a], nameToMemberDict);
					target.setMergeRelevant(resolvedMember, true);
				}
			}
			setMergeRelevant(target, target.getCreatedByMember(), false);
			setMergeRelevant(target, target.getCreatedOnMember(), false);
			setMergeRelevant(target, target.getUpdatedByMember(), false);
			setMergeRelevant(target, target.getUpdatedOnMember(), false);
			setMergeRelevant(target, target.getIdMember(), false);
			setMergeRelevant(target, target.getVersionMember(), false);
			target.initialize(entityFactory);
			return target;
		}
		throw new IllegalStateException("Source of type " + sourceType.getName() + " not supported");
	}

	protected void setMergeRelevant(EntityMetaData metaData, ITypeInfoItem member, boolean value)
	{
		if (member != null)
		{
			metaData.setMergeRelevant(member, value);
		}
	}

	protected ITypeInfoItem getMember(Class<?> entityType, String memberName, Map<String, ITypeInfoItem> nameToMemberDict)
	{
		ITypeInfoItem member = nameToMemberDict.get(memberName);
		if (member != null)
		{
			return member;
		}
		member = typeInfoProvider.getHierarchicMember(entityType, memberName);
		if (member == null)
		{
			throw new RuntimeException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
		}
		nameToMemberDict.put(memberName, member);
		return member;
	}

	protected ITypeInfoItem[] getMembers(Class<?> entityType, String[] memberNames, Map<String, ITypeInfoItem> nameToMemberDict)
	{
		if (memberNames == null)
		{
			return EntityMetaData.emptyTypeInfoItems;
		}
		ITypeInfoItem[] members = new ITypeInfoItem[memberNames.length];
		for (int a = memberNames.length; a-- > 0;)
		{
			members[a] = getMember(entityType, memberNames[a], nameToMemberDict);
		}
		return members;
	}

	protected IRelationInfoItem[] getRelationMembers(Class<?> entityType, String[] memberNames, Map<String, ITypeInfoItem> nameToMemberDict)
	{
		if (memberNames == null)
		{
			return EntityMetaData.emptyRelationInfoItems;
		}
		IRelationInfoItem[] members = new IRelationInfoItem[memberNames.length];
		for (int a = memberNames.length; a-- > 0;)
		{
			members[a] = (IRelationInfoItem) getMember(entityType, memberNames[a], nameToMemberDict);
		}
		return members;
	}

	protected String getNameOfMember(ITypeInfoItem member)
	{
		if (member == null)
		{
			return null;
		}
		return member.getName();
	}

	protected String[] getNamesOfMembers(ITypeInfoItem[] members)
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
