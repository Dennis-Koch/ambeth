package de.osthus.ambeth.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponseWriter;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.ConfigurableGZIPOutputStream;

public class SilverlightWorkaroundGZipEncodingFilter implements ContainerRequestFilter, ContainerResponseFilter
{
	public static final String SILVERLIGHT_ACCEPT_ENCODING_HEADER = "Accept-Encoding-Workaround";

	@Override
	public ContainerRequest filter(ContainerRequest request)
	{
		if (request.getRequestHeaders().containsKey(HttpHeaders.CONTENT_ENCODING))
		{
			if (request.getRequestHeaders().getFirst(HttpHeaders.CONTENT_ENCODING).trim().equals("gzip"))
			{
				request.getRequestHeaders().remove(HttpHeaders.CONTENT_ENCODING);
				try
				{
					request.setEntityInputStream(new GZIPInputStream(request.getEntityInputStream()));
				}
				catch (Throwable e)
				{
					RuntimeExceptionUtil.mask(e);
				}
			}
		}
		return request;
	}

	private static final class Adapter implements ContainerResponseWriter
	{
		private final ContainerResponseWriter crw;

		private ConfigurableGZIPOutputStream gos;

		Adapter(ContainerResponseWriter crw)
		{
			this.crw = crw;
		}

		@Override
		public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException
		{
			gos = new ConfigurableGZIPOutputStream(crw.writeStatusAndHeaders(-1, response), Deflater.BEST_SPEED);
			return gos;
		}

		@Override
		public void finish() throws IOException
		{
			gos.finish();
			crw.finish();
		}
	}

	@Override
	public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
	{
		if (response.getEntity() != null && request.getRequestHeaders().containsKey(SILVERLIGHT_ACCEPT_ENCODING_HEADER)
				&& !response.getHttpHeaders().containsKey(HttpHeaders.CONTENT_ENCODING))
		{
			if (request.getRequestHeaders().getFirst(SILVERLIGHT_ACCEPT_ENCODING_HEADER).contains("gzip"))
			{
				response.getHttpHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
				response.setContainerResponseWriter(new Adapter(response.getContainerResponseWriter()));
			}
		}
		return response;
	}
}