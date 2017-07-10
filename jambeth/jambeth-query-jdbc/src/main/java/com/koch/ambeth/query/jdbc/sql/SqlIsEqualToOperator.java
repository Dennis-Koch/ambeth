package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IMap;

public class SqlIsEqualToOperator extends CaseSensitiveTwoPlaceOperator {
	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean rightValueIsNull) {
		if (rightValueIsNull) {
			querySB.append(" IS ");
		}
		else {
			querySB.append("=");
		}
	}
}
