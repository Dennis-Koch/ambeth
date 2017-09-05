package com.koch.ambeth.util.model;

import java.beans.Introspector;

/*-
 * #%L
 * jambeth-util
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

public interface IDataObject {
	String P_TO_BE_DELETED = "ToBeDeleted";

	String P_TO_BE_UPDATED = "ToBeUpdated";

	String P_TO_BE_CREATED = "ToBeCreated";

	String P_HAS_PENDING_CHANGES = "HasPendingChanges";

	String BEANS_TO_BE_DELETED = Introspector.decapitalize(P_TO_BE_DELETED);

	String BEANS_TO_BE_UPDATED = Introspector.decapitalize(P_TO_BE_UPDATED);

	String BEANS_TO_BE_CREATED = Introspector.decapitalize(P_TO_BE_CREATED);

	String BEANS_HAS_PENDING_CHANGES = Introspector.decapitalize(P_HAS_PENDING_CHANGES);

	boolean isToBeDeleted();

	boolean isToBeUpdated();

	boolean isToBeCreated();

	boolean hasPendingChanges();

	void setToBeDeleted(boolean toBeDeleted);

	void setToBeUpdated(boolean toBeUpdated);
}
