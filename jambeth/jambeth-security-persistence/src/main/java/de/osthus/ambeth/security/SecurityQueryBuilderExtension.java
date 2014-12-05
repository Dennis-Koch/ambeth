package de.osthus.ambeth.security;

import java.util.List;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IPermissionGroup;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilderExtension;
import de.osthus.ambeth.query.IQueryBuilderIntern;
import de.osthus.ambeth.query.ISqlJoin;
import de.osthus.ambeth.query.QueryType;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;

public class SecurityQueryBuilderExtension implements IQueryBuilderExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public IBeanConfiguration applyOnWhereClause(IBeanContextFactory queryBeanContextFactory, IQueryBuilderIntern<?> queryBuilder, IOperand whereClause,
			IList<ISqlJoin> joinClauses, QueryType queryType)
	{
		if (!securityActive || (whereClause instanceof SqlPermissionOperand))
		{
			return null;
		}
		IDatabase database = this.database.getCurrent();
		IOperand userIdCriteriaOperand = queryBuilder.valueName(SqlPermissionOperand.USER_ID_CRITERIA_NAME);
		IOperand valueCriteriaOperand = queryBuilder.value(Boolean.TRUE);
		ArrayList<ISqlJoin> permissionGroupJoins = new ArrayList<ISqlJoin>();
		ArrayList<IOperand> readPermissionValueColumns = new ArrayList<IOperand>();
		ArrayList<IOperand> readPermissionUserIdColumns = new ArrayList<IOperand>();
		{
			ITable tableOfEntity = database.getTableByType(queryBuilder.getEntityType());
			ISqlJoin join = createJoin(tableOfEntity.getName(), queryBuilder, readPermissionValueColumns, readPermissionUserIdColumns, queryBeanContextFactory,
					null);
			if (join != null)
			{
				permissionGroupJoins.add(join);
				joinClauses.add(join);
			}
		}
		for (int i = 0, size = joinClauses.size(); i < size; i++)
		{
			ISqlJoin entityJoin = joinClauses.get(i);
			String tableNameOfJoin = entityJoin.getTableName();

			ISqlJoin join = createJoin(tableNameOfJoin, queryBuilder, readPermissionValueColumns, readPermissionUserIdColumns, queryBeanContextFactory,
					entityJoin);
			if (join != null)
			{
				permissionGroupJoins.add(join);
				joinClauses.add(join);
			}
		}
		if (permissionGroupJoins.size() == 0)
		{
			return null;
		}
		return queryBeanContextFactory.registerBean(SqlPermissionOperand.class)//
				.propertyValue("Operand", whereClause)//
				.propertyValue("UserIdCriteriaOperand", userIdCriteriaOperand)//
				.propertyValue("ValueCriteriaOperand", valueCriteriaOperand)//
				.propertyValue("PermissionGroupJoins", permissionGroupJoins.toArray(ISqlJoin.class))//
				.propertyValue("ReadPermissionOperands", readPermissionValueColumns.toArray(IOperand.class))//
				.propertyValue("UserIdOperands", readPermissionUserIdColumns.toArray(IOperand.class));
	}

	protected ISqlJoin createJoin(String tableNameOfJoin, IQueryBuilderIntern<?> queryBuilder, List<IOperand> readPermissionValueColumns,
			List<IOperand> readPermissionUserIdColumns, IBeanContextFactory queryBeanContextFactory, ISqlJoin baseJoin)
	{
		IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(tableNameOfJoin);
		if (permissionGroup == null)
		{
			// this join is a either link-table join which has (currently) no permission group
			// or it is an entity table but we have already logged a warning in the connection startup for this
			return null;
		}
		String tableName = permissionGroup.getTable().getName();
		IOperand columnOperand = queryBuilder.column(permissionGroup.getPermissionGroupFieldOnTarget().getName(), baseJoin);
		IOperand readPermissionIdColumn = queryBuilder.column(permissionGroup.getPermissionGroupField().getName(), baseJoin);
		ISqlJoin join = queryBuilder.joinIntern(tableName, columnOperand, readPermissionIdColumn, JoinType.INNER, queryBeanContextFactory);
		readPermissionValueColumns.add(queryBuilder.column(permissionGroup.getReadPermissionField().getName(), join));
		readPermissionUserIdColumns.add(queryBuilder.column(permissionGroup.getUserField().getName(), join));
		return join;
	}

	@Override
	public void applyOnQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList)
	{
		if (!securityActive || nameToValueMap.containsKey(SqlPermissionOperand.USER_ID_CRITERIA_NAME))
		{
			// id is explicitly specified. nothing to do here
			return;
		}
		if (!securityActivation.isFilterActivated())
		{
			// we do not filter - however we need to pass this information to the query building mechanism
			nameToValueMap.put(SqlPermissionOperand.USER_ID_CRITERIA_NAME, SqlPermissionOperand.USER_ID_UNSPECIFIED);
			return;
		}
		ISecurityContext context = securityContextHolder.getContext();
		if (context == null)
		{
			throw new IllegalStateException("Should never happen. Security activated but no authentication active");
		}
		IAuthorization authorization = context.getAuthorization();
		if (authorization == null)
		{
			throw new IllegalStateException("Should never happen. Security activated but no authentication active");
		}
		nameToValueMap.put(SqlPermissionOperand.USER_ID_CRITERIA_NAME, authorization.getSID());
	}
}
