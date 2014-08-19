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

package de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.reasoner.rulesys;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.graph.*;

/**
 * Rules employ builtins to do all tests and actions other than simple triple matches and triple creation.
 * <p>
 * Builtins can be invoked in two contexts. In the head of forward rules they perform some action based on the variable bindings generated by the body and
 * additional context (the graph being reasoned over, the set of triples bound by the body). In the body of rules they perform tests, and additional variable
 * bindings.
 * <p>
 * The mapping from the rule definition (which uses functors to hold the parsed call) to the java implementation of the builtin is done via the
 * {@link BuiltinRegistry BuiltinRegistry} which can be user extended.
 */
public interface Builtin
{

	/**
	 * Return a convenient name for this builtin, normally this will be the name of the functor that will be used to invoke it and will often be the final
	 * component of the URI.
	 */
	public String getName();

	/**
	 * Return the full URI which identifies this built in.
	 */
	public String getURI();

	/**
	 * Return the expected number of arguments for this functor or 0 if the number is flexible.
	 */
	public int getArgLength();

	/**
	 * This method is invoked when the builtin is called in a rule body.
	 * 
	 * @param args
	 *            the array of argument values for the builtin, this is an array of Nodes, some of which may be Node_RuleVariables.
	 * @param length
	 *            the length of the argument list, may be less than the length of the args array for some rule engines
	 * @param context
	 *            an execution context giving access to other relevant data
	 * @return return true if the buildin predicate is deemed to have succeeded in the current environment
	 */
	public boolean bodyCall(Node[] args, int length, RuleContext context);

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
	public void headAction(Node[] args, int length, RuleContext context);

	/**
	 * Returns false if this builtin has side effects when run in a body clause, other than the binding of environment variables.
	 */
	public boolean isSafe();

	/**
	 * Returns false if this builtin is non-monotonic. This includes non-monotonic checks like noValue and non-monotonic actions like remove/drop. A
	 * non-monotonic call in a head is assumed to be an action and makes the overall rule and ruleset non-monotonic. Most JenaRules are monotonic deductive
	 * closure rules in which this should be false.
	 */
	public boolean isMonotonic();
}
