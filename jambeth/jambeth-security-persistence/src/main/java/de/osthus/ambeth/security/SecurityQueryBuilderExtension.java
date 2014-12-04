package de.osthus.ambeth.security;

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
		int size = joinClauses.size();
		IOperand userIdCriteriaOperand = queryBuilder.valueName(SqlPermissionOperand.USER_ID_CRITERIA_NAME);
		IOperand valueCriteriaOperand = queryBuilder.value(Boolean.TRUE);
		ArrayList<IOperand> readPermissionValueColumns = new ArrayList<IOperand>();
		ArrayList<IOperand> readPermissionUserIdColumns = new ArrayList<IOperand>();
		{
			ITable tableOfEntity = database.getTableByType(queryBuilder.getEntityType());
			IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(tableOfEntity.getName());
			String tableName = permissionGroup.getTable().getName();
			IOperand columnOperand = queryBuilder.column(permissionGroup.getPermissionGroupFieldOnTarget().getName());
			IOperand readPermissionIdColumn = queryBuilder.column(permissionGroup.getPermissionGroupField().getName());
			ISqlJoin join = queryBuilder.joinIntern(tableName, columnOperand, readPermissionIdColumn, JoinType.INNER, queryBeanContextFactory);
			joinClauses.add(join);
			readPermissionValueColumns.add(queryBuilder.column(permissionGroup.getReadPermissionField().getName(), join));
			readPermissionUserIdColumns.add(queryBuilder.column(permissionGroup.getUserField().getName(), join));
		}
		for (int i = 0; i < size; i++)
		{
			ISqlJoin entityJoin = joinClauses.get(i);
			String tableNameOfJoin = entityJoin.getTableName();

			// ITable tableOfJoin = database.getTableByName(tableNameOfJoin);
			// IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(entityJoin.getTableName());
			IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(tableNameOfJoin);
			if (permissionGroup == null)
			{
				if (database.getTableByName(tableNameOfJoin) == null)
				{
					// this join is a link-table join which has (currently) no permission group
					continue;
				}
				throw new IllegalStateException("No permission group mapped to table " + tableNameOfJoin);
			}
			String tableName = permissionGroup.getTable().getName();
			IOperand columnOperand = queryBuilder.column(permissionGroup.getPermissionGroupFieldOnTarget().getName(), entityJoin);
			IOperand readPermissionIdColumn = queryBuilder.column(permissionGroup.getPermissionGroupField().getName(), entityJoin);
			ISqlJoin permissionJoin = queryBuilder.joinIntern(tableName, columnOperand, readPermissionIdColumn, JoinType.INNER, queryBeanContextFactory);
			joinClauses.add(permissionJoin);
			readPermissionValueColumns.add(queryBuilder.column(permissionGroup.getReadPermissionField().getName(), permissionJoin));
			readPermissionUserIdColumns.add(queryBuilder.column(permissionGroup.getUserField().getName(), permissionJoin));
		}
		return queryBeanContextFactory.registerBean(SqlPermissionOperand.class)//
				.propertyValue("Operand", whereClause)//
				.propertyValue("UserIdCriteriaOperand", userIdCriteriaOperand)//
				.propertyValue("ValueCriteriaOperand", valueCriteriaOperand)//
				.propertyValue("ReadPermissionOperands", readPermissionValueColumns.toArray(IOperand.class))//
				.propertyValue("UserIdOperands", readPermissionUserIdColumns.toArray(IOperand.class));
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
