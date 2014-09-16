package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class DirectedLink implements IDirectedLink, IInitializingBean
{
	protected String constraintName;

	protected ITable fromTable;

	protected ITable toTable;

	protected IField fromField;

	protected IField toField;

	protected RelationMember member;

	protected ILink link;

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
	public ITable getFromTable()
	{
		return fromTable;
	}

	public void setFromTable(ITable fromTable)
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
	public ITable getToTable()
	{
		return toTable;
	}

	public void setToTable(ITable toTable)
	{
		this.toTable = toTable;
	}

	@Override
	public IField getFromField()
	{
		return fromField;
	}

	public void setFromField(IField fromField)
	{
		this.fromField = fromField;
	}

	@Override
	public IField getToField()
	{
		return toField;
	}

	public void setToField(IField toField)
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
	public ILink getLink()
	{
		return link;
	}

	@Override
	public IDirectedLink getReverseLink()
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

	public void setLink(ILink link)
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
		IDirectedLink reverseLink = getReverseLink();
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
	public ILinkCursor findLinked(Object fromId)
	{
		return link.findLinked(this, fromId);
	}

	@Override
	public ILinkCursor findLinkedTo(Object toId)
	{
		return link.findLinkedTo(getReverseLink(), toId);
	}

	@Override
	public ILinkCursor findAllLinked(List<?> fromIds)
	{
		return link.findAllLinked(this, fromIds);
	}

	@Override
	public void linkIds(Object fromId, List<?> toIds)
	{
		link.linkIds(this, fromId, toIds);
	}

	@Override
	public void updateLink(Object fromId, Object toId)
	{
		link.updateLink(this, fromId, toId);
	}

	@Override
	public void unlinkIds(Object fromId, List<?> toIds)
	{
		link.unlinkIds(this, fromId, toIds);
	}

	@Override
	public void unlinkAllIds(Object fromId)
	{
		link.unlinkAllIds(this, fromId);
	}

	@Override
	public String toString()
	{
		return "DirectedLink: " + getName();
	}
}
