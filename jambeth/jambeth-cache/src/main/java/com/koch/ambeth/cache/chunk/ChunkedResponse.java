package com.koch.ambeth.cache.chunk;

/*-
 * #%L
 * jambeth-cache
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

public class ChunkedResponse implements IChunkedResponse {
	protected IChunkedRequest reference;
	protected byte[] payload;
	protected boolean deflated;
	protected int payloadSize;

	/**
	 * Needed for serialization
	 */
	public ChunkedResponse() {
		super();
	}

	public ChunkedResponse(IChunkedRequest reference, byte[] payload, boolean deflated,
			int payloadSize) {
		super();
		this.reference = reference;
		this.payload = payload;
		this.deflated = deflated;
		this.payloadSize = payloadSize;
	}

	@Override
	public IChunkedRequest getReference() {
		return reference;
	}

	public void setReference(IChunkedRequest reference) {
		this.reference = reference;
	}

	@Override
	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	@Override
	public int getPayloadSize() {
		return payloadSize;
	}

	public void setPayloadSize(int payloadSize) {
		this.payloadSize = payloadSize;
	}

	@Override
	public boolean isDeflated() {
		return deflated;
	}

	public void setDeflated(boolean deflated) {
		this.deflated = deflated;
	}
}
