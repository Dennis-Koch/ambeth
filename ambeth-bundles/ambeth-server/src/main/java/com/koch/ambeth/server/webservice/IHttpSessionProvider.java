package com.koch.ambeth.server.webservice;

import javax.servlet.http.HttpSession;

public interface IHttpSessionProvider
{
	HttpSession getCurrentHttpSession();

	void setCurrentHttpSession(HttpSession httpSession);
}