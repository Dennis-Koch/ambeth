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
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ParamChecker;

public class DirectedExternalLinkMetaData extends DirectedLinkMetaData
		implements IDirectedLinkMetaData, IInitializingBean {
	protected Member toMember;

	protected Member fromMember;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet() {
		// super.afterPropertiesSet() intentionally not called here!
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
		ParamChecker.assertTrue(fromTable != null || toTable != null, "fromTable or toTable");
		if (fromTable != null) {
			ParamChecker.assertNotNull(fromField, "fromField");
			ParamChecker.assertNotNull(toMember, "toMember");
		}
		else {
			ParamChecker.assertNotNull(toField, "toField");
			ParamChecker.assertNotNull(fromMember, "fromMember");
		}
		ParamChecker.assertNotNull(link, "link");
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider) {
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public Class<?> getFromEntityType() {
		if (fromTable != null) {
			return super.getFromEntityType();
		}
		else {
			return fromMember.getDeclaringType();
		}
	}

	@Override
	public Class<?> getToEntityType() {
		if (toTable != null) {
			return super.getToEntityType();
		}
		else {
			return toMember.getDeclaringType();
		}
	}

	@Override
	public byte getFromIdIndex() {
		if (fromField != null) {
			return super.getFromIdIndex();
		}
		else {
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(getFromEntityType());
			byte fromIdIndex = metaData.getIdIndexByMemberName(fromMember.getName());
			return fromIdIndex;
		}
	}

	@Override
	public byte getToIdIndex() {
		if (toField != null) {
			return super.getToIdIndex();
		}
		else {
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(getFromEntityType());
			byte toIdIndex = metaData.getIdIndexByMemberName(toMember.getName());
			return toIdIndex;
		}
	}

	@Override
	public Member getFromMember() {
		return fromMember;
	}

	public void setFromMember(Member fromMember) {
		this.fromMember = fromMember;
	}

	@Override
	public Member getToMember() {
		return toMember;
	}

	public void setToMember(Member toMember) {
		this.toMember = toMember;
	}

	@Override
	public String toString() {
		return "DirectedExternalLink: " + getName();
	}
}
