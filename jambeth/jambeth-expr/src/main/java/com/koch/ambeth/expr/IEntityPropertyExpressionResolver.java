package com.koch.ambeth.expr;

public interface IEntityPropertyExpressionResolver
{
	Object resolveExpressionOnEntity(Object entity, String expression);
}
