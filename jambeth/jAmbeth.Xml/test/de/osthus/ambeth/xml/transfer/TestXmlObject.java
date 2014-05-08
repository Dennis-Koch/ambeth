package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlTransient;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public class TestXmlObject
{
	protected String valueString;

	protected long valueLong;

	protected int valueInteger;

	protected double valueDouble;

	protected float valueFloat;

	protected byte valueByte;

	protected char valueCharacter;

	protected boolean valueBoolean;

	protected Long valueLongN;

	protected Integer valueIntegerN;

	protected Double valueDoubleN;

	protected Float valueFloatN;

	protected Byte valueByteN;

	protected Character valueCharacterN;

	protected Boolean valueBooleanN;

	protected transient Object transientField1;

	@XmlTransient
	protected Object transientField2;

	public String getValueString()
	{
		return valueString;
	}

	public void setValueString(String valueString)
	{
		this.valueString = valueString;
	}

	public long getValueLong()
	{
		return valueLong;
	}

	public void setValueLong(long valueLong)
	{
		this.valueLong = valueLong;
	}

	public int getValueInteger()
	{
		return valueInteger;
	}

	public void setValueInteger(int valueInteger)
	{
		this.valueInteger = valueInteger;
	}

	public double getValueDouble()
	{
		return valueDouble;
	}

	public void setValueDouble(double valueDouble)
	{
		this.valueDouble = valueDouble;
	}

	public float getValueFloat()
	{
		return valueFloat;
	}

	public void setValueFloat(float valueFloat)
	{
		this.valueFloat = valueFloat;
	}

	public byte getValueByte()
	{
		return valueByte;
	}

	public void setValueByte(byte valueByte)
	{
		this.valueByte = valueByte;
	}

	public char getValueCharacter()
	{
		return valueCharacter;
	}

	public void setValueCharacter(char valueCharacter)
	{
		this.valueCharacter = valueCharacter;
	}

	public boolean getValueBoolean()
	{
		return valueBoolean;
	}

	public void setValueBoolean(boolean valueBoolean)
	{
		this.valueBoolean = valueBoolean;
	}

	public Long getValueLongN()
	{
		return valueLongN;
	}

	public void setValueLongN(Long valueLongN)
	{
		this.valueLongN = valueLongN;
	}

	public Integer getValueIntegerN()
	{
		return valueIntegerN;
	}

	public void setValueIntegerN(Integer valueIntegerN)
	{
		this.valueIntegerN = valueIntegerN;
	}

	public Double getValueDoubleN()
	{
		return valueDoubleN;
	}

	public void setValueDoubleN(Double valueDoubleN)
	{
		this.valueDoubleN = valueDoubleN;
	}

	public Float getValueFloatN()
	{
		return valueFloatN;
	}

	public void setValueFloatN(Float valueFloatN)
	{
		this.valueFloatN = valueFloatN;
	}

	public Byte getValueByteN()
	{
		return valueByteN;
	}

	public void setValueByteN(Byte valueByteN)
	{
		this.valueByteN = valueByteN;
	}

	public Character getValueCharacterN()
	{
		return valueCharacterN;
	}

	public void setValueCharacterN(Character valueCharacterN)
	{
		this.valueCharacterN = valueCharacterN;
	}

	public Boolean getValueBooleanN()
	{
		return valueBooleanN;
	}

	public void setValueBooleanN(Boolean valueBooleanN)
	{
		this.valueBooleanN = valueBooleanN;
	}

	public Object getTransientField1()
	{
		return transientField1;
	}

	public void setTransientField1(Object transientField1)
	{
		this.transientField1 = transientField1;
	}

	public Object getTransientField2()
	{
		return transientField2;
	}

	public void setTransientField2(Object transientField2)
	{
		this.transientField2 = transientField2;
	}

	@Override
	public int hashCode()
	{
		return 42; // The answer to the sense of life, the universe and everything else
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		TestXmlObject other = (TestXmlObject) obj;
		if (valueBoolean != other.valueBoolean)
		{
			return false;
		}
		if (valueBooleanN == null)
		{
			if (other.valueBooleanN != null)
			{
				return false;
			}
		}
		else if (!valueBooleanN.equals(other.valueBooleanN))
		{
			return false;
		}
		if (valueByte != other.valueByte)
		{
			return false;
		}
		if (valueByteN == null)
		{
			if (other.valueByteN != null)
			{
				return false;
			}
		}
		else if (!valueByteN.equals(other.valueByteN))
		{
			return false;
		}
		if (valueCharacter != other.valueCharacter)
		{
			return false;
		}
		if (valueCharacterN == null)
		{
			if (other.valueCharacterN != null)
			{
				return false;
			}
		}
		else if (!valueCharacterN.equals(other.valueCharacterN))
		{
			return false;
		}
		if (Double.doubleToLongBits(valueDouble) != Double.doubleToLongBits(other.valueDouble))
		{
			return false;
		}
		if (valueDoubleN == null)
		{
			if (other.valueDoubleN != null)
			{
				return false;
			}
		}
		else if (!valueDoubleN.equals(other.valueDoubleN))
		{
			return false;
		}
		if (Float.floatToIntBits(valueFloat) != Float.floatToIntBits(other.valueFloat))
		{
			return false;
		}
		if (valueFloatN == null)
		{
			if (other.valueFloatN != null)
			{
				return false;
			}
		}
		else if (!valueFloatN.equals(other.valueFloatN))
		{
			return false;
		}
		if (valueInteger != other.valueInteger)
		{
			return false;
		}
		if (valueIntegerN == null)
		{
			if (other.valueIntegerN != null)
			{
				return false;
			}
		}
		else if (!valueIntegerN.equals(other.valueIntegerN))
		{
			return false;
		}
		if (valueLong != other.valueLong)
		{
			return false;
		}
		if (valueLongN == null)
		{
			if (other.valueLongN != null)
			{
				return false;
			}
		}
		else if (!valueLongN.equals(other.valueLongN))
		{
			return false;
		}
		if (valueString == null)
		{
			if (other.valueString != null)
			{
				return false;
			}
		}
		else if (!valueString.equals(other.valueString))
		{
			return false;
		}
		return true;
	}

}
