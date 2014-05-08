﻿using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using AmbethCompositeFilterDescriptor = De.Osthus.Ambeth.Filter.Model.CompositeFilterDescriptor;
using AmbethFilterDescriptor = De.Osthus.Ambeth.Filter.Model.FilterDescriptor;
using AmbethFilterOperator = De.Osthus.Ambeth.Filter.Model.FilterOperator;
using AmbethIFilterDescriptor = De.Osthus.Ambeth.Filter.Model.IFilterDescriptor;
using AmbethLogicalOperator = De.Osthus.Ambeth.Filter.Model.LogicalOperator;
using TelerikColumnFilterDescriptor = Telerik.Windows.Controls.GridView.ColumnFilterDescriptor;
using TelerikDistinctValuesFilterDescriptor = Telerik.Windows.Controls.GridView.DistinctValuesFilterDescriptor;
using TelerikFieldFilterDescriptor = Telerik.Windows.Controls.GridView.FieldFilterDescriptor;
using TelerikFilterDescriptor = Telerik.Windows.Data.FilterDescriptor;
using TelerikFilterOperator = Telerik.Windows.Data.FilterOperator;
using TelerikIFilterDescriptor = Telerik.Windows.Data.IFilterDescriptor;
using TelerikLogicalOperator = Telerik.Windows.Data.FilterCompositionLogicalOperator;


namespace De.Osthus.Minerva.Converter
{
    public class FilterDescriptorConverter : IFilterDescriptorConverter, IInitializingBean
    {
        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        // Extend an Ambeth filter descriptor by an arbitrary Telerik filter descriptor:
        public virtual AmbethIFilterDescriptor AddTelerikFilterDescriptor(AmbethIFilterDescriptor ambethFilter, TelerikIFilterDescriptor telerikFilterDescriptor, AmbethLogicalOperator logicalOperator = AmbethLogicalOperator.AND)
        {
            return Compose(ambethFilter, ConvertTelerikFilterDescriptor(telerikFilterDescriptor), logicalOperator);
        }

        // Convert a complete list of arbitrary Telerik filter descriptors into a single Ambeth filter descriptor:
        public virtual AmbethIFilterDescriptor ConvertTelerikFilterCollection(IList<TelerikIFilterDescriptor> filterCollection, AmbethLogicalOperator logicalOperator = AmbethLogicalOperator.AND)
        {
            if (filterCollection.Count < 1)
            {
                return null;
            }
            AmbethIFilterDescriptor resultFilter = ConvertTelerikFilterDescriptor(filterCollection[0]);
            for (int i = 1; i < filterCollection.Count; ++i)
            {
                resultFilter = AddTelerikFilterDescriptor(resultFilter, filterCollection[i], logicalOperator);
            }
            return resultFilter;
        }

        // Convert a single arbitrary Telerik filter descriptor into an Ambeth filter descriptor:
        public virtual AmbethIFilterDescriptor ConvertTelerikFilterDescriptor(TelerikIFilterDescriptor telerikFilterDescriptor)
        {
            if (telerikFilterDescriptor == null)
            {
                return null;
            }
            // A TelerikFilterDescriptor corresponds to an Ambeth FilterDescriptor:
            else if (telerikFilterDescriptor is TelerikFilterDescriptor)
            {
                if ((telerikFilterDescriptor as TelerikFilterDescriptor).Value == null)
                {
                    return null;
                }
                return ConvertTelerikSimpleFilterDescriptor(telerikFilterDescriptor as TelerikFilterDescriptor);
            }
            // A TelerikDistinctValuesFilterDescriptor has a list of TelerikFilterDescriptor objects:
            else if (telerikFilterDescriptor is TelerikDistinctValuesFilterDescriptor)
            {
                return ConvertTelerikFilterCollection((telerikFilterDescriptor as TelerikDistinctValuesFilterDescriptor).FilterDescriptors);
            }
            // A TelerikFieldFilterDescriptor consists of two TelerikFilterDescriptor filters that are logically related:
            else if (telerikFilterDescriptor is TelerikFieldFilterDescriptor)
            {
                if (!(telerikFilterDescriptor as TelerikFieldFilterDescriptor).IsActive)
                {
                    return null;
                }
                return ConvertTelerikFilterCollection((telerikFilterDescriptor as TelerikFieldFilterDescriptor).FilterDescriptors,
                                                      ConvertLogicalOperator((telerikFilterDescriptor as TelerikFieldFilterDescriptor).LogicalOperator));
            }
            // A TelerikColumnFilterDescriptor consists of a TelerikDistinctValuesFilterDescriptor and a TelerikFieldFilterDescriptor:
            else if (telerikFilterDescriptor is TelerikColumnFilterDescriptor)
            {
                if (!(telerikFilterDescriptor as TelerikColumnFilterDescriptor).IsActive)
                {
                    return null;
                }
                return Compose(ConvertTelerikFilterDescriptor((telerikFilterDescriptor as TelerikColumnFilterDescriptor).DistinctFilter),
                               ConvertTelerikFilterDescriptor((telerikFilterDescriptor as TelerikColumnFilterDescriptor).FieldFilter),
                               ConvertLogicalOperator((telerikFilterDescriptor as TelerikColumnFilterDescriptor).LogicalOperator));
            }
            throw new ArgumentException("Unknown Telerik filter type");
        }

        public virtual AmbethIFilterDescriptor RemoveTelerikFilterDescriptor(AmbethIFilterDescriptor ambethFilter, TelerikIFilterDescriptor telerikFilterDescriptor)
        {
            AmbethIFilterDescriptor toRemoveFilter = ConvertTelerikFilterDescriptor(telerikFilterDescriptor);
            return RemoveAmbethFilterDescriptor(ambethFilter, toRemoveFilter);
        }

        public virtual AmbethIFilterDescriptor Compose(AmbethIFilterDescriptor filter1, AmbethIFilterDescriptor filter2, AmbethLogicalOperator logicalOperator = AmbethLogicalOperator.AND)
        {
            // Try to simplify both filters before combining them:
            filter1 = TryReduce(filter1);
            if (filter1 == null)
            {
                return TryReduce(filter2);
            }
            filter2 = TryReduce(filter2);
            if (filter2 == null)
            {
                return filter1;
            }

            if (filter1 is AmbethCompositeFilterDescriptor)
            {
                if (filter2 is AmbethCompositeFilterDescriptor)
                {
                    return ComposeTwoComposite(filter1 as AmbethCompositeFilterDescriptor, filter2 as AmbethCompositeFilterDescriptor, logicalOperator);
                }
                return ComposeSimpleAndComposite(filter2 as AmbethFilterDescriptor, filter1 as AmbethCompositeFilterDescriptor, logicalOperator);
            }

            if (filter2 is AmbethCompositeFilterDescriptor)
            {
                return ComposeSimpleAndComposite(filter1 as AmbethFilterDescriptor, filter2 as AmbethCompositeFilterDescriptor, logicalOperator);
            }

            AmbethCompositeFilterDescriptor newComposite = new AmbethCompositeFilterDescriptor();
            newComposite.LogicalOperator = logicalOperator;
            newComposite.ChildFilterDescriptors = new List<AmbethIFilterDescriptor>();
            newComposite.ChildFilterDescriptors.Add(filter1);
            newComposite.ChildFilterDescriptors.Add(filter2);
            return newComposite;
        }

#if DEBUG
        // Return a String that visualizes the complete filter hierarchy:
        public virtual String DebugVisualize(AmbethIFilterDescriptor filter, String prefix, String logicalOperator)
        {
            String result;
            if (filter is AmbethFilterDescriptor)
            {
                result = prefix + logicalOperator + " " + filter.Member + " " + filter.Operator.ToString() + " " + filter.Value;
                if (filter.IsCaseSensitive == true)
                {
                    result += " (case sensitive)";
                }
                result += "\n";
                return result;
            }
            else
            {
                result = prefix + logicalOperator + " {\n";
                String newPrefix = prefix + "   " + new String(' ', logicalOperator.Length);
                bool first = true;
                String newLogicalOperator;
                if (filter.ChildFilterDescriptors != null)
                {
                    foreach (AmbethIFilterDescriptor filterDescriptor in filter.ChildFilterDescriptors)
                    {
                        if (first)
                        {
                            first = false;
                            newLogicalOperator = "   ";
                        }
                        else
                        {
                            if (filter.LogicalOperator == AmbethLogicalOperator.AND)
                            {
                                newLogicalOperator = "AND";
                            }
                            else
                            {
                                newLogicalOperator = " OR";
                            }
                        }
                        result += DebugVisualize(filterDescriptor, newPrefix, newLogicalOperator);
                    }
                }
                result += prefix + new String(' ', logicalOperator.Length) + " }\n";
                return result;
            }
        }
#endif

        // Convert Telerik enum for logical operators into Ambeth equivalent:
        protected virtual AmbethLogicalOperator ConvertLogicalOperator(TelerikLogicalOperator telerikLogicalOperator)
        {
            switch (telerikLogicalOperator)
            {
                case TelerikLogicalOperator.And:
                    return AmbethLogicalOperator.AND;
                case TelerikLogicalOperator.Or:
                    return AmbethLogicalOperator.OR;
                default:
                    throw new ArgumentException("No conversion of Telerik LogicalOperator '" + telerikLogicalOperator + "' possible");
            }
        }

        // Convert Telerik enum for filter operators into Ambeth equivalent:
        protected virtual AmbethFilterOperator ConvertFilterOperator(TelerikFilterOperator telerikFilterOperator)
        {
            switch (telerikFilterOperator)
            {
                case TelerikFilterOperator.Contains:
                    return AmbethFilterOperator.CONTAINS;
                case TelerikFilterOperator.DoesNotContain:
                    return AmbethFilterOperator.DOES_NOT_CONTAIN;
                case TelerikFilterOperator.EndsWith:
                    return AmbethFilterOperator.ENDS_WITH;
                case TelerikFilterOperator.IsContainedIn:
                    return AmbethFilterOperator.IS_CONTAINED_IN;
                case TelerikFilterOperator.IsEqualTo:
                    return AmbethFilterOperator.IS_EQUAL_TO;
                case TelerikFilterOperator.IsGreaterThan:
                    return AmbethFilterOperator.IS_GREATER_THAN;
                case TelerikFilterOperator.IsGreaterThanOrEqualTo:
                    return AmbethFilterOperator.IS_GREATER_THAN_OR_EQUAL_TO;
                case TelerikFilterOperator.IsLessThan:
                    return AmbethFilterOperator.IS_LESS_THAN;
                case TelerikFilterOperator.IsLessThanOrEqualTo:
                    return AmbethFilterOperator.IS_LESS_THAN_OR_EQUAL_TO;
                case TelerikFilterOperator.IsNotContainedIn:
                    return AmbethFilterOperator.IS_NOT_CONTAINED_IN;
                case TelerikFilterOperator.IsNotEqualTo:
                    return AmbethFilterOperator.IS_NOT_EQUAL_TO;
                case TelerikFilterOperator.StartsWith:
                    return AmbethFilterOperator.STARTS_WITH;
                default:
                    throw new ArgumentException("No conversion of Telerik FilterOperator '" + telerikFilterOperator + "' possible");
            }
        }

        protected virtual bool IsMatch(AmbethIFilterDescriptor filter1, AmbethIFilterDescriptor filter2)
        {
            if (filter1 is AmbethFilterDescriptor)
            {
                if (filter2 is AmbethCompositeFilterDescriptor)
                {
                    return false;
                }
                if (filter1.Member != filter2.Member ||
                    filter1.Value != filter2.Value ||
                    filter1.Operator != filter2.Operator ||
                    filter1.IsCaseSensitive != filter2.IsCaseSensitive)
                {
                    return false;
                }
                return true;
            }
            if (filter2 is AmbethFilterDescriptor)
            {
                return false;
            }
            if (filter1.ChildFilterDescriptors.Count != filter2.ChildFilterDescriptors.Count)
            {
                return false;
            }
            foreach (AmbethIFilterDescriptor child1 in filter1.ChildFilterDescriptors)
            {
                bool isMatched = false;
                foreach (AmbethIFilterDescriptor child2 in filter2.ChildFilterDescriptors)
                {
                    if (IsMatch(child1, child2))
                    {
                        isMatched = true;
                        break;
                    }
                }
                if (!isMatched)
                {
                    return false;
                }
            }
            return true;
        }

        protected virtual AmbethIFilterDescriptor RemoveAmbethFilterDescriptor(AmbethIFilterDescriptor ambethFilter, AmbethIFilterDescriptor toRemoveFilter)
        {
            if (IsMatch(ambethFilter, toRemoveFilter))
            {
                return null;
            }
            if (ambethFilter is AmbethFilterDescriptor)
            {
                return ambethFilter;
            }
            foreach (AmbethIFilterDescriptor child in ambethFilter.ChildFilterDescriptors)
            {
                if (IsMatch(child, toRemoveFilter))
                {
                    ambethFilter.ChildFilterDescriptors.Remove(child);
                    return ambethFilter;
                }
            }
            return ambethFilter;
        }

        // Tries to simplify a given AmbethFilter:
        protected virtual AmbethIFilterDescriptor TryReduce(AmbethIFilterDescriptor filter)
        {
            if (filter == null)
            {
                return null;
            }
            if (filter is AmbethFilterDescriptor)
            {
                return filter;
            }
            if (filter.ChildFilterDescriptors == null || filter.ChildFilterDescriptors.Count == 0)
            {
                return null;
            }
            if (filter.ChildFilterDescriptors.Count == 1)
            {
                return TryReduce(filter.ChildFilterDescriptors[0]);
            }
            return filter;
        }

        protected virtual AmbethIFilterDescriptor ComposeTwoComposite(AmbethCompositeFilterDescriptor filter1, AmbethCompositeFilterDescriptor filter2, AmbethLogicalOperator logicalOperator)
        {
            if (filter1.LogicalOperator == logicalOperator)
            {
                // If the logical operators of both composites are equal to the logical operator that relates them, they can be combined into a single composite: 
                if (filter2.LogicalOperator == logicalOperator)
                {
                    foreach (AmbethIFilterDescriptor filter in filter2.ChildFilterDescriptors)
                    {
                        filter1.ChildFilterDescriptors.Add(filter);
                    }
                    return filter1;
                }
                // If only one of the composites has the same logical operator that relates them, the other can be added to its childs:
                filter1.ChildFilterDescriptors.Add(filter2);
                return filter1;
            }
            if (filter2.LogicalOperator == logicalOperator)
            {
                filter2.ChildFilterDescriptors.Add(filter1);
                return filter2;
            }
            // The logical operators do not match, hence a new composite must be created:
            AmbethCompositeFilterDescriptor newComposite = new AmbethCompositeFilterDescriptor();
            newComposite.LogicalOperator = logicalOperator;
            newComposite.ChildFilterDescriptors = new List<AmbethIFilterDescriptor>();
            newComposite.ChildFilterDescriptors.Add(filter1);
            newComposite.ChildFilterDescriptors.Add(filter2);
            return newComposite;
        }

        protected virtual AmbethIFilterDescriptor ComposeSimpleAndComposite(AmbethFilterDescriptor filter1, AmbethCompositeFilterDescriptor filter2, AmbethLogicalOperator logicalOperator)
        {
            if (filter2.LogicalOperator == logicalOperator)
            {
                filter2.ChildFilterDescriptors.Add(filter1);
                return filter2;
            }
            AmbethCompositeFilterDescriptor newComposite = new AmbethCompositeFilterDescriptor();
            newComposite.LogicalOperator = logicalOperator;
            newComposite.ChildFilterDescriptors = new List<AmbethIFilterDescriptor>();
            newComposite.ChildFilterDescriptors.Add(filter1);
            newComposite.ChildFilterDescriptors.Add(filter2);
            return newComposite;
        }

        protected virtual AmbethIFilterDescriptor ConvertTelerikSimpleFilterDescriptor(TelerikFilterDescriptor telerikFilterDescriptor)
        {
            AmbethFilterDescriptor newFilter = new AmbethFilterDescriptor();
            newFilter.Member = telerikFilterDescriptor.Member;
            newFilter.Value = new List<String>();
            newFilter.Value.Add(telerikFilterDescriptor.Value.ToString());
            newFilter.Operator = ConvertFilterOperator(telerikFilterDescriptor.Operator);
            newFilter.IsCaseSensitive = telerikFilterDescriptor.IsCaseSensitive;
            return newFilter;
        }
    }
}