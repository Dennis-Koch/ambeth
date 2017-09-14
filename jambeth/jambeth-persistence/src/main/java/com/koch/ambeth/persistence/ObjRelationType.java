package com.koch.ambeth.persistence;

import java.util.Objects;

import com.koch.ambeth.util.IPrintable;

public class ObjRelationType implements IPrintable {
	protected final Class<?> entityType;

	protected final byte idIndex;

	protected final String memberName;

	public ObjRelationType(Class<?> entityType, byte idIndex, String memberName) {
		this.entityType = entityType;
		this.idIndex = idIndex;
		this.memberName = memberName;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public byte getIdIndex() {
		return idIndex;
	}

	public String getMemberName() {
		return memberName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ObjRelationType)) {
			return false;
		}
		ObjRelationType other = (ObjRelationType) obj;
		return Objects.equals(getEntityType(), other.getEntityType())
				&& getIdIndex() == other.getIdIndex()
				&& Objects.equals(getMemberName(), other.getMemberName());
	}

	@Override
	public int hashCode() {
		return getEntityType().hashCode() ^ getIdIndex() ^ getMemberName().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	private String getClassName() {
		return "ObjRel";
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(getClassName()).append(" idIndex=").append(idIndex).append(" type=")
				.append(entityType.getName()).append(" property=").append(memberName);
	}
}
