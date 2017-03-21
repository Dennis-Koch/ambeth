package com.koch.ambeth.server.helloworld.transfer;

/*-
 * #%L
 * jambeth-server-helloworld
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.util.annotation.ParentChild;

@XmlRootElement(namespace = "HelloWorld")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmbeddedObject {
	@XmlElement
	protected TestEntity2 relationOfEmbeddedObject;

	@XmlElement
	protected String name;

	@XmlElement
	protected int value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@ParentChild
	public TestEntity2 getRelationOfEmbeddedObject() {
		return relationOfEmbeddedObject;
	}

	public void setRelationOfEmbeddedObject(TestEntity2 relationOfEmbeddedObject) {
		this.relationOfEmbeddedObject = relationOfEmbeddedObject;
	}
}
