package com.koch.ambeth.audit.model;

/*-
 * #%L
 * jambeth-audit
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

import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.util.annotation.Interning;

/**
 * Describes the addition or removal of a relationship between two entity instances. If a
 * relationship is bidirectional there will also be two instances of this to describe the addition
 * or removal from "both sides". Note that as a user you do not have to explicitly change the
 * relationship from both sides in any case: Bidirectional implications will be resolved and trailed
 * automatically.
 */
@Audited(false)
public interface IAuditedEntityRelationProperty {
	String Entity = "Entity";

	String Items = "Items";

	String Name = "Name";

	String Order = "Order";

	IAuditedEntity getEntity();

	int getOrder();

	@Interning // it can be assumed that the variance of distinct entity property names is limited
	String getName();

	List<? extends IAuditedEntityRelationPropertyItem> getItems();
}
