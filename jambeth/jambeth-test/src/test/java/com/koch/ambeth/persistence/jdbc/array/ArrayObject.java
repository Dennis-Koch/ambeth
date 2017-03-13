package com.koch.ambeth.persistence.jdbc.array;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public class ArrayObject extends AbstractEntity
{
	protected String[] arrayContentString;

	protected List<String> listContentString;

	protected double[] arrayContentDouble;

	protected Double[] arrayContentDouble2;

	protected float[] arrayContentFloat;

	protected Float[] arrayContentFloat2;

	protected long[] arrayContentLong;

	protected Long[] arrayContentLong2;

	protected Set<Long> setContentLong;

	protected List<Long> listContentLong;

	protected Collection<Long> collContentLong;

	protected int[] arrayContentInt;

	protected Integer[] arrayContentInt2;

	protected short[] arrayContentShort;

	protected Short[] arrayContentShort2;

	protected byte[] arrayContentByte;

	protected Byte[] arrayContentByte2;

	protected char[] arrayContentChar;

	protected Character[] arrayContentChar2;

	protected boolean[] arrayContentBool;

	protected Boolean[] arrayContentBool2;

	protected ArrayObject()
	{
		// Intended blank
	}

	public String[] getArrayContentString()
	{
		return arrayContentString;
	}

	public void setArrayContentString(String[] arrayContentString)
	{
		this.arrayContentString = arrayContentString;
	}

	public List<String> getListContentString()
	{
		return listContentString;
	}

	public void setListContentString(List<String> listContentString)
	{
		this.listContentString = listContentString;
	}

	public double[] getArrayContentDouble()
	{
		return arrayContentDouble;
	}

	public void setArrayContentDouble(double[] arrayContentDouble)
	{
		this.arrayContentDouble = arrayContentDouble;
	}

	public float[] getArrayContentFloat()
	{
		return arrayContentFloat;
	}

	public void setArrayContentFloat(float[] arrayContentFloat)
	{
		this.arrayContentFloat = arrayContentFloat;
	}

	public long[] getArrayContentLong()
	{
		return arrayContentLong;
	}

	public void setArrayContentLong(long[] arrayContentLong)
	{
		this.arrayContentLong = arrayContentLong;
	}

	public int[] getArrayContentInt()
	{
		return arrayContentInt;
	}

	public void setArrayContentInt(int[] arrayContentInt)
	{
		this.arrayContentInt = arrayContentInt;
	}

	public short[] getArrayContentShort()
	{
		return arrayContentShort;
	}

	public void setArrayContentShort(short[] arrayContentShort)
	{
		this.arrayContentShort = arrayContentShort;
	}

	public byte[] getArrayContentByte()
	{
		return arrayContentByte;
	}

	public void setArrayContentByte(byte[] arrayContentByte)
	{
		this.arrayContentByte = arrayContentByte;
	}

	public char[] getArrayContentChar()
	{
		return arrayContentChar;
	}

	public void setArrayContentChar(char[] arrayContentChar)
	{
		this.arrayContentChar = arrayContentChar;
	}

	public boolean[] getArrayContentBool()
	{
		return arrayContentBool;
	}

	public void setArrayContentBool(boolean[] arrayContentBool)
	{
		this.arrayContentBool = arrayContentBool;
	}

	public Double[] getArrayContentDouble2()
	{
		return arrayContentDouble2;
	}

	public void setArrayContentDouble2(Double[] arrayContentDouble2)
	{
		this.arrayContentDouble2 = arrayContentDouble2;
	}

	public Float[] getArrayContentFloat2()
	{
		return arrayContentFloat2;
	}

	public void setArrayContentFloat2(Float[] arrayContentFloat2)
	{
		this.arrayContentFloat2 = arrayContentFloat2;
	}

	public Long[] getArrayContentLong2()
	{
		return arrayContentLong2;
	}

	public void setArrayContentLong2(Long[] arrayContentLong2)
	{
		this.arrayContentLong2 = arrayContentLong2;
	}

	public Integer[] getArrayContentInt2()
	{
		return arrayContentInt2;
	}

	public void setArrayContentInt2(Integer[] arrayContentInt2)
	{
		this.arrayContentInt2 = arrayContentInt2;
	}

	public Short[] getArrayContentShort2()
	{
		return arrayContentShort2;
	}

	public void setArrayContentShort2(Short[] arrayContentShort2)
	{
		this.arrayContentShort2 = arrayContentShort2;
	}

	public Byte[] getArrayContentByte2()
	{
		return arrayContentByte2;
	}

	public void setArrayContentByte2(Byte[] arrayContentByte2)
	{
		this.arrayContentByte2 = arrayContentByte2;
	}

	public Character[] getArrayContentChar2()
	{
		return arrayContentChar2;
	}

	public void setArrayContentChar2(Character[] arrayContentChar2)
	{
		this.arrayContentChar2 = arrayContentChar2;
	}

	public Boolean[] getArrayContentBool2()
	{
		return arrayContentBool2;
	}

	public void setArrayContentBool2(Boolean[] arrayContentBool2)
	{
		this.arrayContentBool2 = arrayContentBool2;
	}

	public Set<Long> getSetContentLong()
	{
		return setContentLong;
	}

	public void setSetContentLong(Set<Long> setContentLong)
	{
		this.setContentLong = setContentLong;
	}

	public List<Long> getListContentLong()
	{
		return listContentLong;
	}

	public void setListContentLong(List<Long> listContentLong)
	{
		this.listContentLong = listContentLong;
	}

	public Collection<Long> getCollContentLong()
	{
		return collContentLong;
	}

	public void setCollContentLong(Collection<Long> collContentLong)
	{
		this.collContentLong = collContentLong;
	}
}
