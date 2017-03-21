package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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
import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;

public class CUDResultComparer implements ICUDResultComparer {
	public static class CUDResultDiff {
		public final boolean doFullDiff;

		public final ICUDResult left;

		public final ICUDResult right;

		public final IList<IChangeContainer> diffChanges;

		public final IList<Object> originalRefs;

		private IChangeContainer leftContainer;

		public String memberName;

		public CreateOrUpdateContainerBuild containerBuild;

		public RelationUpdateItemBuild relationBuild;

		protected boolean hasChanges;

		private final ICUDResultHelper cudResultHelper;

		private final IEntityMetaDataProvider entityMetaDataProvider;

		private HashMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap;
		private HashMap<Class<?>, HashMap<String, Integer>> typeToPrimitiveMemberNameToIndexMap;

		public CUDResultDiff(ICUDResult left, ICUDResult right, boolean doFullDiff,
				ICUDResultHelper cudResultHelper, IEntityMetaDataProvider entityMetaDataProvider) {
			this.doFullDiff = doFullDiff;
			this.left = left;
			this.right = right;
			this.cudResultHelper = cudResultHelper;
			this.entityMetaDataProvider = entityMetaDataProvider;
			if (doFullDiff) {
				diffChanges = new ArrayList<>();
				originalRefs = new ArrayList<>();
			}
			else {
				diffChanges = EmptyList.<IChangeContainer>getInstance();
				originalRefs = EmptyList.<Object>getInstance();
			}
		}

		public void setHasChanges(boolean hasChanges) {
			this.hasChanges = hasChanges;
		}

		public boolean hasChanges() {
			return hasChanges || diffChanges.size() > 0;
		}

		public CreateOrUpdateContainerBuild updateContainerBuild() {
			if (containerBuild != null) {
				return containerBuild;
			}
			IEntityMetaData metaData =
					entityMetaDataProvider.getMetaData(getLeftContainer().getReference().getRealType());
			containerBuild =
					new CreateOrUpdateContainerBuild(metaData, leftContainer instanceof CreateContainer,
							getOrCreateRelationMemberNameToIndexMap(metaData.getEntityType()),
							getOrCreatePrimitiveMemberNameToIndexMap(metaData.getEntityType()), cudResultHelper);
			containerBuild.setReference(getLeftContainer().getReference());
			return containerBuild;
		}

		public RelationUpdateItemBuild updateRelationBuild() {
			if (relationBuild != null) {
				return relationBuild;
			}
			relationBuild = new RelationUpdateItemBuild(memberName);
			return relationBuild;
		}

		protected HashMap<String, Integer> getOrCreateRelationMemberNameToIndexMap(
				Class<?> entityType) {
			if (typeToMemberNameToIndexMap == null) {
				typeToMemberNameToIndexMap = new HashMap<>();
			}
			HashMap<String, Integer> memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
			if (memberNameToIndexMap != null) {
				return memberNameToIndexMap;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			RelationMember[] relationMembers = metaData.getRelationMembers();
			memberNameToIndexMap = HashMap.create(relationMembers.length);
			for (int a = relationMembers.length; a-- > 0;) {
				memberNameToIndexMap.put(relationMembers[a].getName(), Integer.valueOf(a));
			}
			typeToMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
			return memberNameToIndexMap;
		}

		protected HashMap<String, Integer> getOrCreatePrimitiveMemberNameToIndexMap(
				Class<?> entityType) {
			if (typeToPrimitiveMemberNameToIndexMap == null) {
				typeToPrimitiveMemberNameToIndexMap = new HashMap<>();
			}
			HashMap<String, Integer> memberNameToIndexMap =
					typeToPrimitiveMemberNameToIndexMap.get(entityType);
			if (memberNameToIndexMap != null) {
				return memberNameToIndexMap;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
			memberNameToIndexMap = HashMap.create(primitiveMembers.length);
			for (int a = primitiveMembers.length; a-- > 0;) {
				memberNameToIndexMap.put(primitiveMembers[a].getName(), Integer.valueOf(a));
			}
			typeToPrimitiveMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
			return memberNameToIndexMap;
		}

		public IChangeContainer getLeftContainer() {
			return leftContainer;
		}

		public void setLeftContainer(IChangeContainer leftContainer) {
			if (leftContainer != null && containerBuild != null) {
				throw new IllegalStateException();
			}
			this.leftContainer = leftContainer;
		}

	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICUDResultHelper cudResultHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected boolean equalsChangeContainer(CUDResultDiff cudResultDiff, IChangeContainer left,
			IChangeContainer right) {
		if (left.getClass() != right.getClass()) {
			throw new IllegalStateException("Must never happen");
		}
		cudResultDiff.setLeftContainer(left);
		try {
			if (left instanceof CreateContainer) {
				CreateContainer leftCreate = (CreateContainer) left;
				CreateContainer rightCreate = (CreateContainer) right;
				boolean isEqual =
						equalsPUIs(cudResultDiff, leftCreate.getPrimitives(), rightCreate.getPrimitives());
				if (!isEqual) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
				}
				isEqual &= equalsRUIs(cudResultDiff, leftCreate.getRelations(), rightCreate.getRelations());
				if (!isEqual) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
				}
				return isEqual;
			}
			if (left instanceof UpdateContainer) {
				UpdateContainer leftUpdate = (UpdateContainer) left;
				UpdateContainer rightUpdate = (UpdateContainer) right;
				boolean isEqual =
						equalsPUIs(cudResultDiff, leftUpdate.getPrimitives(), rightUpdate.getPrimitives());
				if (!isEqual) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
				}
				isEqual &= equalsRUIs(cudResultDiff, leftUpdate.getRelations(), rightUpdate.getRelations());
				if (!isEqual) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
				}
				return isEqual;
			}
			// a DeleteContainer is only compared by the reference. But we know that this is already equal
			// since we entered this method
			return true;
		}
		finally {
			cudResultDiff.setLeftContainer(null);
		}
	}

	@Override
	public boolean equalsCUDResult(ICUDResult left, ICUDResult right) {
		CUDResultDiff diff =
				new CUDResultDiff(left, right, false, cudResultHelper, entityMetaDataProvider);
		return equalsCUDResult(diff);
	}

	@Override
	public ICUDResult diffCUDResult(ICUDResult left, ICUDResult right) {
		CUDResultDiff diff =
				new CUDResultDiff(left, right, true, cudResultHelper, entityMetaDataProvider);
		equalsCUDResult(diff);

		if (!diff.hasChanges()) {
			return null; // null means empty diff
		}
		IList<IChangeContainer> diffChanges = diff.diffChanges;
		for (int a = diffChanges.size(); a-- > 0;) {
			IChangeContainer changeContainer = diffChanges.get(a);
			if (!(changeContainer instanceof CreateOrUpdateContainerBuild)) {
				continue;
			}
			diffChanges.set(a, ((CreateOrUpdateContainerBuild) changeContainer).build());
		}
		return new CUDResult(diffChanges, diff.originalRefs);
	}

	protected boolean equalsCUDResult(CUDResultDiff cudResultDiff) {
		ICUDResult left = cudResultDiff.left;
		ICUDResult right = cudResultDiff.right;
		List<Object> leftRefs = left.getOriginalRefs();
		List<Object> rightRefs = right.getOriginalRefs();
		if (leftRefs.size() != rightRefs.size()) {
			if (!cudResultDiff.doFullDiff) {
				return false;
			}
		}
		List<IChangeContainer> leftChanges = left.getAllChanges();
		List<IChangeContainer> rightChanges = right.getAllChanges();
		IdentityHashMap<Object, Integer> rightMap = IdentityHashMap.create(rightRefs.size());
		for (int a = rightRefs.size(); a-- > 0;) {
			rightMap.put(rightRefs.get(a), Integer.valueOf(a));
		}
		for (int a = leftRefs.size(); a-- > 0;) {
			Object leftEntity = leftRefs.get(a);
			Integer rightIndex = rightMap.remove(leftEntity);
			if (rightIndex == null) {
				if (!cudResultDiff.doFullDiff) {
					return false;
				}
				cudResultDiff.diffChanges.add(leftChanges.get(a));
				cudResultDiff.originalRefs.add(leftEntity);
				continue;
			}
			if (!equalsChangeContainer(cudResultDiff, leftChanges.get(a),
					rightChanges.get(rightIndex.intValue()))) {
				if (!cudResultDiff.doFullDiff) {
					if (cudResultDiff.containerBuild != null) {
						throw new IllegalStateException();
					}
					return false;
				}
				cudResultDiff.diffChanges.add(cudResultDiff.containerBuild);
				cudResultDiff.originalRefs.add(rightRefs.get(rightIndex.intValue()));
				cudResultDiff.containerBuild = null;
			}
			else if (cudResultDiff.containerBuild != null) {
				throw new IllegalStateException();
			}
		}
		if (rightMap.size() == 0) {
			return true;
		}
		for (Entry<Object, Integer> entry : rightMap) {
			Object rightRef = entry.getKey();
			int rightIndex = entry.getValue().intValue();
			IChangeContainer rightChange = rightChanges.get(rightIndex);
			cudResultDiff.diffChanges.add(rightChange);
			cudResultDiff.originalRefs.add(rightRef);
		}
		return false;
	}

	protected boolean equalsPUIs(CUDResultDiff cudResultDiff, IPrimitiveUpdateItem[] left,
			IPrimitiveUpdateItem[] right) {
		if (left == null || left.length == 0) {
			if (right == null || right.length == 0) {
				return true;
			}
			if (!cudResultDiff.doFullDiff) {
				return false;
			}
			CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
			for (IPrimitiveUpdateItem rightPui : right) {
				containerBuild.addPrimitive(rightPui);
			}
			return false;
		}
		if (right == null || right.length == 0) {
			throw new IllegalStateException("Must never happen");
		}
		if (left.length != right.length) {
			if (!cudResultDiff.doFullDiff) {
				return false;
			}
			int leftIndex = left.length - 1;
			for (int rightIndex = right.length; rightIndex-- > 0;) {
				IPrimitiveUpdateItem leftPui = leftIndex >= 0 ? left[leftIndex] : null;
				IPrimitiveUpdateItem rightPui = right[rightIndex];
				if (leftPui == null || !leftPui.getMemberName().equals(rightPui.getMemberName())) {
					CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
					containerBuild.addPrimitive(rightPui);
					continue;
				}
				if (!equalsPUI(cudResultDiff, leftPui, rightPui)) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
					CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
					containerBuild.addPrimitive(rightPui);
				}
				leftIndex--;
			}
			return false;
		}
		boolean isEqual = true;
		for (int a = left.length; a-- > 0;) {
			IPrimitiveUpdateItem rightPui = right[a];
			if (!equalsPUI(cudResultDiff, left[a], rightPui)) {
				if (!cudResultDiff.doFullDiff) {
					return false;
				}
				CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
				containerBuild.addPrimitive(rightPui);
				isEqual = false;
			}
		}
		return isEqual;
	}

	protected boolean equalsPUI(CUDResultDiff cudResultDiff, IPrimitiveUpdateItem left,
			IPrimitiveUpdateItem right) {
		if (EqualsUtil.equals(left.getNewValue(), right.getNewValue())) {
			return true;
		}
		if (!cudResultDiff.doFullDiff) {
			return false;
		}
		CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
		containerBuild.addPrimitive(right);
		return false;
	}

	protected boolean equalsRUIs(CUDResultDiff cudResultDiff, IRelationUpdateItem[] left,
			IRelationUpdateItem[] right) {
		if (left == null || left.length == 0) {
			if (right == null || right.length == 0) {
				return true;
			}
			if (cudResultDiff.doFullDiff) {
				CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
				for (IRelationUpdateItem rightRui : right) {
					containerBuild.addRelation(rightRui);
				}
			}
			return false;
		}
		if (right == null || right.length == 0) {
			throw new IllegalStateException("Must never happen");
		}
		if (left.length != right.length) {
			if (!cudResultDiff.doFullDiff) {
				return false;
			}
			int leftIndex = left.length - 1;
			for (int rightIndex = right.length; rightIndex-- > 0;) {
				IRelationUpdateItem leftRui = leftIndex >= 0 ? left[leftIndex] : null;
				IRelationUpdateItem rightRui = right[rightIndex];
				if (leftRui == null || !leftRui.getMemberName().equals(rightRui.getMemberName())) {
					CreateOrUpdateContainerBuild containerBuild = cudResultDiff.updateContainerBuild();
					containerBuild.addRelation(rightRui);
					continue;
				}
				if (!equalsRUI(cudResultDiff, leftRui, rightRui)) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
				}
				leftIndex--;
			}
			return false;
		}
		boolean isEqual = true;
		for (int a = left.length; a-- > 0;) {
			IRelationUpdateItem rightPui = right[a];
			if (!equalsRUI(cudResultDiff, left[a], rightPui)) {
				if (!cudResultDiff.doFullDiff) {
					return false;
				}
				isEqual = false;
			}
		}
		return isEqual;
	}

	protected boolean equalsRUI(CUDResultDiff cudResultDiff, IRelationUpdateItem left,
			IRelationUpdateItem right) {
		// we do NOT have to check each relational ObjRef because IF an objRef is in the scope it must
		// not be removed afterwards
		// so we know by design that the arrays can only grow

		try {
			IObjRef[] leftORIs = left.getAddedORIs();
			IObjRef[] rightORIs = right.getAddedORIs();

			if (leftORIs == null) {
				if (rightORIs != null) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
					RelationUpdateItemBuild relationBuild = cudResultDiff.updateRelationBuild();
					relationBuild.addObjRefs(rightORIs);
				}
			}
			else if (rightORIs == null) {
				throw new IllegalStateException("Must never happen");
			}
			else if (leftORIs.length != rightORIs.length) {
				if (!cudResultDiff.doFullDiff) {
					return false;
				}
				LinkedHashSet<IObjRef> added = new LinkedHashSet<>(leftORIs);
				added.removeAll(rightORIs);

				RelationUpdateItemBuild relationBuild = cudResultDiff.updateRelationBuild();
				relationBuild.addObjRefs(added);
			}
			leftORIs = left.getRemovedORIs();
			rightORIs = right.getRemovedORIs();
			if (leftORIs == null) {
				if (rightORIs != null) {
					if (!cudResultDiff.doFullDiff) {
						return false;
					}
					RelationUpdateItemBuild relationBuild = cudResultDiff.updateRelationBuild();
					relationBuild.removeObjRefs(rightORIs);
				}
			}
			else if (rightORIs == null) {
				throw new IllegalStateException("Must never happen");
			}
			else if (leftORIs.length != rightORIs.length) {
				if (!cudResultDiff.doFullDiff) {
					return false;
				}
				LinkedHashSet<IObjRef> removed = new LinkedHashSet<>(leftORIs);
				removed.removeAll(rightORIs);

				RelationUpdateItemBuild relationBuild = cudResultDiff.updateRelationBuild();
				relationBuild.removeObjRefs(removed);
			}
			return true;
		}
		finally {
			cudResultDiff.relationBuild = null;
		}
	}
}
