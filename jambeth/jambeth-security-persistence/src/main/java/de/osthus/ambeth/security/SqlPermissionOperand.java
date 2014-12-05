package de.osthus.ambeth.security;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.ISqlJoin;

public class SqlPermissionOperand implements IOperator
{
	public static final Object USER_ID_UNSPECIFIED = new Object();

	public static final String USER_ID_CRITERIA_NAME = "sec#userId";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IOperand operand;

	@Autowired
	protected IOperand userIdCriteriaOperand;

	@Autowired
	protected IOperand valueCriteriaOperand;

	@Autowired
	protected IOperand[] userIdOperands;

	@Autowired
	protected IOperand[] readPermissionOperands;

	@Autowired
	protected ISqlJoin[] permissionGroupJoins;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);

		if (nameToValueMap.get(USER_ID_CRITERIA_NAME) == USER_ID_UNSPECIFIED)
		{
			return;
		}
		AppendableStringBuilder joinSB = (AppendableStringBuilder) nameToValueMap.get("JoinSB");
		for (int i = 0; i < permissionGroupJoins.length; i++)
		{
			if (i > 0)
			{
				joinSB.append(' ');
			}
			permissionGroupJoins[i].expandQuery(joinSB, nameToValueMap, true, parameters);
		}

		IOperand firstUserIdOperand = userIdOperands[0];
		IOperand firstValueOperand = readPermissionOperands[0];

		querySB.append(" AND ");
		firstUserIdOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append('=');
		userIdCriteriaOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);

		querySB.append(" AND ");
		firstValueOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append('=');
		valueCriteriaOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);

		for (int a = 1, size = userIdOperands.length; a < size; a++)
		{
			querySB.append(" AND ");
			userIdOperands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			querySB.append('=');
			firstUserIdOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		for (int a = 1, size = readPermissionOperands.length; a < size; a++)
		{
			querySB.append(" AND ");
			readPermissionOperands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			querySB.append('=');
			firstValueOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
	}
}
