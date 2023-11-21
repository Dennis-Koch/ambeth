package com.koch.ambeth.service.transfer;

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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.metadata.IDTOType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AmbethServiceException implements IDTOType {
	@XmlElement(required = false)
	protected String message;

	@XmlElement(required = false)
	protected String stackTrace;

	@XmlElement(required = false)
	protected String exceptionType;

	@XmlElement(required = false)
	protected AmbethServiceException cause;

	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public AmbethServiceException getCause() {
		return cause;
	}

	public void setCause(AmbethServiceException cause) {
		this.cause = cause;
	}
}
