package de.osthus.ambeth.orm;

public class CompositeMemberConfig extends AbstractMemberConfig
{
	private static String constructName(MemberConfig[] members)
	{
		StringBuilder sb = new StringBuilder(members[0].getName());
		for (int i = 1; i < members.length; i++)
		{
			MemberConfig member = members[i];
			sb.append('-').append(member.getName());
		}
		return sb.toString();
	}

	private final MemberConfig[] members;

	public CompositeMemberConfig(MemberConfig[] members)
	{
		super(constructName(members));
		this.members = members;
	}

	public MemberConfig[] getMembers()
	{
		return members;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof CompositeMemberConfig)
		{
			return equals((AbstractMemberConfig) obj);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getName().hashCode();
	}
}
