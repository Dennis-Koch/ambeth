package de.osthus.ambeth.shell.core.resulttype;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.shell.util.Utils;

public class TableResult<T> extends CommandResult
{
	private SortedMap<Integer, String> indexToColumnNameMap = new TreeMap<Integer, String>();

	private List<Row> rows = new ArrayList<Row>();

	public String[] getColumnNames()
	{
		return indexToColumnNameMap.values().toArray(new String[indexToColumnNameMap.size()]);
	}

	public String getColumnName(int index)
	{
		return indexToColumnNameMap.get(index);
	}

	public int getColumnIndex(String columnName)
	{
		for (Integer cIndex : indexToColumnNameMap.keySet())
		{
			if (indexToColumnNameMap.get(cIndex) == null && columnName == null)
			{
				return cIndex;
			}
			else if (indexToColumnNameMap.get(cIndex) == null)
			{
				continue;
			}
			if (indexToColumnNameMap.get(cIndex).equals(columnName))
			{
				return cIndex;
			}
		}
		return -1;
	}

	/**
	 * add headers to the table
	 * 
	 * @param header
	 *            header name
	 */
	public void addHeader(String... header)
	{
		if (header == null)
		{
			return;
		}
		for (int i = 0; i < header.length; i++)
		{
			indexToColumnNameMap.put(indexToColumnNameMap.size(), header[i]);
		}
	}

	/**
	 * add a row to the table
	 * 
	 * @param row
	 *            instance of {@link Row}
	 */
	public void addRow(Row row)
	{
		rows.add(row);
	}

	/**
	 * add a row to the table
	 * 
	 * @param values
	 *            values of the row
	 */
	public void addNewRow(List<T> values)
	{
		Row row = new Row();
		row.values.addAll(values);
		rows.add(row);
	}

	/**
	 * remove a row from the table
	 * 
	 * @param row
	 *            instance of {@link Row}
	 */
	public void removeRow(Row row)
	{
		rows.remove(row);
	}

	/**
	 * remove a row from the table
	 * 
	 * @param rowNumber
	 *            int
	 */
	public void removeRow(int rowNumber)
	{
		rows.remove(rowNumber);
	}

	/**
	 * get total count of all rows of the table
	 * 
	 * @return total row count
	 */
	public int getRowCount()
	{
		return rows.size();
	}

	/**
	 * get all the rows
	 * 
	 * @return List<{@link Row}
	 */
	public List<Row> getRows()
	{
		return rows;
	}

	/**
	 * get a row
	 * 
	 * @return List<{@link Row}
	 */
	public Row getRow(int index)
	{
		return rows.get(index);
	}

	private Map<Integer, Integer> calColWidths()
	{
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		// Widths of headers
		Set<Integer> keySet = indexToColumnNameMap.keySet();
		for (Integer index : keySet)
		{
			map.put(index, (indexToColumnNameMap.get(index) == null ? 0 : indexToColumnNameMap.get(index).length()));
		}

		// Preparation pass : find the maximum width for each column
		for (int j = 0; j < rows.size(); j++)
		{
			for (Integer index : keySet)
			{
				String data = rows.get(j).getValue(index) == null ? "" : rows.get(j).getValue(index).toString();
				if (map.get(index) < data.length())
				{
					map.put(index, data.length());
				}
			}
		}
		return map;
	}

	private void printSeparateLine(PrintWriter pw, Map<Integer, Integer> colWidths)
	{
		int max;
		final int padding = 3;

		Set<Integer> keySet = indexToColumnNameMap.keySet();
		for (Integer index : keySet)
		{
			max = colWidths.get(index);
			pw.print(Utils.stringPadEnd("+" + "", max + padding, '-'));
		}
		pw.println("+");
	}

	@Override
	public String toString()
	{
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try
		{
			int max;
			String value = null;
			final int padding = 3;
			final Map<Integer, Integer> colWidths = calColWidths();

			printSeparateLine(pw, colWidths);

			Set<Integer> keySet = indexToColumnNameMap.keySet();
			for (Integer index : keySet)
			{
				max = colWidths.get(index);
				value = indexToColumnNameMap.get(index) == null ? "" : indexToColumnNameMap.get(index);
				value = Utils.stringPadEnd("| " + value + "", max + padding, ' ');
				pw.print(value);
			}

			pw.println("|");

			printSeparateLine(pw, colWidths);

			for (int r = 0; r < rows.size(); r++)
			{
				for (Integer index : keySet)
				{
					max = colWidths.get(index);
					value = rows.get(r).getValue(index) != null ? rows.get(r).getValue(index).toString() : "";
					value = Utils.stringPadEnd("| " + value + "", max + padding, ' ');
					pw.print(value);
				}
				pw.println("|");
			}

			printSeparateLine(pw, colWidths);

			return strWriter.toString();
		}
		finally
		{
			pw.close();
			try
			{
				strWriter.close();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	public class Row implements Iterable<T>
	{
		private List<T> values = new ArrayList<T>();

		public T getValue(int index)
		{
			return values.get(index);
		}

		public T getValue(String columnName)
		{
			return this.getValue(getColumnIndex(columnName));
		}

		public void addValue(T value)
		{
			values.add(value);
		}

		@Override
		public Iterator<T> iterator()
		{
			return values.iterator();
		}
	}

}