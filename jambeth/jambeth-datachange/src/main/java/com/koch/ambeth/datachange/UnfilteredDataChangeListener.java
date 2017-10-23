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
import com.koch.ambeth.datachange.model.IPostDataChange;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IDisposableBean;

public class UnfilteredDataChangeListener
		implements IEventListener, IDataChangeListener, IDisposableBean {
	public static final String P_DATA_CHANGE_LISTENER = "DataChangeListener";

	public static IDataChangeListener create(IDataChangeListener dataChangeListener) {
		UnfilteredDataChangeListener dcListener = new UnfilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		return dcListener;
	}

	public static IEventListener createEventListener(IDataChangeListener dataChangeListener) {
		UnfilteredDataChangeListener dcListener = new UnfilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		return dcListener;
	}

	protected IDataChangeListener dataChangeListener;

	@Override
	public void destroy() throws Throwable {
		dataChangeListener = null;
	}

	public void setDataChangeListener(IDataChangeListener dataChangeListener) {
		this.dataChangeListener = dataChangeListener;
	}

	@Override
	public final void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
		if (eventObject instanceof IPostDataChange) {
			eventObject = ((IPostDataChange) eventObject).getDataChange();
		}
		if (eventObject instanceof IDataChange) {
			dataChanged((IDataChange) eventObject, dispatchTime, sequenceId);
		}
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId) {
		if (dataChange.isEmpty()) {
			return;
		}
		dataChangeListener.dataChanged(dataChange, dispatchTime, sequenceId);
	}
}
