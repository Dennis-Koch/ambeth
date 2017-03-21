package com.koch.ambeth.merge.orm;

/*-
 * #%L
 * jambeth-merge
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
