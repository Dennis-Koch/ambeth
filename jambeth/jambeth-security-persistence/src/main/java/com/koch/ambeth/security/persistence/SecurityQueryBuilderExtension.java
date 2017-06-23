package com.koch.ambeth.security.persistence;

/*-
 * #%L
 * jambeth-security-persistence
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

import java.util.List;

import javax.persistence.criteria.JoinType;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IPermissionGroup;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQueryBuilderExtension;
import com.koch.ambeth.query.IQueryBuilderIntern;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.query.QueryType;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SecurityQueryBuilderExtension implements IQueryBuilderExtension {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public IBeanConfiguration applyOnWhereClause(IBeanContextFactory queryBeanContextFactory,
			IQueryBuilderIntern<?> queryBuilder, IOperand whereClause, IList<ISqlJoin> joinClauses,
			QueryType queryType) {
		if (!securityActive || (whereClause instanceof SqlPermissionOperand)) {
			return null;
		}
		IDatabaseMetaData databaseMetaData = this.databaseMetaData;
		IOperand userIdCriteriaOperand =
				queryBuilder.valueName(SqlPermissionOperand.USER_ID_CRITERIA_NAME);
		IOperand valueCriteriaOperand = queryBuilder.value(Boolean.TRUE);
		ArrayList<ISqlJoin> permissionGroupJoins = new ArrayList<>();
		ArrayList<IOperand> readPermissionValueColumns = new ArrayList<>();
		ArrayList<IOperand> readPermissionUserIdColumns = new ArrayList<>();
		{
			ITableMetaData tableOfEntity = databaseMetaData.getTableByType(queryBuilder.getEntityType());
			ISqlJoin join = createJoin(tableOfEntity.getName(), queryBuilder, readPermissionValueColumns,
					readPermissionUserIdColumns, queryBeanContextFactory, null);
			if (join != null) {
				permissionGroupJoins.add(join);
				joinClauses.add(join);
			}
		}
		for (int i = 0, size = joinClauses.size(); i < size; i++) {
			ISqlJoin entityJoin = joinClauses.get(i);
			String tableNameOfJoin = entityJoin.getTableName();

			ISqlJoin join = createJoin(tableNameOfJoin, queryBuilder, readPermissionValueColumns,
					readPermissionUserIdColumns, queryBeanContextFactory, entityJoin);
			if (join != null) {
				permissionGroupJoins.add(join);
				joinClauses.add(join);
			}
		}
		if (permissionGroupJoins.isEmpty()) {
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

	protected ISqlJoin createJoin(String tableNameOfJoin, IQueryBuilderIntern<?> queryBuilder,
			List<IOperand> readPermissionValueColumns, List<IOperand> readPermissionUserIdColumns,
			IBeanContextFactory queryBeanContextFactory, ISqlJoin baseJoin) {
		IPermissionGroup permissionGroup = databaseMetaData.getPermissionGroupOfTable(tableNameOfJoin);
		if (permissionGroup == null) {
			// this join is a either link-table join which has (currently) no permission group
			// or it is an entity table but we have already logged a warning in the connection startup for
			// this
			return null;
		}
		String tableName = permissionGroup.getTable().getName();
		IOperand columnOperand = queryBuilder
				.column(permissionGroup.getPermissionGroupFieldOnTarget().getName(), baseJoin, false);
		IOperand readPermissionIdColumn =
				queryBuilder.column(permissionGroup.getPermissionGroupField().getName(), baseJoin, false);
		ISqlJoin join = queryBuilder.joinIntern(tableName, columnOperand, readPermissionIdColumn,
				JoinType.LEFT, queryBeanContextFactory);
		readPermissionValueColumns
				.add(queryBuilder.column(permissionGroup.getReadPermissionField().getName(), join, false));
		readPermissionUserIdColumns
				.add(queryBuilder.column(permissionGroup.getUserField().getName(), join, false));
		return join;
	}

	@Override
	public void applyOnQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<String> additionalSelectColumnList) {
		if (!securityActive || nameToValueMap.containsKey(SqlPermissionOperand.USER_ID_CRITERIA_NAME)) {
			// id is explicitly specified. nothing to do here
			return;
		}
		if (!securityActivation.isFilterActivated()) {
			// we do not filter - however we need to pass this information to the query building mechanism
			nameToValueMap.put(SqlPermissionOperand.USER_ID_CRITERIA_NAME,
					SqlPermissionOperand.USER_ID_UNSPECIFIED);
			return;
		}
		ISecurityContext context = securityContextHolder.getContext();
		if (context == null) {
			throw new IllegalStateException(
					"Should never happen. Security activated but no authentication active");
		}
		IAuthorization authorization = context.getAuthorization();
		if (authorization == null) {
			throw new IllegalStateException(
					"Should never happen. Security activated but no authentication active");
		}
		nameToValueMap.put(SqlPermissionOperand.USER_ID_CRITERIA_NAME, authorization.getSID());
	}
}
