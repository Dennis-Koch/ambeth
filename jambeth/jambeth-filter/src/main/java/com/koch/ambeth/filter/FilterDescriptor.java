package com.koch.ambeth.filter;

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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The FilterDescriptor is used for querying filtered results
 *
 * <p>
 * Java class for FilterDescriptorType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class FilterDescriptor<T> implements IFilterDescriptor<T> {

	@XmlElement
	protected Class<? extends T> entityType;

	@XmlElement
	protected String member;

	@XmlElement
	protected List<String> value;

	@XmlElement
	protected Boolean caseSensitive;

	@XmlElement
	protected FilterOperator operator;

	public FilterDescriptor() {
		// Intended blank. For XML serialization usage only
	}

	public FilterDescriptor(Class<? extends T> entityType) {
		setEntityType(entityType);
	}

	@Override
	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public FilterDescriptor<T> withMember(String member) {
		setMember(member);
		return this;
	}

	@Override
	public Class<? extends T> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<? extends T> entityType) {
		this.entityType = entityType;
	}

	@Override
	public List<String> getValue() {
		if (value == null) {
			value = new ArrayList<>();
		}
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public FilterDescriptor<T> withValue(String value) {
		if (this.value == null) {
			this.value = new ArrayList<>();
		}
		this.value.add(value);
		return this;
	}

	public FilterDescriptor<T> withValues(List<String> value) {
		setValue(value);
		return this;
	}

	@Override
	public Boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public FilterDescriptor<T> withCaseSensitive(Boolean caseSensitive) {
		setCaseSensitive(caseSensitive);
		return this;
	}

	@Override
	public FilterOperator getOperator() {
		return operator;
	}

	public void setOperator(FilterOperator operator) {
		this.operator = operator;
	}

	public FilterDescriptor<T> withOperator(FilterOperator operator) {
		setOperator(operator);
		return this;
	}

	@Override
	public LogicalOperator getLogicalOperator() {
		return null;
	}

	@Override
	public List<IFilterDescriptor<T>> getChildFilterDescriptors() {
		return null;
	}
}
