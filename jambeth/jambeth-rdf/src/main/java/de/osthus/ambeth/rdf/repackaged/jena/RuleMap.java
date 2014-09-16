/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.osthus.ambeth.rdf.repackaged.jena;

import static de.osthus.ambeth.rdf.repackaged.jena.cmdline.CmdLineUtils.setLog4jConfiguration;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.*;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.*;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.reasoner.Reasoner;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.reasoner.rulesys.*;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.FileManager;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.util.FileUtils;

import de.osthus.ambeth.rdf.repackaged.jena.cmdline.*;
import java.util.*;
import java.io.*;

/**
 * General command line utility to process one RDF file into another by application of a set of forward chaining rules.
 * 
 * <pre>
 * Usage:  RuleMap [-il inlang] [-ol outlang] [-d] rulefile infile
 * </pre>
 * 
 * The resulting RDF data is written to stdout in format <code>outlang</code> (default N3). If <code>-d</code> is given then only the deductions generated by
 * the rules are output. Otherwise all data including any input data (other than any removed triples) is output.
 * <p>
 * Rules are permitted an additional action "deduce" which forces triples to be added to the deductions graph even if they are already known (for use in
 * deductions only mode).
 * </p>
 */
public class RuleMap
{
	static
	{
		setLog4jConfiguration();
	}

	/**
	 * Load a set of rule definitions including processing of comment lines and any initial prefix definition lines. Also notes the prefix definitions for
	 * adding to a later inf model.
	 */
	public static List<Rule> loadRules(String filename, Map<String, String> prefixes)
	{
		String fname = filename;
		if (fname.startsWith("file:///"))
		{
			fname = File.separator + fname.substring(8);
		}
		else if (fname.startsWith("file:/"))
		{
			fname = File.separator + fname.substring(6);
		}
		else if (fname.startsWith("file:"))
		{
			fname = fname.substring(5);
		}

		BufferedReader src = FileUtils.openResourceFile(fname);
		return loadRules(src, prefixes);
	}

	/**
	 * Load a set of rule definitions including processing of comment lines and any initial prefix definition lines. Also notes the prefix definitions for
	 * adding to a later inf model.
	 */
	public static List<Rule> loadRules(BufferedReader src, Map<String, String> prefixes)
	{
		Rule.Parser parser = Rule.rulesParserFromReader(src);
		List<Rule> rules = Rule.parseRules(parser);
		prefixes.putAll(parser.getPrefixMap());
		return rules;
	}

	/**
	 * Internal implementation of the "deduce" primitve. This takes the form <code> ... -> deduce(s, p, o)</code>
	 */
	static class Deduce extends BaseBuiltin
	{

		/**
		 * Return a name for this builtin, normally this will be the name of the functor that will be used to invoke it.
		 */
		@Override
		public String getName()
		{
			return "deduce";
		}

		/**
		 * Return the expected number of arguments for this functor or 0 if the number is flexible.
		 */
		@Override
		public int getArgLength()
		{
			return 3;
		}

		/**
		 * This method is invoked when the builtin is called in a rule head. Such a use is only valid in a forward rule.
		 * 
		 * @param args
		 *            the array of argument values for the builtin, this is an array of Nodes.
		 * @param length
		 *            the length of the argument list, may be less than the length of the args array for some rule engines
		 * @param context
		 *            an execution context giving access to other relevant data
		 */
		@Override
		public void headAction(Node[] args, int length, RuleContext context)
		{
			if (context.getGraph() instanceof FBRuleInfGraph)
			{
				Triple t = new Triple(args[0], args[1], args[2]);
				((FBRuleInfGraph) context.getGraph()).addDeduction(t);
			}
			else
			{
				throw new BuiltinException(this, context, "Only usable in FBrule graphs");
			}
		}
	}

	/**
	 * General command line utility to process one RDF file into another by application of a set of forward chaining rules.
	 * 
	 * <pre>
	 * Usage:  RuleMap [-il inlang] [-ol outlang] -d infile rulefile
	 * </pre>
	 */
	public static void main(String[] args)
	{
		try
		{

			// Parse the command line
			CommandLine cl = new CommandLine();
			String usage = "Usage:  RuleMap [-il inlang] [-ol outlang] [-d] rulefile infile (- for stdin)";
			cl.setUsage(usage);
			cl.add("il", true);
			cl.add("ol", true);
			cl.add("d", false);
			cl.process(args);
			if (cl.numItems() != 2)
			{
				System.err.println(usage);
				System.exit(1);
			}

			// Load the input data
			Arg il = cl.getArg("il");
			String inLang = (il == null) ? null : il.getValue();
			String fname = cl.getItem(1);
			Model inModel = null;
			if (fname.equals("-"))
			{
				inModel = ModelFactory.createDefaultModel();
				inModel.read(System.in, null, inLang);
			}
			else
			{
				inModel = FileManager.get().loadModel(fname, inLang);
			}

			// Determine the type of the output
			Arg ol = cl.getArg("ol");
			String outLang = (ol == null) ? "N3" : ol.getValue();

			Arg d = cl.getArg("d");
			boolean deductionsOnly = (d != null);

			// Fetch the rule set and create the reasoner
			BuiltinRegistry.theRegistry.register(new Deduce());
			Map<String, String> prefixes = new HashMap<>();
			List<Rule> rules = loadRules(cl.getItem(0), prefixes);
			Reasoner reasoner = new GenericRuleReasoner(rules);

			// Process
			InfModel infModel = ModelFactory.createInfModel(reasoner, inModel);
			infModel.prepare();
			infModel.setNsPrefixes(prefixes);

			// Output
			try (PrintWriter writer = new PrintWriter(System.out))
			{
				if (deductionsOnly)
				{
					Model deductions = infModel.getDeductionsModel();
					deductions.setNsPrefixes(prefixes);
					deductions.setNsPrefixes(inModel);
					deductions.write(writer, outLang);
				}
				else
				{
					infModel.write(writer, outLang);
				}
			}
		}
		catch (Throwable t)
		{
			System.err.println("An error occured: \n" + t);
			t.printStackTrace();
		}
	}

}
