package com.koch.ambeth.datachange;

/*-
 * #%L
 * jambeth-datachange
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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListener;

public class TypeFilteredDataChangeListener extends UnfilteredDataChangeListener {
	public static IDataChangeListener create(IDataChangeListener dataChangeListener,
			Class<?>[] interestedTypes) {
		TypeFilteredDataChangeListener dcListener = new TypeFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedTypes = interestedTypes;
		return dcListener;
	}

	public static IEventListener createEventListener(IDataChangeListener dataChangeListener,
			Class<?>[] interestedTypes) {
		TypeFilteredDataChangeListener dcListener = new TypeFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedTypes = interestedTypes;
		return dcListener;
	}

	protected Class<?>[] interestedTypes;

	public Class<?>[] getInterestedTypes() {
		return interestedTypes;
	}

	public void setInterestedTypes(Class<?>[] interestedTypes) {
		this.interestedTypes = interestedTypes;
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId) {
		if (dataChange.isEmpty()) {
			return;
		}
		dataChange = dataChange.derive(interestedTypes);
		if (dataChange.isEmpty()) {
			return;
		}
		dataChangeListener.dataChanged(dataChange, dispatchTime, sequenceId);
	}
}
