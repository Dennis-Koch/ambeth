package com.koch.ambeth.server.rest.filter;

/*-
 * #%L
 * jambeth-server-rest
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

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.ConfigurableGZIPOutputStream;

public class SilverlightWorkaroundGZipEncodingFilter
		implements ContainerRequestFilter, ContainerResponseFilter {
	public static final String SILVERLIGHT_ACCEPT_ENCODING_HEADER = "Accept-Encoding-Workaround";

	@Override
	public void filter(ContainerRequestContext request) {
		if (request.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING)) {
			if ("gzip".equals(request.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING).trim())) {
				request.getHeaders().remove(HttpHeaders.CONTENT_ENCODING);
				try {
					request.setEntityStream(new GZIPInputStream(request.getEntityStream()));
				}
				catch (Throwable e) {
					RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response)
			throws IOException {
		if (response.getEntity() != null
				&& request.getHeaders().containsKey(SILVERLIGHT_ACCEPT_ENCODING_HEADER)
				&& !response.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING)) {
			if (request.getHeaders().getFirst(SILVERLIGHT_ACCEPT_ENCODING_HEADER).contains("gzip")) {
				response.getStringHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");

				response.setEntityStream(
						new ConfigurableGZIPOutputStream(response.getEntityStream(), Deflater.BEST_SPEED));
			}
		}
	}
}
