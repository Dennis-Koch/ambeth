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
import com.koch.ambeth.service.merge.model.IObjRef;

public class IdFilteredDataChangeListener extends UnfilteredDataChangeListener {
	public static IDataChangeListener create(IDataChangeListener dataChangeListener,
			Object[] interestedIds) {
		IdFilteredDataChangeListener dcListener = new IdFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedIds = interestedIds;
		return dcListener;
	}

	public static IEventListener createEventListener(IDataChangeListener dataChangeListener,
			Object[] interestedIds) {
		IdFilteredDataChangeListener dcListener = new IdFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedIds = interestedIds;
		return dcListener;
	}

	protected Object[] interestedIds;

	public Object[] getInterestedIds() {
		return interestedIds;
	}

	public void setInterestedIds(Object[] interestedIds) {
		this.interestedIds = interestedIds;
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId) {
		if (dataChange.isEmpty()) {
			return;
		}
		dataChange = dataChange.derive(IObjRef.PRIMARY_KEY_INDEX, interestedIds);
		if (dataChange.isEmpty()) {
			return;
		}
		dataChangeListener.dataChanged(dataChange, dispatchTime, sequenceId);
	}
}
