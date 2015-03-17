package de.osthus.ambeth.metadata;

import java.util.regex.Pattern;

public final class EmbeddedMember
{
	private static final Pattern pattern = Pattern.compile(Pattern.quote("."));

	public static String[] split(String memberName)
	{
		return pattern.split(memberName);
	}

	public static String buildMemberPathString(Member[] memberPath)
	{
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			Member member = memberPath[a];
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(member.getName());
		}
		return sb.toString();
	}

	public static String[] buildMemberPathToken(Member[] memberPath)
	{
		String[] token = new String[memberPath.length];
		for (int a = memberPath.length; a-- > 0;)
		{
			Member member = memberPath[a];
			token[a] = member.getName();
		}
		return token;
	}

	private EmbeddedMember()
	{
		// intended blank
	}
}
