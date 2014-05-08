package de.osthus.ant.task.createclasspath.type;

import org.apache.tools.ant.types.DataType;

public class ResourceLinkType extends DataType {

	private String local;
	private String target;
	
	public ResourceLinkType() {
		super();
	}
	
	public ResourceLinkType(String local, String target) {
		this();
		this.local = local;
		this.target = target;
	}
	
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return "ResourceLink[local="+local+", target="+target+"]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((local == null) ? 0 : local.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( !(obj instanceof ResourceLinkType) ) {
			return false;
		}
		ResourceLinkType other = (ResourceLinkType) obj;		
		return this.local.equals(other.local) && this.target.equals(other.target);
	}
	

}
