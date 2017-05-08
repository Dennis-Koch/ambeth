package com.koch.ambeth.persistence.external.compositeid;

/*-
 * #%L
 * jambeth-test
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

public class CompositeIdEntity2 {
	protected int id1;

	protected Embedded id2;

	protected short aid1;

	protected Embedded aid2;

	protected String name;

	protected CompositeIdEntity2() {
		// Intended blank
	}

	public int getId1() {
		return id1;
	}

	public void setId1(int id1) {
		this.id1 = id1;
	}

	public Embedded getId2() {
		return id2;
	}

	public void setId2(Embedded id2) {
		this.id2 = id2;
	}

	public short getAid1() {
		return aid1;
	}

	public void setAid1(short aid1) {
		this.aid1 = aid1;
	}

	public Embedded getAid2() {
		return aid2;
	}

	public void setAid2(Embedded aid2) {
		this.aid2 = aid2;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
