package com.koch.ambeth.merge.model;

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;

public class RelationUpdateItemBuild implements IRelationUpdateItem, IPrintable {
	protected String memberName;

	protected ISet<IObjRef> addedORIs = EmptySet.<IObjRef>emptySet();

	protected ISet<IObjRef> removedORIs = EmptySet.<IObjRef>emptySet();

	public RelationUpdateItemBuild(String memberName) {
		this.memberName = memberName;
	}

	@Override
	public String getMemberName() {
		return memberName;
	}

	public int getAddedCount() {
		return addedORIs.size();
	}

	public int getRemovedCount() {
		return removedORIs.size();
	}

	@Override
	public IObjRef[] getAddedORIs() {
		if (addedORIs.size() == 0) {
			return null;
		}
		return addedORIs.toArray(IObjRef.class);
	}

	@Override
	public IObjRef[] getRemovedORIs() {
		if (removedORIs.size() == 0) {
			return null;
		}
		return removedORIs.toArray(IObjRef.class);
	}

	public void addObjRef(IObjRef objRef) {
		if (addedORIs.size() == 0) {
			addedORIs = new HashSet<IObjRef>();
		}
		addedORIs.add(objRef);
	}

	public void addObjRefs(IObjRef[] objRefs) {
		for (IObjRef objRef : objRefs) {
			addObjRef(objRef);
		}
	}

	public void addObjRefs(List<IObjRef> objRefs) {
		for (int a = 0, size = objRefs.size(); a < size; a++) {
			addObjRef(objRefs.get(a));
		}
	}

	public void addObjRefs(Collection<IObjRef> objRefs) {
		for (IObjRef objRef : objRefs) {
			addObjRef(objRef);
		}
	}

	public void removeObjRef(IObjRef objRef) {
		if (removedORIs.size() == 0) {
			removedORIs = new HashSet<IObjRef>();
		}
		removedORIs.add(objRef);
	}

	public void removeObjRefs(IObjRef[] objRefs) {
		for (IObjRef objRef : objRefs) {
			removeObjRef(objRef);
		}
	}

	public void removeObjRefs(List<IObjRef> objRefs) {
		for (int a = objRefs.size(); a-- > 0;) {
			removeObjRef(objRefs.get(a));
		}
	}

	public void removeObjRefs(Collection<IObjRef> objRefs) {
		for (IObjRef objRef : objRefs) {
			removeObjRef(objRef);
		}
	}

	public IRelationUpdateItem buildRUI() {
		RelationUpdateItem rui = new RelationUpdateItem();
		rui.setMemberName(memberName);
		rui.setAddedORIs(getAddedORIs());
		rui.setRemovedORIs(getRemovedORIs());
		return rui;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("RUI: MemberName=").append(getMemberName());
		IObjRef[] addedORIs = getAddedORIs();
		IObjRef[] removedORIs = getRemovedORIs();
		if (addedORIs != null && addedORIs.length > 0) {
			sb.append(" AddedORIs=");
			Arrays.toString(sb, addedORIs);
		}
		if (removedORIs != null && removedORIs.length > 0) {
			sb.append(" RemovedORIs=");
			Arrays.toString(sb, removedORIs);
		}
	}
}
