package de.osthus.ambeth.query.shuang;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilder;

public enum Condition
{
	IS_NULL
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return qb.isNull(qb.property(nestFieldName));
		}
	},
	IS_NOT_NULL
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return qb.isNotNull(qb.property(nestFieldName));
		}
	},
	EQ
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isEqualTo(qb.property(nestFieldName), qb.value(value));
		}
	},
	NOT_EQ
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isNotEqualTo(qb.property(nestFieldName), qb.value(value));
		}
	},
	GT
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isGreaterThan(qb.property(nestFieldName), qb.value(value));
		}
	},
	GE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isGreaterThanOrEqualTo(qb.property(nestFieldName), qb.value(value));
		}
	},
	LT
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isLessThan(qb.property(nestFieldName), qb.value(value));
		}
	},
	LE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isLessThanOrEqualTo(qb.property(nestFieldName), qb.value(value));
		}
	},
	LIKE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.like(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	NOT_LIKE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.notLike(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	START_WITH
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.startsWith(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	END_WITH
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.endsWith(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	CONTAINS
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.contains(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	NOT_CONTAINS
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.notContains(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	CONTAINED_IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.isContainedIn(qb.property(nestFieldName), qb.value(value), false);
		}

	},
	NOT_CONTAINED_IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.isNotContainedIn(qb.property(nestFieldName), qb.value(value), false);
		}

	},
	IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isIn(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	NOT_IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return value == null ? null : qb.isNotIn(qb.property(nestFieldName), qb.value(value), false);
		}
	},
	REGEXP_LIKE
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value)
		{
			return isBlankString(value) ? null : qb.regexpLike(qb.property(nestFieldName), qb.value(value));
		}
	};

	private static final Pattern PATTERN_FIRST_CHAR = Pattern.compile("(?<=[a-z])(?=[A-Z])");
	private static final Pattern PATTERN_NOT_FIRST_CHAR = Pattern.compile("(?<=(^|_)[A-Z])[^_]+");

	public static Condition build(String condition)
	{
		if (condition == null)
		{
			return EQ;
		}
		return Condition.valueOf(toUncapitalize(condition));
	}

	public abstract IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName, Object value);

	/**
	 * @return eg: START_WITH -> StartWith
	 */
	public String toCapitalize()
	{
		StringBuilder result = new StringBuilder(this.name());
		Matcher matcher = PATTERN_NOT_FIRST_CHAR.matcher(result);
		while (matcher.find())
		{
			String needReplace = matcher.group(0);
			int start = matcher.start();
			int end = matcher.end();
			// if just use String#replace(), it will repalce all, include the
			// wrong position
			result.replace(start, end, needReplace.toLowerCase());
		}
		return result.toString().replace("_", "");
	}

	/**
	 * @param condition
	 * @return e.g: StartWith -> START_WITH
	 */
	private static String toUncapitalize(String condition)
	{
		Objects.requireNonNull(condition, "operator can't be null");
		return PATTERN_FIRST_CHAR.matcher(condition).replaceAll("_").toUpperCase();
	}

	private static boolean isBlankString(Object obj)
	{
		if (obj == null)
		{
			return true;
		}
		else if (obj instanceof String)
		{
			return obj.toString().trim().isEmpty();
		}
		else
		{
			return false;
		}
	}
}
