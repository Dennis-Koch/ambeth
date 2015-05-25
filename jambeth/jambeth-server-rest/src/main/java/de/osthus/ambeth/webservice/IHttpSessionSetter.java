package de.osthus.ambeth.webservice;

import javax.servlet.http.HttpSession;

public interface IHttpSessionSetter
{
	void setCurrentHttpSession(HttpSession httpSession);
}