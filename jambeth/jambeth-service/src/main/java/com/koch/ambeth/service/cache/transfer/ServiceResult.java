package com.koch.ambeth.service.cache.transfer;

/*-
 * #%L
 * jambeth-service
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement(name = "ServiceResult", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceResult implements IServiceResult {
	@XmlElement
	protected List<IObjRef> objRefs;

	@XmlElement
	protected Object additionalInformation;

	public ServiceResult() {
		// Intended blank
	}

	public ServiceResult(List<IObjRef> objRefs) {
		this.objRefs = objRefs;
	}

	@Override
	public List<IObjRef> getObjRefs() {
		return objRefs;
	}

	public void setObjRefs(List<IObjRef> objRefs) {
		this.objRefs = objRefs;
	}

	@Override
	public Object getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(Object additionalInformation) {
		this.additionalInformation = additionalInformation;
	}
}
