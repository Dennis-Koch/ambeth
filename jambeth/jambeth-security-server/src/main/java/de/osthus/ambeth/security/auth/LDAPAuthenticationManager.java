package de.osthus.ambeth.security.auth;

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

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.AuthenticationException;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthenticationResult;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;

public class LDAPAuthenticationManager extends AbstractAuthenticationManager
{
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

	@Property(name = SecurityServerConfigurationConstants.LdapBase)
	protected String searchBase;

	@Property(name = SecurityServerConfigurationConstants.LdapUserAttribute, defaultValue = "uid")
	protected String userPrincipalName;

	@Property(name = SecurityServerConfigurationConstants.LdapCtxFactory, defaultValue = "com.sun.jndi.ldap.LdapCtxFactory")
	protected String ldapContextFactory;

	@Property(name = SecurityServerConfigurationConstants.LdapFilter, defaultValue = "(&(objectClass=user)(sAMAccountName=" + USER_NAME_VARIABLE_DEF + "))")
	protected String searchFilter;

	protected LdapContext createContext(String userName, String password)
	{
		try
		{
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContextFactory);
			env.put(Context.PROVIDER_URL, ldapHost);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, userName + "@" + domain);
			env.put(Context.SECURITY_CREDENTIALS, new String(password));

			return new InitialLdapContext(env, null);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected NamingEnumeration<SearchResult> query(String userName, LdapContext ctxGC)
	{
		try
		{
			String[] returnedAtts = { userPrincipalName };

			// Create the search controls
			SearchControls searchCtls = new SearchControls();
			searchCtls.setReturningAttributes(returnedAtts);

			// Specify the search scope
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			Properties props = new Properties();
			props.putString(USER_NAME_VARIABLE, userName);
			String searchFilter = props.resolvePropertyParts(this.searchFilter);

			return ctxGC.search(searchBase, searchFilter, searchCtls);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IAuthenticationResult buildAuthenticationResult(IList<Map<String, Object>> result)
	{
		Map<String, Object> map = result.get(0);
		Object nameValue = map.get(userPrincipalName);
		final String userName = (String) nameValue;
		return new IAuthenticationResult()
		{

			@Override
			public boolean isRehashPasswordRecommended()
			{
				return false;
			}

			@Override
			public boolean isChangePasswordRecommended()
			{
				return false;
			}

			@Override
			public String getUserName()
			{
				return userName;
			}
		};
	}

	@Override
	public IAuthenticationResult authenticate(IAuthentication authentication) throws AuthenticationException
	{
		IAuthenticationResult authenticationResult = authenticationResultCache.resolveAuthenticationResult(authentication);
		if (authenticationResult != null)
		{
			return authenticationResult;
		}
		authenticationResult = doLDAPAuthentication(authentication);
		authenticationResultCache.cacheAuthenticationResult(authentication, authenticationResult);
		return authenticationResult;
	}

	protected IAuthenticationResult doLDAPAuthentication(final IAuthentication authentication) throws AuthenticationException
	{

		try
		{
			LdapContext ctxGC = createContext(authentication.getUserName(), new String(authentication.getPassword()));
			try
			{
				ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
				// Now try a simple search and get some attributes as defined in returnedAtts
				NamingEnumeration<SearchResult> answer = query(authentication.getUserName(), ctxGC);
				try
				{
					while (answer.hasMoreElements())
					{
						SearchResult sr = answer.next();
						Attributes attrs = sr.getAttributes();
						if (attrs == null)
						{
							continue;
						}
						NamingEnumeration<? extends Attribute> ne = attrs.getAll();
						try
						{
							Map<String, Object> entryMap = new HashMap<String, Object>();
							while (ne.hasMore())
							{
								Attribute attr = ne.next();
								if (attr.size() == 1)
								{
									entryMap.put(attr.getID(), attr.get());
								}
								else
								{
									HashSet<String> s = new HashSet<String>();
									NamingEnumeration<?> n = attr.getAll();
									while (n.hasMoreElements())
									{
										s.add((String) n.nextElement());
									}
									entryMap.put(attr.getID(), s);
								}
							}
							result.add(entryMap);
						}
						finally
						{
							ne.close();
						}
					}
				}
				finally
				{
					answer.close();
				}
				if (result.size() == 0)
				{
					throw createAuthenticationException(authentication);
				}
				return buildAuthenticationResult(result);
			}
			finally
			{
				ctxGC.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
