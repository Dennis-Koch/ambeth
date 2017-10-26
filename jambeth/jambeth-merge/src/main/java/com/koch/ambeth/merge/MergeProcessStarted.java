package com.koch.ambeth.merge;

import java.util.ArrayList;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.merge.model.ICUDResult;

public class MergeProcessStarted
		implements IMergeProcessContent, ProceedWithMergeHook, MergeFinishedCallback,
		DataChangeReceivedCallback {
	ArrayList<Object> mergeList;

	ArrayList<Object> deleteList;

	ArrayList<ProceedWithMergeHook> hooks;

	ArrayList<DataChangeReceivedCallback> dataChangeCallbacks;

	ArrayList<MergeFinishedCallback> callbacks;

	boolean addNewEntitiesToCache = true;

	boolean deepMerge = true;

	private MergeProcess mergeProcess;

	public MergeProcessStarted(MergeProcess mergeProcess) {
		this.mergeProcess = mergeProcess;
	}

	@Override
	public IMergeProcessContent merge(Object objectsToMerge) {
		if (objectsToMerge == null) {
			return this;
		}
		if (mergeList == null) {
			mergeList = new ArrayList<>();
		}
		mergeList.add(objectsToMerge);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IMergeProcessContent merge(T objectsToMerge1, T... objectsToMerge2) {
		return merge(new Object[] {objectsToMerge1, objectsToMerge2});
	}

	@Override
	public IMergeProcessContent delete(Object objectsToDelete) {
		if (objectsToDelete == null) {
			return this;
		}
		if (deleteList == null) {
			deleteList = new ArrayList<>();
		}
		deleteList.add(objectsToDelete);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IMergeProcessContent delete(T objectsToDelete1, T... objectsToDelete2) {
		return delete(new Object[] {objectsToDelete1, objectsToDelete2});
	}

	@Override
	public IMergeProcessContent onLocalDiff(ProceedWithMergeHook hook) {
		if (hook == null) {
			return this;
		}
		if (hooks == null) {
			hooks = new ArrayList<>();
		}
		hooks.add(hook);
		return this;
	}

	@Override
	public IMergeProcessContent onDataChange(DataChangeReceivedCallback callback) {
		if (callback == null) {
			return this;
		}
		if (dataChangeCallbacks == null) {
			dataChangeCallbacks = new ArrayList<>();
		}
		dataChangeCallbacks.add(callback);
		return this;
	}

	@Override
	public IMergeProcessContent onSuccess(MergeFinishedCallback callback) {
		if (callback == null) {
			return this;
		}
		if (callbacks == null) {
			callbacks = new ArrayList<>();
		}
		callbacks.add(callback);
		return this;
	}

	@Override
	public IMergeProcessContent suppressNewEntitiesAddedToCache() {
		addNewEntitiesToCache = false;
		return this;
	}

	@Override
	public IMergeProcessContent shallow() {
		deepMerge = false;
		return this;
	}

	@Override
	public void finish() {
		if (mergeList != null || deleteList != null) {
			mergeProcess.ensureMergeOutOfGui(mergeList, deleteList, //
					hooks != null ? this : null, //
					dataChangeCallbacks != null ? this : null, //
					callbacks != null ? this : null,
					addNewEntitiesToCache, deepMerge);
		}
		mergeProcess = null;
	}

	@Override
	public boolean checkToProceed(ICUDResult result) {
		for (ProceedWithMergeHook hook : hooks) {
			if (!hook.checkToProceed(result)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void handleDataChange(IDataChange dataChange) {
		for (DataChangeReceivedCallback callback : dataChangeCallbacks) {
			callback.handleDataChange(dataChange);
		}
	}

	@Override
	public void invoke(boolean success) {
		for (MergeFinishedCallback callback : callbacks) {
			callback.invoke(success);
		}
	}
}
