package com.koch.ambeth.util;

public final class HexUtil
{
	private static final char[] hexArray;

	private static final char[] hexArrayLowerLetters;

	private static final byte[] byteArray;

	static
	{
		hexArray = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		hexArrayLowerLetters = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		byteArray = new byte[256];
		byteArray['0'] = 0;
		byteArray['1'] = 1;
		byteArray['2'] = 2;
		byteArray['3'] = 3;
		byteArray['4'] = 4;
		byteArray['5'] = 5;
		byteArray['6'] = 6;
		byteArray['7'] = 7;
		byteArray['8'] = 8;
		byteArray['9'] = 9;
		byteArray['A'] = 10;
		byteArray['a'] = 10;
		byteArray['B'] = 11;
		byteArray['b'] = 11;
		byteArray['C'] = 12;
		byteArray['c'] = 12;
		byteArray['D'] = 13;
		byteArray['d'] = 13;
		byteArray['E'] = 14;
		byteArray['e'] = 14;
		byteArray['F'] = 15;
		byteArray['f'] = 15;
	}

	public static String toHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String toHexLowerLetters(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0, size = bytes.length; j < size; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArrayLowerLetters[v >>> 4];
			hexChars[j * 2 + 1] = hexArrayLowerLetters[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] toBytes(String hex)
	{
		if (hex.length() == 0)
		{
			return new byte[0];
		}
		if (hex.length() == 1)
		{
			return new byte[] { byteArray[hex.charAt(0)] };
		}
		byte[] bytes = new byte[hex.length() / 2];
		int byteIndex = 0;
		for (int j = 0, size = hex.length(); j < size; j += 2)
		{
			char upperChar = hex.charAt(j);
			char lowerChar = hex.charAt(j + 1);
			bytes[byteIndex++] = (byte) ((byteArray[upperChar] << 4) + byteArray[lowerChar]);
		}
		return bytes;
	}

	private HexUtil()
	{
		// intended blank
	}
}
