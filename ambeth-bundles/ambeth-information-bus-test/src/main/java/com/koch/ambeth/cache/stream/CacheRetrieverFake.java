package com.koch.ambeth.cache.stream;

/*-
 * #%L
 * jambeth-cache-test
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.koch.ambeth.cache.chunk.ChunkedResponse;
import com.koch.ambeth.cache.chunk.IChunkProvider;
import com.koch.ambeth.cache.chunk.IChunkedRequest;
import com.koch.ambeth.cache.chunk.IChunkedResponse;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class CacheRetrieverFake implements ICacheService, IChunkProvider {
	public Map<IObjRef, ILoadContainer> entities = new HashMap<>();

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
		List<ILoadContainer> entities = new ArrayList<>(orisToLoad.size());

		for (int i = orisToLoad.size(); i-- > 0;) {
			ILoadContainer lc = this.entities.get(orisToLoad.get(i));
			if (lc != null) {
				entities.add(lc);
			}
		}

		return entities;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription rootServiceContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IChunkedResponse> getChunkedContents(List<IChunkedRequest> chunkedRequests) {
		ArrayList<IChunkedResponse> chunkedResponses =
				new ArrayList<>(chunkedRequests.size());

		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(bos, deflater);
		byte[] buffer = new byte[1];

		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		for (int i = chunkedRequests.size(); i-- > 0;) {
			IChunkedRequest chunkedRequest = chunkedRequests.get(i);
			IObjRelation objRelation = chunkedRequest.getObjRelation();
			ILoadContainer lc = entities.get(objRelation.getObjRefs()[0]);
			if (lc == null) {
				continue;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRelation.getRealType());
			int index = metaData.getIndexByPrimitiveName(objRelation.getMemberName());
			Object requestedValue = lc.getPrimitives()[index];

			IBinaryInputStream payloadIS =
					conversionHelper.convertValueToType(IBinaryInputStream.class, requestedValue);
			byte[] payload;
			int payloadSize = 0;
			try {
				int oneByte;
				while ((oneByte = payloadIS.readByte()) != -1) {
					payloadSize++;
					buffer[0] = (byte) oneByte;
					dos.write(buffer);
				}
				dos.finish();
				payload = bos.toByteArray();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			finally {
				try {
					payloadIS.close();
				}
				catch (IOException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
				bos.reset();
				deflater.reset();
			}
			chunkedResponses.add(new ChunkedResponse(chunkedRequest, payload, true, payloadSize));
		}

		return chunkedResponses;
	}
}
