package com.koch.ambeth.filter;

import java.util.Arrays;

/*-
 * #%L
 * jambeth-filter
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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.util.collections.EmptyList;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class CompositeFilterDescriptor<T> implements IFilterDescriptor<T> {
	@XmlElement
	protected Class<T> entityType;

	@XmlElement
	protected LogicalOperator logicalOperator;

	@SuppressWarnings({"unchecked", "rawtypes"})
	@XmlElement
	protected List<IFilterDescriptor<T>> childFilterDescriptors =
			(List) EmptyList.createTypedEmptyList(IFilterDescriptor.class);

	public CompositeFilterDescriptor() {
		// Intended blank. For XML serialization usage only
	}

	public CompositeFilterDescriptor(Class<T> entityType) {
		setEntityType(entityType);
	}

	@Override
	public Class<T> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<T> entityType) {
		this.entityType = entityType;
	}

	@Override
	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public CompositeFilterDescriptor<T> withLogicalOperator(LogicalOperator logicalOperator) {
		setLogicalOperator(logicalOperator);
		return this;
	}

	@Override
	public List<IFilterDescriptor<T>> getChildFilterDescriptors() {
		return childFilterDescriptors;
	}

	public void setChildFilterDescriptors(List<IFilterDescriptor<T>> childFilterDescriptors) {
		this.childFilterDescriptors = childFilterDescriptors;
	}

	public CompositeFilterDescriptor<T> withChildFilterDescriptors(
			List<IFilterDescriptor<T>> childFilterDescriptors) {
		setChildFilterDescriptors(childFilterDescriptors);
		return this;
	}

	@SuppressWarnings("unchecked")
	public CompositeFilterDescriptor<T> withChildFilterDescriptors(
			IFilterDescriptor<?>... childFilterDescriptors) {
		setChildFilterDescriptors(
				(List<IFilterDescriptor<T>>) (Object) Arrays.asList(childFilterDescriptors));
		return this;
	}

	@Override
	public String getMember() {
		return null;
	}

	@Override
	public FilterOperator getOperator() {
		return null;
	}

	@Override
	public List<String> getValue() {
		return null;
	}

	@Override
	public Boolean isCaseSensitive() {
		return null;
	}
}
