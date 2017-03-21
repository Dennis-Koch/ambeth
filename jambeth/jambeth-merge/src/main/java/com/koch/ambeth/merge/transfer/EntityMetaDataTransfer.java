package com.koch.ambeth.merge.transfer;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

@XmlRootElement(name = "EntityMetaDataTransfer", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntityMetaDataTransfer {
	@XmlElement
	protected Class<?> entityType;

	@XmlElement
	protected Class<?>[] typesRelatingToThis;

	@XmlElement
	protected Class<?>[] typesToCascadeDelete;

	@XmlElement
	protected String[] relationMemberNames;

	@XmlElement
	protected String[] primitiveMemberNames;

	@XmlElement
	protected String[] alternateIdMemberNames;

	@XmlElement
	protected String versionMemberName;

	@XmlElement
	protected String idMemberName;

	@XmlElement
	protected int[][] alternateIdMemberIndicesInPrimitives;

	@XmlElement
	protected String createdOnMemberName;

	@XmlElement
	protected String createdByMemberName;

	@XmlElement
	protected String updatedOnMemberName;

	@XmlElement
	protected String updatedByMemberName;

	@XmlElement
	protected String[] mergeRelevantNames;

	public Class<?> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<?> entityType) {
		this.entityType = entityType;
	}

	public Class<?>[] getTypesRelatingToThis() {
		return typesRelatingToThis;
	}

	public void setTypesRelatingToThis(Class<?>[] typesRelatingToThis) {
		this.typesRelatingToThis = typesRelatingToThis;
	}

	public Class<?>[] getTypesToCascadeDelete() {
		return typesToCascadeDelete;
	}

	public void setTypesToCascadeDelete(Class<?>[] typesToCascadeDelete) {
		this.typesToCascadeDelete = typesToCascadeDelete;
	}

	public String[] getMergeRelevantNames() {
		return mergeRelevantNames;
	}

	public void setMergeRelevantNames(String[] mergeRelevantNames) {
		this.mergeRelevantNames = mergeRelevantNames;
	}

	public String[] getRelationMemberNames() {
		return relationMemberNames;
	}

	public void setRelationMemberNames(String[] relationMemberNames) {
		this.relationMemberNames = relationMemberNames;
	}

	public String[] getPrimitiveMemberNames() {
		return primitiveMemberNames;
	}

	public void setPrimitiveMemberNames(String[] primitiveMemberNames) {
		this.primitiveMemberNames = primitiveMemberNames;
	}

	public String[] getAlternateIdMemberNames() {
		return alternateIdMemberNames;
	}

	public void setAlternateIdMemberNames(String[] alternateIdMemberNames) {
		this.alternateIdMemberNames = alternateIdMemberNames;
	}

	public String getVersionMemberName() {
		return versionMemberName;
	}

	public void setVersionMemberName(String versionMemberName) {
		this.versionMemberName = versionMemberName;
	}

	public String getIdMemberName() {
		return idMemberName;
	}

	public void setIdMemberName(String idMemberName) {
		this.idMemberName = idMemberName;
	}

	public ITypeInfoItem getIdMemberByIdIndex(byte idIndex) {
		throw new UnsupportedOperationException();
	}

	public int[][] getAlternateIdMemberIndicesInPrimitives() {
		return alternateIdMemberIndicesInPrimitives;
	}

	public void setAlternateIdMemberIndicesInPrimitives(
			int[][] alternateIdMemberIndicesInPrimitives) {
		this.alternateIdMemberIndicesInPrimitives = alternateIdMemberIndicesInPrimitives;
	}

	public ITypeInfoItem getIdMember() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem getVersionMember() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem[] getAlternateIdMembers() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem getCreatedOnMember() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem getCreatedByMember() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem getUpdatedOnMember() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem getUpdatedByMember() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem[] getPrimitiveMembers() {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem[] getRelationMembers() {
		throw new UnsupportedOperationException();
	}

	public boolean isMergeRelevant(ITypeInfoItem primitiveMember) {
		throw new UnsupportedOperationException();
	}

	public ITypeInfoItem getMemberByName(String memberName) {
		throw new UnsupportedOperationException();
	}

	public int getIndexByName(String memberName) {
		throw new UnsupportedOperationException();
	}

	public boolean isCascadeDelete(Class<?> other) {
		throw new UnsupportedOperationException();
	}

	public String getCreatedOnMemberName() {
		return createdOnMemberName;
	}

	public void setCreatedOnMemberName(String createdOnMemberName) {
		this.createdOnMemberName = createdOnMemberName;
	}

	public String getCreatedByMemberName() {
		return createdByMemberName;
	}

	public void setCreatedByMemberName(String createdByMemberName) {
		this.createdByMemberName = createdByMemberName;
	}

	public String getUpdatedOnMemberName() {
		return updatedOnMemberName;
	}

	public void setUpdatedOnMemberName(String updatedOnMemberName) {
		this.updatedOnMemberName = updatedOnMemberName;
	}

	public String getUpdatedByMemberName() {
		return updatedByMemberName;
	}

	public void setUpdatedByMemberName(String updatedByMemberName) {
		this.updatedByMemberName = updatedByMemberName;
	}

}
