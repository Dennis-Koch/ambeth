package com.koch.ambeth.shell.core;

import java.util.ArrayList;
import java.util.List;

public interface CommandExtension
{
	Usage getUsage();

	public static class Usage
	{
		private String name;
		private String description;
		private List<Parameter> parameters;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public List<Parameter> getParameters()
		{
			return parameters;
		}

		public void setParameters(List<Parameter> parameters)
		{
			this.parameters = parameters;
		}

		public void addParameter(String name, String description, String defaultValue, boolean mandatory)
		{
			if (parameters == null)
			{
				parameters = new ArrayList<CommandExtension.Parameter>();
			}
			Parameter p = new Parameter();
			p.setName(name);
			p.setDescription(description);
			p.setDefaultValue(defaultValue);
			p.setMandatory(mandatory);
			parameters.add(p);
		}
	}

	public static class Parameter
	{
		private String name;
		private String description;
		private String defaultValue;
		private boolean mandatory;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public String getDefaultValue()
		{
			return defaultValue;
		}

		public void setDefaultValue(String defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		public boolean isMandatory()
		{
			return mandatory;
		}

		public void setMandatory(boolean mandatory)
		{
			this.mandatory = mandatory;
		}
	}
}
