package com.koch.ambeth.security.server.auth;

/*-
 * #%L
 * jambeth-security-server
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

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.security.AuthenticationException;
import com.koch.ambeth.security.AuthenticationResult;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class LDAPAuthenticationManager extends AbstractAuthenticationManager {
	public static final String USER_NAME_VARIABLE = "userName";

	public static final String USER_NAME_VARIABLE_DEF = "${" + USER_NAME_VARIABLE + "}";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuthenticationResultCache authenticationResultCache;

	@Property(name = SecurityServerConfigurationConstants.LdapDomain)
	protected String domain;

	@Property(name = SecurityServerConfigurationConstants.LdapHost)
	protected String ldapHost;

	@Property(name = SecurityServerConfigurationConstants.LdapBase, defaultValue = "")
	protected String searchBase;

	@Property(name = SecurityServerConfigurationConstants.LdapUserAttribute, defaultValue = "uid")
	protected String userPrincipalName;

	@Property(name = SecurityServerConfigurationConstants.LdapCtxFactory,
			defaultValue = "com.sun.jndi.ldap.LdapCtxFactory")
	protected String ldapContextFactory;

	@Property(name = SecurityServerConfigurationConstants.LdapFilter,
			defaultValue = "(&(objectClass=user)(sAMAccountName=" + USER_NAME_VARIABLE_DEF + "))")
	protected String searchFilter;

	protected LdapContext createContext(String userName, String password) {
		try {
			Hashtable<String, String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContextFactory);
			env.put(Context.PROVIDER_URL, ldapHost);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");

			String[] userNameAndDomain = getUserNameAndDomain(userName);
			env.put(Context.SECURITY_PRINCIPAL, userNameAndDomain[0] + "@" + userNameAndDomain[1]);

			env.put(Context.SECURITY_CREDENTIALS, new String(password));

			return new InitialLdapContext(env, null);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected NamingEnumeration<SearchResult> query(String userName, LdapContext ctxGC) {
		try {
			String[] returnedAtts = {userPrincipalName};

			// Create the search controls
			SearchControls searchCtls = new SearchControls();
			searchCtls.setReturningAttributes(returnedAtts);

			// Specify the search scope
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			Properties props = new Properties();
			String[] userNameAndDomain = getUserNameAndDomain(userName);
			props.putString(USER_NAME_VARIABLE, userNameAndDomain[0]);
			String searchFilter = props.resolvePropertyParts(this.searchFilter);

			return ctxGC.search(searchBase, searchFilter, searchCtls);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IAuthenticationResult buildAuthenticationResult(IList<Map<String, Object>> result) {
		Map<String, Object> map = result.get(0);
		Object nameValue = map.get(userPrincipalName);
		final String sid = (String) nameValue;
		return new AuthenticationResult(sid, false, false);
	}

	@Override
	public IAuthenticationResult authenticate(IAuthentication authentication)
			throws AuthenticationException {
		IAuthenticationResult authenticationResult =
				authenticationResultCache.resolveAuthenticationResult(authentication);
		if (authenticationResult != null) {
			return authenticationResult;
		}
		authenticationResult = doLDAPAuthentication(authentication);
		authenticationResultCache.cacheAuthenticationResult(authentication, authenticationResult);
		return authenticationResult;
	}

	protected IAuthenticationResult doLDAPAuthentication(final IAuthentication authentication)
			throws AuthenticationException {
		try {
			LdapContext ctxGC =
					createContext(authentication.getUserName(), new String(authentication.getPassword()));
			try {
				ArrayList<Map<String, Object>> result = new ArrayList<>();
				// Now try a simple search and get some attributes as defined in returnedAtts
				NamingEnumeration<SearchResult> answer = query(authentication.getUserName(), ctxGC);
				try {
					while (answer.hasMoreElements()) {
						SearchResult sr = answer.next();
						Attributes attrs = sr.getAttributes();
						if (attrs == null) {
							continue;
						}
						NamingEnumeration<? extends Attribute> ne = attrs.getAll();
						try {
							Map<String, Object> entryMap = new HashMap<>();
							while (ne.hasMore()) {
								Attribute attr = ne.next();
								if (attr.size() == 1) {
									entryMap.put(attr.getID(), attr.get());
								}
								else {
									HashSet<String> s = new HashSet<>();
									NamingEnumeration<?> n = attr.getAll();
									while (n.hasMoreElements()) {
										s.add((String) n.nextElement());
									}
									entryMap.put(attr.getID(), s);
								}
							}
							result.add(entryMap);
						}
						finally {
							ne.close();
						}
					}
				}
				finally {
					answer.close();
				}
				if (result.size() == 0) {
					throw createAuthenticationException(authentication);
				}
				return buildAuthenticationResult(result);
			}
			finally {
				ctxGC.close();
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * Get the username and domain (the domain is taken from username if specified or default domain)
	 *
	 * @param userName The username (with or without domain (e.g. "admin@example.com"))
	 * @return String array with username and domain
	 */
	private String[] getUserNameAndDomain(String userName) {
		int index = userName.indexOf("@");
		if (index > -1) {
			if (index == (userName.length() - 1)) {
				throw new IllegalArgumentException("No domain specified in user name: " + userName);
			}
			String userNameWithoutDomain = userName.substring(0, index);
			String domain = userName.substring(index + 1);
			return new String[] {userNameWithoutDomain, domain};
		}
		else {
			// no domain specified in userName so use default domain
			return new String[] {userName, domain};
		}
	}
}
