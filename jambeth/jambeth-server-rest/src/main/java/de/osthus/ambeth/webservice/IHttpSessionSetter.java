package de.osthus.ambeth.webservice;

import javax.servlet.http.HttpSession;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IHttpSessionSetter
{

	void setCurrentHttpSession(HttpSession httpSession);

}