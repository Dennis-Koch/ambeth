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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class PagingRequest implements IPagingRequest {
	@XmlElement(name = "Number", required = true)
	protected int number;

	@XmlElement(name = "Size", required = true)
	protected int size;

	@Override
	public int getNumber() {
		return number;
	}

	/**
	 * 0 based paging index.
	 *
	 * @param number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * 0 based paging index.
	 *
	 * @param number
	 */
	public PagingRequest withNumber(int number) {
		setNumber(number);
		return this;
	}

	@Override
	public int getSize() {
		return size;
	}

	/**
	 * @param size Max. item count per page
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @param size Max. item count per page
	 */
	public PagingRequest withSize(int size) {
		setSize(size);
		return this;
	}
}
