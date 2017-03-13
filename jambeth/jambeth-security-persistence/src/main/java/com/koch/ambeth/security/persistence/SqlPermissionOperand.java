package com.koch.ambeth.security.persistence;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlPermissionOperand implements IOperator
{
	public static final Object USER_ID_UNSPECIFIED = new Object();

	public static final String USER_ID_CRITERIA_NAME = "sec#userId";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Property
	protected IOperand operand;

	@Property
	protected IOperand userIdCriteriaOperand;

	@Property
	protected IOperand valueCriteriaOperand;

	@Property
	protected IOperand[] userIdOperands;

	@Property
	protected IOperand[] readPermissionOperands;

	@Property
	protected ISqlJoin[] permissionGroupJoins;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		if (operand != null)
		{
			operand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		if (nameToValueMap.get(USER_ID_CRITERIA_NAME) == USER_ID_UNSPECIFIED)
		{
			return;
		}
		AppendableStringBuilder joinSB = (AppendableStringBuilder) nameToValueMap.get("#JoinSB");
		for (int i = 0; i < permissionGroupJoins.length; i++)
		{
			if (joinSB.length() > 0)
			{
				joinSB.append(' ');
			}
			permissionGroupJoins[i].expandQuery(joinSB, nameToValueMap, true, parameters);
		}
		IOperand firstUserIdOperand = userIdOperands[0];
		IOperand firstValueOperand = readPermissionOperands[0];

		if (operand != null)
		{
			querySB.append(" AND ");
		}
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
