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
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isNull(qb.property(nestFieldName));
		}
	},
	IS_NOT_NULL
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isNotNull(qb.property(nestFieldName));
		}
	},
	EQ
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isEqualTo(qb.property(nestFieldName), qb.valueName(nestFieldName));
		}
	},
	NOT_EQ
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isNotEqualTo(qb.property(nestFieldName), qb.valueName(nestFieldName));
		}
	},
	GT
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isGreaterThan(qb.property(nestFieldName), qb.valueName(nestFieldName));
		}
	},
	GE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isGreaterThanOrEqualTo(qb.property(nestFieldName), qb.valueName(nestFieldName));
		}
	},
	LT
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isLessThan(qb.property(nestFieldName), qb.valueName(nestFieldName));
		}
	},
	LE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isLessThanOrEqualTo(qb.property(nestFieldName), qb.valueName(nestFieldName));
		}
	},
	LIKE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.like(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	NOT_LIKE
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.notLike(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	START_WITH
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.startsWith(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	END_WITH
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.endsWith(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	CONTAINS
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.contains(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	NOT_CONTAINS
	{
		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.notContains(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	CONTAINED_IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isContainedIn(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}

	},
	NOT_CONTAINED_IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isNotContainedIn(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}

	},
	IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isIn(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	NOT_IN
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.isNotIn(qb.property(nestFieldName), qb.valueName(nestFieldName), false);
		}
	},
	REGEXP_LIKE
	{

		@Override
		public IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName)
		{
			return qb.regexpLike(qb.property(nestFieldName), qb.valueName(nestFieldName));
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

	public abstract IOperand createOperand(IQueryBuilder<?> qb, String nestFieldName);

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
}
