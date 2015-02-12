package de.osthus.ambeth.persistence;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class DirectedLinkMetaData implements IDirectedLinkMetaData, IInitializingBean
{
	protected String constraintName;

	protected ITableMetaData fromTable;

	protected ITableMetaData toTable;

	protected IFieldMetaData fromField;

	protected IFieldMetaData toField;

	protected RelationMember member;

	protected ILinkMetaData link;

	protected IThreadLocalObjectCollector objectCollector;

	protected boolean isStandaloneLink;

	protected boolean cascadeDelete;

	protected boolean reverse;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(fromTable, "fromTable");
		ParamChecker.assertNotNull(toTable, "toTable");
		ParamChecker.assertNotNull(fromField, "fromField");
		ParamChecker.assertNotNull(toField, "toField");
		ParamChecker.assertNotNull(link, "link");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public ITableMetaData getFromTable()
	{
		return fromTable;
	}

	public void setFromTable(ITableMetaData fromTable)
	{
		this.fromTable = fromTable;
	}

	public void setConstraintName(String constraintName)
	{
		this.constraintName = constraintName;
	}

	public String getConstraintName()
	{
		return constraintName;
	}

	@Override
	public ITableMetaData getToTable()
	{
		return toTable;
	}

	public void setToTable(ITableMetaData toTable)
	{
		this.toTable = toTable;
	}

	@Override
	public IFieldMetaData getFromField()
	{
		return fromField;
	}

	public void setFromField(IFieldMetaData fromField)
	{
		this.fromField = fromField;
	}

	@Override
	public IFieldMetaData getToField()
	{
		return toField;
	}

	public void setToField(IFieldMetaData toField)
	{
		this.toField = toField;
	}

	@Override
	public Class<?> getFromEntityType()
	{
		return fromTable.getEntityType();
	}

	@Override
	public Class<?> getToEntityType()
	{
		return toTable.getEntityType();
	}

	@Override
	public byte getFromIdIndex()
	{
		return fromField.getIdIndex();
	}

	@Override
	public byte getToIdIndex()
	{
		return toField.getIdIndex();
	}

	@Override
	public Member getFromMember()
	{
		return fromField.getMember();
	}

	@Override
	public Member getToMember()
	{
		return toField.getMember();
	}

	@Override
	public Class<?> getEntityType()
	{
		if (fromTable == null)
		{
			return null;
		}
		return fromTable.getEntityType();
	}

	@Override
	public boolean isNullable()
	{
		return link.isNullable();
	}

	@Override
	public RelationMember getMember()
	{
		return member;
	}

	public void setMember(RelationMember member)
	{
		this.member = member;
	}

	@Override
	public ILinkMetaData getLink()
	{
		return link;
	}

	@Override
	public IDirectedLinkMetaData getReverseLink()
	{
		if (link.getDirectedLink().equals(this))
		{
			return link.getReverseDirectedLink();
		}
		else
		{
			return link.getDirectedLink();
		}
	}

	public void setLink(ILinkMetaData link)
	{
		this.link = link;
	}

	@Override
	public boolean isCascadeDelete()
	{
		return cascadeDelete;
	}

	public void setCascadeDelete(boolean cascadeDelete)
	{
		this.cascadeDelete = cascadeDelete;
	}

	@Override
	public boolean isStandaloneLink()
	{
		return isStandaloneLink;
	}

	public void setStandaloneLink(boolean isStandaloneLink)
	{
		this.isStandaloneLink = isStandaloneLink;
	}

	@Override
	public CascadeLoadMode getCascadeLoadMode()
	{
		RelationMember member = this.member;
		return member != null ? member.getCascadeLoadMode() : CascadeLoadMode.LAZY;
	}

	@Override
	public String getName()
	{
		if (reverse)
		{
			return StringBuilderUtil.concat(objectCollector, link.getName(), "_R");
		}
		else
		{
			return StringBuilderUtil.concat(objectCollector, link.getName(), "_F");
		}
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	@Override
	public boolean isReverse()
	{
		return reverse;
	}

	@Override
	public boolean isPersistingLink()
	{
		IDirectedLinkMetaData reverseLink = getReverseLink();
		if (getFromTable().equals(reverseLink.getFromTable()))
		{
			// In this special case we do only want to save "one side" of the changes
			if (isStandaloneLink() && !reverseLink.isStandaloneLink())
			{
				// I am not the one who saves the changes :)
				// I am NOT the chosen one :(
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "DirectedLink: " + getName();
	}
}
