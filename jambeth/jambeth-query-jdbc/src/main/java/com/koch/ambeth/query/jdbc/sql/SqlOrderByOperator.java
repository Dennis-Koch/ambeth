package com.koch.ambeth.query.jdbc.sql;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class SqlOrderByOperator implements IOperator, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected static final Pattern ignoredColumnNamesPattern =
			Pattern.compile("([A-Z_]+\\.)?\"?(ID|VERSION)\"?");

	protected IOperand column;

	protected IThreadLocalObjectCollector objectCollector;

	protected OrderByType orderByType;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(column, "column");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(orderByType, "orderByType");
	}

	public void setColumn(IOperand column) {
		this.column = column;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
		this.objectCollector = objectCollector;
	}

	public void setOrderByType(OrderByType orderByType) {
		this.orderByType = orderByType;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters) {
		Boolean firstOrderByState = (Boolean) nameToValueMap.get(QueryConstants.FIRST_ORDER_BY_STATE);
		List<String> additionalSelectColumnList =
				(List<String>) nameToValueMap.get(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		if (firstOrderByState == null || Boolean.TRUE.equals(firstOrderByState)) {
			nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.FALSE);
			querySB.append(" ORDER BY ");
		}
		else {
			querySB.append(',');
		}
		if (additionalSelectColumnList != null) {
			IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
			AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
			try {
				column.expandQuery(sb, nameToValueMap, joinQuery, parameters);
				Matcher matcher = ignoredColumnNamesPattern.matcher(sb);
				boolean matches = matcher.matches();
				String tableAlias = matches ? matcher.group(1) : null;
				if (!matches || (tableAlias != null && !tableAlias.startsWith("S_"))) {
					additionalSelectColumnList.add(sb.toString());
				}
				column.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
			finally {
				tlObjectCollector.dispose(sb);
			}
		}
		else {
			column.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		switch (orderByType) {
			case ASC:
				querySB.append(" ASC");
				break;
			case DESC:
				querySB.append(" DESC");
				break;
			default:
				throw new IllegalStateException("Type " + orderByType + " not supported");
		}
	}
}
