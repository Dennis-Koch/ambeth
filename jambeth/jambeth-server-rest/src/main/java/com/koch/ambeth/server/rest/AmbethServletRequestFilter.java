package com.koch.ambeth.server.rest;

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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.ws.rs.ext.Provider;

import com.koch.ambeth.util.state.IStateRollback;

@WebFilter
@Provider
public class AmbethServletRequestFilter implements Filter {

	AmbethServletAspect aspect = new AmbethServletAspect();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// intended blank
	}

	@Override
	public void destroy() {
		// intended blank
	}

	/**
	 * This method always clear thread local variables after a http request was handled. Override
	 * {@link #doFilterIntern(ServletRequest, ServletResponse, FilterChain)} when deriving this class.
	 *
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		IStateRollback rollback = aspect.pushServletAspectWithThreadLocals(request, response);
		try {
			chain.doFilter(request, response);
		}
		finally {
			rollback.rollback();
		}
	}
}
