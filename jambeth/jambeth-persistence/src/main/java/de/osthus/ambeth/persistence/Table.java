package de.osthus.ambeth.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IMemberTypeProvider;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;

public class Table implements ITable, IInitializingBean
{

	public static final short[] EMPTY_SHORT_ARRAY = new short[0];

	public static final IField[] EMPTY_FIELD_ARRAY = new IField[0];

	private static final ILogger log = LoggerFactory.getLogger(Table.class.getName());

	protected final ArrayList<IField> primitiveFields;

	protected final ArrayList<IField> fulltextFields;

	protected final ArrayList<IField> allFields;

	protected final ArrayList<IDirectedLink> links;

	protected final Map<String, Integer> fieldNameToFieldIndexDict = new HashMap<String, Integer>();

	protected final Map<String, IField> fieldNameToFieldDict = new HashMap<String, IField>();

	protected final Set<String> fieldNameToIgnoreDict = new HashSet<String>();

	protected final Map<String, IField> memberNameToFieldDict = new HashMap<String, IField>();

	protected final Set<String> memberNameToIgnoreDict = new HashSet<String>();

	protected final Map<String, IDirectedLink> linkNameToLinkDict = new HashMap<String, IDirectedLink>();

	protected final Map<String, IDirectedLink> memberNameToLinkDict = new HashMap<String, IDirectedLink>();

	protected final Map<String, String> linkNameToMemberNameDict = new HashMap<String, String>();

	protected String name;

	protected boolean viewBased = false;

	protected Class<?> entityType;

	protected boolean archive = false;

	protected IField idField;

	protected IField versionField;

	protected IField createdByField;

	protected IField createdOnField;

	protected IField updatedByField;

	protected IField updatedOnField;

	protected String sequenceName;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected IField[] alternateIdFields = EMPTY_FIELD_ARRAY;

	protected short[] alternateIdFieldIndices = EMPTY_SHORT_ARRAY;

	public Table()
	{
		primitiveFields = new ArrayList<IField>();
		fulltextFields = new ArrayList<IField>();
		allFields = new ArrayList<IField>();
		links = new ArrayList<IDirectedLink>();
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(name, "name");
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getFullqualifiedEscapedName()
	{
		return getName();
	}

	@Override
	public boolean isViewBased()
	{
		return viewBased;
	}

	public void setViewBased(boolean viewBased)
	{
		this.viewBased = viewBased;
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;

		// formerly in afterPropertiesSet, but now only active if an explicit entity mapping is done
		if (!viewBased)
		{
			ParamChecker.assertNotNull(idField, "idField (in " + name + ")");
		}
		if (log.isWarnEnabled() && versionField == null)
		{
			log.warn("No version field (in " + name + ")");
		}
	}

	@Override
	public boolean isArchive()
	{
		return archive;
	}

	public void setArchive(boolean archive)
	{
		this.archive = archive;
	}

	@Override
	public IField getIdField()
	{
		return idField;
	}

	public void setIdField(IField idField)
	{
		this.idField = idField;
	}

	@Override
	public IField getVersionField()
	{
		return versionField;
	}

	public void setVersionField(IField versionField)
	{
		this.versionField = versionField;
	}

	@Override
	public IField getIdFieldByAlternateIdIndex(byte idIndex)
	{
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			return getIdField();
		}
		return getAlternateIdFields()[idIndex];
	}

	@Override
	public IField[] getAlternateIdFields()
	{
		return alternateIdFields;
	}

	public void setAlternateIdFields(IField[] alternateIdFields)
	{
		this.alternateIdFields = alternateIdFields;
		refreshAlternateIdFieldIndices();
	}

	@Override
	public int getAlternateIdCount()
	{
		return alternateIdFields.length;
	}

	protected void refreshAlternateIdFieldIndices()
	{
		if (alternateIdFields == null || alternateIdFields.length == 0 || allFields == null || allFields.size() == 0)
		{
			alternateIdFieldIndices = EMPTY_SHORT_ARRAY;
			return;
		}
		alternateIdFieldIndices = new short[alternateIdFields.length];
		for (int a = alternateIdFields.length; a-- > 0;)
		{
			short index = -1;
			String alternateIdFieldName = alternateIdFields[a].getName();
			for (int b = allFields.size(); b-- > 0;)
			{
				if (alternateIdFieldName.equals(allFields.get(b).getName()))
				{
					index = (short) b;
					break;
				}
			}
			if (index == -1)
			{
				throw new IllegalArgumentException("Alternate id member '" + alternateIdFieldName + "' has to be a valid primitive member");
			}
			alternateIdFieldIndices[a] = index;
		}
	}

	@Override
	public short[] getAlternateIdFieldIndicesInFields()
	{
		return alternateIdFieldIndices;
	}

	@Override
	public IField getCreatedByField()
	{
		return createdByField;
	}

	public void setCreatedByField(IField createdByField)
	{
		this.createdByField = createdByField;
	}

	@Override
	public IField getCreatedOnField()
	{
		return createdOnField;
	}

	public void setCreatedOnField(IField createdOnField)
	{
		this.createdOnField = createdOnField;
	}

	@Override
	public IField getUpdatedByField()
	{
		return updatedByField;
	}

	public void setUpdatedByField(IField updatedByField)
	{
		this.updatedByField = updatedByField;
	}

	@Override
	public IField getUpdatedOnField()
	{
		return updatedOnField;
	}

	public void setUpdatedOnField(IField updatedOnField)
	{
		this.updatedOnField = updatedOnField;
	}

	@Override
	public List<IField> getPrimitiveFields()
	{
		return primitiveFields;
	}

	@Override
	public List<IField> getFulltextFields()
	{
		return fulltextFields;
	}

	public void setFulltextFieldsByNames(List<String> names)
	{
		for (int i = names.size(); i-- > 0;)
		{
			IField field = getFieldByName(names.get(i));
			if (field != null)
			{
				fulltextFields.add(field);
			}
		}
	}

	@Override
	public List<IField> getAllFields()
	{
		return allFields;
	}

	@Override
	public List<IDirectedLink> getLinks()
	{
		return links;
	}

	@Override
	public String getSequenceName()
	{
		return sequenceName;
	}

	public void setSequenceName(String sequenceName)
	{
		this.sequenceName = sequenceName;
	}

	@Override
	public IList<Object> acquireIds(int count)
	{
		return new ArrayList<Object>(count);
	}

	public void configureLink(String linkName, String memberName)
	{
		IDirectedLink link = linkNameToLinkDict.get(linkName);
		if (memberNameToLinkDict.containsKey(memberName))
		{
			throw new IllegalArgumentException("Member '" + memberName + "' already configured to link '" + memberNameToLinkDict.get(memberName).getName()
					+ "': memberName");
		}
		linkNameToMemberNameDict.put(linkName, memberName);
		memberNameToLinkDict.put(memberName, link);
	}

	public void mapField(IField field)
	{
		String fieldName = field.getName();
		if (getFieldByName(fieldName) != null)
		{
			throw new RuntimeException("Field '" + getName() + "." + fieldName + "' already configured");
		}
		primitiveFields.add(field);
		allFields.add(field);
		fieldName = fieldName.toUpperCase();
		fieldNameToFieldIndexDict.put(fieldName, Integer.valueOf(fieldNameToFieldIndexDict.size()));
		fieldNameToFieldDict.put(fieldName, field);
	}

	@Override
	public IField mapField(String fieldName, String memberName)
	{
		Member member = memberTypeProvider.getPrimitiveMember(getEntityType(), memberName);

		if (member == null)
		{
			throw new NullPointerException("Member '" + getEntityType().getName() + "." + memberName + "' not found to map to field '" + getName() + "."
					+ fieldName + "'");
		}
		IField field = getFieldByName(fieldName);
		if (field == null)
		{
			throw new NullPointerException("Field '" + getName() + "." + fieldName + "' not found to map to member '" + getEntityType().getName() + "."
					+ memberName + "'");
		}
		Field fieldInstance = (Field) field;
		fieldInstance.setMember(member);

		if (field != idField && field != versionField)
		{
			memberNameToFieldDict.put(memberName, field);
		}
		return field;
	}

	@Override
	public void mapIgnore(String fieldName, String memberName)
	{
		if (fieldName != null)
		{
			fieldNameToIgnoreDict.add(fieldName);
		}
		if (memberName != null)
		{
			memberNameToIgnoreDict.add(memberName);
		}
	}

	@Override
	public boolean isIgnoredField(String fieldName)
	{
		return fieldNameToIgnoreDict.contains(fieldName);
	}

	@Override
	public boolean isIgnoredMember(String memberName)
	{
		return memberNameToIgnoreDict.contains(memberName);
	}

	public void mapLink(IDirectedLink link)
	{
		String linkName = link.getName();
		if (getLinkByName(linkName) != null)
		{
			throw new RuntimeException("Link '" + getName() + "." + link + "' already configured");
		}
		links.add(link);
		linkNameToLinkDict.put(linkName.toUpperCase(), link);
	}

	@Override
	public IDirectedLink mapLink(String linkName, String memberName)
	{
		RelationMember member = memberTypeProvider.getRelationMember(getEntityType(), memberName);
		if (member == null)
		{
			throw new NullPointerException("Member '" + getEntityType().getName() + "." + memberName + "' not found to map to link '" + linkName + "'");
		}
		IDirectedLink link = getLinkByName(linkName);
		if (link == null)
		{
			throw new NullPointerException("Link '" + linkName + "' not found to map to member '" + getEntityType().getName() + "." + memberName + "'");
		}
		DirectedLink linkInstance = (DirectedLink) link;
		linkInstance.setMember(member);
		// TODO: REGRESSION - This is related to the current RelationAutomappingTest
		// Comment can be completely removed after the test is solved
		// if (link.isPersistingLink())
		// {
		memberNameToLinkDict.put(memberName, link);
		// }
		linkNameToMemberNameDict.put(linkName.toUpperCase(), memberName);
		return link;
	}

	@Override
	public IVersionCursor selectVersion(List<?> ids)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectVersionWhere(CharSequence whereSql)
	{
		return selectVersionWhere(null, whereSql, null);
	}

	@Override
	public IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereWithOrderBySql, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql,
			List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql,
			List<Object> parameters, String tableAlias)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			int offset, int length, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			int offset, int length, List<Object> parameters, String tableAlias)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql, List<Object> parameters,
			String tableAlias)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataPaging(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, int offset,
			int length, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectAll()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(List<?> ids)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(String alternateIdMemberName, List<?> alternateIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void preProcessDelete(Object id, Object version)
	{
		List<IDirectedLink> links = getLinks();
		if (links.isEmpty())
		{
			return;
		}

		for (int i = links.size(); i-- > 0;)
		{
			IDirectedLink link = links.get(i);
			if (link.isNullable())
			{
				link.unlinkAllIds(id);
			}
			else if (name.equals(link.getFromTable().getName()))
			{
				if (hasBackLink(link.getToTable(), link))
				{
					throw new UnsupportedOperationException("Not implemented: Cyclic constraint!");
				}
				// Reference lays in this table and there is no back link - no problem here.
				continue;
			}
			else
			{
				throw new UnsupportedOperationException("Not implemented: Not nullable link from other table!");
			}
		}
	}

	@Override
	public void preProcessUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis)
	{
		List<IDirectedLink> links = getLinks();
		if (links.isEmpty() || ruis == null || ruis.isEmpty())
		{
			return;
		}
		HashSet<String> toUpdate = HashSet.create(ruis.size());
		for (int i = ruis.size(); i-- > 0;)
		{
			toUpdate.add(ruis.get(i).getMemberName());
		}
		for (int i = links.size(); i-- > 0;)
		{
			IDirectedLink link = links.get(i);
			if (toUpdate.contains(link.getMember().getName()) && name.equals(link.getFromTable().getName()))
			{
				if (hasBackLink(link.getToTable(), link))
				{
					throw new UnsupportedOperationException("Not implemented: Cyclic constraint!");
				}
			}
		}
	}

	protected boolean hasBackLink(ITable other, IDirectedLink link)
	{
		List<IDirectedLink> otherLinks = other.getLinks();
		for (int i = otherLinks.size(); i-- > 0;)
		{
			IDirectedLink otherLink = otherLinks.get(i);
			if (!otherLink.getName().equals(link.getName()) && getName().equals(otherLink.getToTable().getName()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void postProcessInsertAndUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis)
	{
		if (ruis == null)
		{
			return;
		}

		ArrayList<Object> toIds = new ArrayList<Object>();
		for (int a = ruis.size(); a-- > 0;)
		{
			IRelationUpdateItem rui = ruis.get(a);
			IDirectedLink link = getLinkByMemberName(rui.getMemberName());
			if (link == null)
			{
				throw new RuntimeException("No link found for member '" + rui.getMemberName() + "' on entity '" + getEntityType() + "'");
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			IObjRef[] addedORIs = rui.getAddedORIs();
			if (!link.getName().equals(link.getLink().getTableName()) && removedORIs != null && addedORIs != null && removedORIs.length == 1
					&& addedORIs.length == 1)
			{
				// Do a real update on data table with link, field may not
				// be null-able
				link.updateLink(id, addedORIs[0].getId());
			}
			else
			{
				if (removedORIs != null)
				{
					for (IObjRef removedORI : removedORIs)
					{
						toIds.add(removedORI.getId());
					}
					link.unlinkIds(id, toIds);
					toIds.clear();
				}
				if (addedORIs != null)
				{
					for (IObjRef addedORI : addedORIs)
					{
						toIds.add(addedORI.getId());
					}
					link.linkIds(id, toIds);
					toIds.clear();
				}
			}
		}
	}

	@Override
	public IField getFieldByName(String fieldName)
	{
		if (fieldName == null || fieldName.isEmpty())
		{
			return null;
		}
		if (idField != null && fieldName.equals(idField.getName()))
		{
			return idField;
		}
		if (versionField != null && fieldName.equals(versionField.getName()))
		{
			return versionField;
		}
		return fieldNameToFieldDict.get(fieldName.toUpperCase());
	}

	@Override
	public IField getFieldByMemberName(String memberName)
	{
		return memberNameToFieldDict.get(memberName);
	}

	@Override
	public int getFieldIndexByName(String fieldName)
	{
		Integer index = fieldNameToFieldIndexDict.get(fieldName);
		if (index == null)
		{
			return -1;
		}
		return index.intValue();
	}

	@Override
	public IField getFieldByPropertyName(String propertyName)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IDirectedLink getLinkByName(String linkName)
	{
		return linkNameToLinkDict.get(linkName.toUpperCase());
	}

	@Override
	public IDirectedLink getLinkByMemberName(String memberName)
	{
		return memberNameToLinkDict.get(memberName);
	}

	@Override
	public String getMemberNameByLinkName(String linkName)
	{
		return linkNameToMemberNameDict.get(linkName.toUpperCase());
	}

	@Override
	public void delete(List<IObjRef> oris)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void startBatch()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int[] finishBatch()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void clearBatch()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object insert(Object id, IParamHolder<Object> newId, ILinkedMap<String, Object> puis)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object update(Object id, Object version, ILinkedMap<String, Object> puis)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected void deleteLinksToId(Object id)
	{
		for (IDirectedLink relatedLink : links)
		{
			relatedLink.unlinkAllIds(id);
		}
	}

	@Override
	public String toString()
	{
		return "Table: " + getName();
	}
}
