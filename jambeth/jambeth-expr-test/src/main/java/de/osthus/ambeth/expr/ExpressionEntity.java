package de.osthus.ambeth.expr;

public interface ExpressionEntity
{
	int getId();

	int getVersion();

	long getMyProp1();

	void setMyProp1(long myProp1);

	@PropertyExpression("${MyProp1} * 2.3")
	double getValueForPredefinedExpression();

	String getMyExpressionProp();

	void setMyExpressionProp(String myExpressionProp);

	@PropertyExpression("${MyExpressionProp}")
	double getValueForDynamicExpression();
}
