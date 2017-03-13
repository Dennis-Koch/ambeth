package com.koch.ambeth.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.shell.AmbethShell;
import com.koch.ambeth.shell.AmbethShellStarter;

public class AmbethShellStarterTest
{

	private static final String batchFileName = "AmbethShellStarterTest.as";

	protected static AmbethShell shell;
	private static ByteArrayOutputStream shellOut;
	private static ByteArrayOutputStream shellErr;

	private static PrintStream sysOut;
	private static PrintStream sysErr;

	@Before
	public void setup()
	{
		sysOut = System.out;
		sysErr = System.err;

		shellOut = new ByteArrayOutputStream();
		shellErr = new ByteArrayOutputStream();

		System.setOut(new PrintStream(shellOut));
		System.setErr(new PrintStream(shellErr));
	}

	@After
	public void restoreSystemIO() throws IOException
	{
		System.setOut(sysOut);
		System.setErr(sysErr);

		Files.deleteIfExists(Paths.get(".", batchFileName));
	}

	/**
	 * test executing batch file, with wrong format setting for variables from the command line
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrongFormatForBatchFile_1() throws IOException
	{
		executeAmbethShell(new String[] { batchFileName, "echo" });
	}

	/**
	 * test executing batch file, with wrong format setting for variables from the command line
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrongFormatForBatchFile_2() throws IOException
	{
		executeAmbethShell(new String[] { batchFileName, "helpCmd=" });
	}

	/**
	 * test executing batch file, with wrong format setting for variables from the command line
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrongFormatForBatchFile_3() throws IOException
	{
		executeAmbethShell(new String[] { batchFileName, "=echo" });
	}

	/**
	 * test executing batch file, with wrong format setting for variables from the command line
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrongFormatForBatchFile_4() throws IOException
	{
		executeAmbethShell(new String[] { batchFileName, "helpCmd=echo", "exit" });
	}

	/**
	 * test executing batch file, but without setting variables from the command line
	 *
	 * @throws Exception
	 */
	@Ignore(value = "Need to set useThread=false in AmbethShellStarter.afterStarted() because shellOut cannot be redirected")
	@Test
	public void testWithOutVarsForBatchFile() throws Exception
	{
		List<String> listAllCmd = getAllCmdsWithoutVariableSetting();

		prepareAndExecute(listAllCmd, new String[] { batchFileName });

		// verify the result of Ambeth-shell execution
		String outFromShell = shellOut.toString();
		for (String string : listAllCmd)
		{
			Assert.assertTrue(outFromShell + "\nshould contain the line:" + string, outFromShell.contains(string));
		}

		Assert.assertTrue(outFromShell + "\nthe 'help' command shoule output 'echo'.", outFromShell.contains("echo"));
		Assert.assertTrue(outFromShell + "\nthe 'help' command shoule output description of 'echo' command.",
				outFromShell.contains("Prints messages to the console"));
		Assert.assertTrue(outFromShell + "\nthe 'help' command shoule output description of 'exit' command.", outFromShell.contains("exit the shell"));
	}

	/**
	 * test executing batch file, with setting variables from the command line (not very file shellOut, because useThread=true in
	 * AmbethShellStarter.afterStarted())
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testHasVarsForBatchFile_NoVerify() throws ClassNotFoundException, IOException, InterruptedException
	{
		List<String> listAllCmd = getAllCmdsHasVariableSetting();
		prepareAndExecute(listAllCmd, new String[] { batchFileName, "helpCmd=echo" });
		Thread.sleep(2000L);// sleep to let ambeth-shell thread run
	}

	/**
	 * test executing batch file, with setting variables from the command line
	 *
	 * @throws IOException
	 */
	@Ignore(value = "Need to set useThread=false in AmbethShellStarter.afterStarted() because shellOut cannot be redirected")
	@Test
	public void testHasVarsForBatchFile() throws ClassNotFoundException, IOException
	{
		List<String> listAllCmd = getAllCmdsHasVariableSetting();
		prepareAndExecute(listAllCmd, new String[] { batchFileName, "helpCmd=echo" });

		// verify the result of Ambeth-shell execution
		String outFromShell = shellOut.toString();
		for (String string : listAllCmd)
		{
			Assert.assertTrue(outFromShell + "\nshould contain the line:" + string, outFromShell.contains(string));
		}

		Assert.assertTrue("the 'help' command shoule output 'echo'.", outFromShell.contains("echo"));
		Assert.assertTrue("the 'help' command shoule output description of 'echo' command.", outFromShell.contains("Prints messages to the console"));
		Assert.assertTrue("the 'help' command shoule not ouput 'exit' command.", !outFromShell.contains("exit the shell"));
	}

	/**
	 * test executing batch file, with setting variables from the command line and also in batch file
	 *
	 * @throws IOException
	 */
	@Ignore(value = "Need to set useThread=false in AmbethShellStarter.afterStarted() because shellOut cannot be redirected")
	@Test
	public void testOverlapVarsForBatchFile() throws ClassNotFoundException, IOException
	{
		List<String> listAllCmd = getAllCmdsOverlapVariableSetting();
		prepareAndExecute(listAllCmd, new String[] { batchFileName, "helpCmd=echo" });

		// verify the result of Ambeth-shell execution
		String outFromShell = shellOut.toString();
		for (String string : listAllCmd)
		{
			Assert.assertTrue(outFromShell + "\nshould contain the line:" + string, outFromShell.contains(string));
		}

		Assert.assertTrue("the 'help' command shoule not output 'echo'.", !outFromShell.contains("echo"));
		Assert.assertTrue("the 'help' command shoule not output description of 'echo' command.", !outFromShell.contains("Prints messages to the console"));
		Assert.assertTrue("the 'help' command shoule ouput 'exit' command.", outFromShell.contains("exit the shell"));
	}

	/**
	 * prepare the batch file and run Jambeth-Shell to execute it
	 *
	 * @throws IOException
	 */
	private void prepareAndExecute(List<String> listAllCmd, String... cmdArgs) throws IOException
	{
		Assert.assertTrue(listAllCmd.size() > 0);

		// create batch file(.as file) to include all the commands
		File asFile = new File(batchFileName);

		if (asFile.exists())
		{
			if (!asFile.delete())
			{
				throw new IllegalStateException("Unable to cleanup test resources!");
			}
		}

		Assert.assertTrue("should use a non-existing file for testing", !asFile.exists());
		asFile.createNewFile();
		OutputStream out = null;
		try
		{
			out = new FileOutputStream(asFile);
			for (String string : listAllCmd)
			{
				out.write((string + "\n").getBytes());
			}
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}

		executeAmbethShell(cmdArgs);
	}

	@SuppressWarnings("unchecked")
	private void executeAmbethShell(String... cmdArgs)
	{
		Properties ambethProperties = new Properties();
		ambethProperties.put(CoreConfigurationConstants.PackageScanPatterns, "n/a");
		// execute Ambeth-Shell
		AmbethShellStarter.start(cmdArgs, ambethProperties);
	}

	/**
	 * prepare some commands used in the batch file
	 *
	 * @return command lists
	 */
	private List<String> getAllCmdsWithoutVariableSetting()
	{
		List<String> listAllCmd = new ArrayList<String>();
		listAllCmd.add("set varTestParam1=123");
		listAllCmd.add("help");
		return listAllCmd;
	}

	/**
	 * prepare some commands used in the batch file(with variables that would be set from the command line)
	 *
	 * @return command lists
	 */
	private List<String> getAllCmdsHasVariableSetting()
	{
		List<String> listAllCmd = new ArrayList<String>();
		listAllCmd.add("set varTestParam1=123");
		listAllCmd.add("help $helpCmd");
		return listAllCmd;
	}

	/**
	 * prepare some commands used in the batch file(with variables that would be set from the command line and also in batch file)
	 *
	 * @return command lists
	 */
	private List<String> getAllCmdsOverlapVariableSetting()
	{
		List<String> listAllCmd = new ArrayList<String>();
		listAllCmd.add("set varTestParam1=123");
		listAllCmd.add("set helpCmd=exit");
		listAllCmd.add("help $helpCmd");
		return listAllCmd;
	}
}
