package com.koch.ambeth.service;

import javax.net.ssl.SSLContext;

public interface ISSLContextFactory {
	SSLContext createSSLContext();
}
