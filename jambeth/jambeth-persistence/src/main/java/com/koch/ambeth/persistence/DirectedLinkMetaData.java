package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.annotation.CascadeLoadMode;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

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
