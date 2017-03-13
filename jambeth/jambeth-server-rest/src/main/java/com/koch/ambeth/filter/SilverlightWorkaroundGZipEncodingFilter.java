package com.koch.ambeth.filter;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.ConfigurableGZIPOutputStream;

public class SilverlightWorkaroundGZipEncodingFilter implements ContainerRequestFilter, ContainerResponseFilter
{
	public static final String SILVERLIGHT_ACCEPT_ENCODING_HEADER = "Accept-Encoding-Workaround";

	@Override
	public void filter(ContainerRequestContext request)
	{
		if (request.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING))
		{
			if (request.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING).trim().equals("gzip"))
			{
				request.getHeaders().remove(HttpHeaders.CONTENT_ENCODING);
				try
				{
					request.setEntityStream(new GZIPInputStream(request.getEntityStream()));
				}
				catch (Throwable e)
				{
					RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException
	{
		if (response.getEntity() != null && request.getHeaders().containsKey(SILVERLIGHT_ACCEPT_ENCODING_HEADER)
				&& !response.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING))
		{
			if (request.getHeaders().getFirst(SILVERLIGHT_ACCEPT_ENCODING_HEADER).contains("gzip"))
			{
				response.getStringHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
				
				response.setEntityStream(new ConfigurableGZIPOutputStream(response.getEntityStream(), Deflater.BEST_SPEED));
			}
		}
	}
}