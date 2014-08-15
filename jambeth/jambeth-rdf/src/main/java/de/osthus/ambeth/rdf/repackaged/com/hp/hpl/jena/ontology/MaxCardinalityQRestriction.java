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

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////

/**
 * <p>
 * Interface representing the ontology abstraction for a qualified maximum cardinality
 * restriction.   A qualified restriction is a DAML+OIL term for a restriction
 * with a cardinality constraint <em>and</em> a constraint that the values of
 * the restricted property must all belong to the given class.  At the current 
 * time, qualified restrictions are part of DAML+OIL, but not part of OWL.
 * </p>
 */
public interface MaxCardinalityQRestriction 
    extends QualifiedRestriction
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that this restriction restricts the property to have the given
     * maximum cardinality. Any existing statements for <code>maxCardinalityQ</code>
     * will be removed.</p>
     * @param maxCardinality The maximum cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY_Q()} property is not supported in the current language profile.   
     */ 
    public void setMaxCardinalityQ( int maxCardinality );

    /**
     * <p>Answer the max qualified cardinality of the restricted property.</p>
     * @return The cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY_Q()} property is not supported in the current language profile.   
     */ 
    public int getMaxCardinalityQ();

    /**
     * <p>Answer true if this property restriction has the given maximum qualifed cardinality.</p>
     * @param maxCardinality The cardinality to test against 
     * @return True if the given cardinality is the maximum qualified cardinality of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY_Q()} property is not supported in the current language profile.   
     */
    public boolean hasMaxCardinalityQ( int maxCardinality );
    
    /**
     * <p>Remove the statement that this restriction has the given maximum qualified cardinality 
     * for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param maxCardinality A cardinality value to be removed from this restriction
     * @exception ProfileException If the {@link Profile#MAX_CARDINALITY_Q()} property is not supported in the current language profile.   
     */
    public void removeMaxCardinalityQ( int maxCardinality );
    


}
