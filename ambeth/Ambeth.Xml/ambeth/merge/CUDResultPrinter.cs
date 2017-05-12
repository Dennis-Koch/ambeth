using De.Osthus.Ambeth.Appendable;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Incremental;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml;
using System;
using System.Collections.Generic;
using System.Text;
namespace De.Osthus.Ambeth.Merge
{
    public class CUDResultPrinter : ICUDResultPrinter
    {
	    [LogInstance]
	    public ILogger Log { private get; set; }

	    [Autowired]
	    public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
	    public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        protected readonly Comparison<CreateContainer> createContainerComparator = new Comparison<CreateContainer>(delegate(CreateContainer o1, CreateContainer o2)
		    {
			    return o1.Reference.RealType.FullName.CompareTo(o2.Reference.RealType.FullName);
		    });

        protected readonly Comparison<UpdateContainer> updateContainerComparator = new Comparison<UpdateContainer>(delegate(UpdateContainer o1, UpdateContainer o2)
        {
            return o1.Reference.RealType.FullName.CompareTo(o2.Reference.RealType.FullName);
        });

        protected readonly Comparison<DeleteContainer> deleteContainerComparator = new Comparison<DeleteContainer>(delegate(DeleteContainer o1, DeleteContainer o2)
        {
            return o1.Reference.RealType.FullName.CompareTo(o2.Reference.RealType.FullName);
        });

	    public String PrintCUDResult(ICUDResult cudResult, IIncrementalMergeState state)
	    {
		    StringBuilder sb = new StringBuilder();
			DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStringBuilder(sb), null);
			writer.SetBeautifierActive(true);
			writer.SetBeautifierLinebreak("\n");

			WriteCUDResult(cudResult, writer, (IncrementalMergeState) state);
			return sb.ToString();
	    }

	    protected void WriteCUDResult(ICUDResult cudResult, IWriter writer, IncrementalMergeState state)
	    {
		    IList<IChangeContainer> allChanges = cudResult.AllChanges;

		    List<CreateContainer> creates = new List<CreateContainer>();
		    List<UpdateContainer> updates = new List<UpdateContainer>();
		    List<DeleteContainer> deletes = new List<DeleteContainer>();

		    for (int a = allChanges.Count; a-- > 0;)
		    {
			    IChangeContainer changeContainer = allChanges[a];
			    if (changeContainer is CreateContainer)
			    {
				    creates.Add((CreateContainer) changeContainer);
			    }
			    else if (changeContainer is UpdateContainer)
			    {
				    updates.Add((UpdateContainer) changeContainer);
			    }
			    else
			    {
				    deletes.Add((DeleteContainer) changeContainer);
			    }
		    }
		    creates.Sort(createContainerComparator);
		    updates.Sort(updateContainerComparator);
		    deletes.Sort(deleteContainerComparator);

		    writer.WriteStartElement("CUDResult");
		    writer.WriteAttribute("size", allChanges.Count);
		    writer.WriteAttribute("creates", creates.Count);
		    writer.WriteAttribute("updates", updates.Count);
		    writer.WriteAttribute("deletes", deletes.Count);
		    writer.WriteStartElementEnd();

		    WriteChangeContainers(creates, writer, "Creates", state);
		    WriteChangeContainers(updates, writer, "Updates", state);
		    WriteChangeContainers(deletes, writer, "Deletes", state);

		    writer.WriteCloseElement("CUDResult");
	    }

	    protected void WriteChangeContainers<V>(IList<V> changes, IWriter writer, String elementName, IncrementalMergeState state) where V : IChangeContainer
	    {
		    if (changes.Count == 0)
		    {
			    return;
		    }
		    writer.WriteStartElement(elementName);
		    writer.WriteStartElementEnd();
		    for (int a = 0, size = changes.Count; a < size; a++)
		    {
			    WriteChangeContainer(changes[a], writer, state);
		    }
		    writer.WriteCloseElement(elementName);
	    }

	    protected void WriteChangeContainer(IChangeContainer changeContainer, IWriter writer, IncrementalMergeState incrementalState)
	    {
		    IObjRef objRef = changeContainer.Reference;

		    writer.WriteStartElement("Entity");
		    writer.WriteAttribute("type", objRef.RealType.FullName);
		    writer.WriteAttribute("id", ConversionHelper.ConvertValueToType<String>(objRef.Id));
		    if (objRef.Version != null)
		    {
			    writer.WriteAttribute("version", ConversionHelper.ConvertValueToType<String>(objRef.Version));
		    }
		    StateEntry stateEntry = incrementalState.objRefToStateMap.Get(objRef);
		    if (stateEntry == null)
		    {
			    throw new Exception();
		    }
		    writer.WriteAttribute("idx", stateEntry.index);
		    if (changeContainer is DeleteContainer)
		    {
			    writer.WriteEndElement();
			    return;
		    }
		    ICreateOrUpdateContainer createOrUpdate = (ICreateOrUpdateContainer) changeContainer;
		    WritePUIs(createOrUpdate.GetFullPUIs(), writer);
		    WriteRUIs(createOrUpdate.GetFullRUIs(), writer, incrementalState);
		    writer.WriteCloseElement("Entity");
	    }

	    protected void WriteRUIs(IRelationUpdateItem[] fullRUIs, IWriter writer, IncrementalMergeState state)
	    {
		    if (fullRUIs == null)
		    {
			    return;
		    }
		    foreach (IRelationUpdateItem rui in fullRUIs)
		    {
			    WriteRUI(rui, writer, state);
		    }
	    }

	    protected void WriteRUI(IRelationUpdateItem rui, IWriter writer, IncrementalMergeState state)
	    {
		    if (rui == null)
		    {
			    return;
		    }
		    IObjRef[] addedORIs = rui.AddedORIs;
		    IObjRef[] removedORIs = rui.RemovedORIs;

		    if (addedORIs != null)
		    {
			    writer.WriteStartElement(rui.MemberName);
			    writer.WriteAttribute("add", addedORIs.Length);
			    writer.WriteStartElementEnd();
			    WriteObjRefs(addedORIs, writer, state);
			    writer.WriteCloseElement(rui.MemberName);
		    }
		    if (removedORIs != null)
		    {
			    writer.WriteStartElement(rui.MemberName);
			    writer.WriteAttribute("remove", removedORIs.Length);
			    writer.WriteStartElementEnd();
			    WriteObjRefs(removedORIs, writer, state);
			    writer.WriteCloseElement(rui.MemberName);
		    }
	    }

	    protected void WriteObjRefs(IObjRef[] objRefs, IWriter writer, IncrementalMergeState incrementalState)
	    {
            IConversionHelper conversionHelper = ConversionHelper;
            IObjRef[] clone = (IObjRef[])objRefs.Clone();
		    Array.Sort(clone, incrementalState.objRefComparator);
		    foreach (IObjRef item in clone)
		    {
			    writer.WriteStartElement("Entity");
			    StateEntry stateEntry = incrementalState.objRefToStateMap.Get(item);
			    if (stateEntry != null)
			    {
				    writer.WriteAttribute("idx", stateEntry.index);
				    writer.WriteEndElement();
				    continue;
			    }
       			IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(item.RealType);
			    writer.WriteAttribute("type", metaData.EntityType.FullName);
			    PrimitiveMember idMember = metaData.GetIdMemberByIdIndex(item.IdNameIndex);
			    writer.WriteAttribute(idMember.Name, conversionHelper.ConvertValueToType<String>(item.Id));
			    if (item.Version != null)
			    {
				    writer.WriteAttribute("version", conversionHelper.ConvertValueToType<String>(item.Version));
			    }
			    writer.WriteEndElement();
		    }
	    }

	    protected void WritePUIs(IPrimitiveUpdateItem[] fullPUIs, IWriter writer)
	    {
		    if (fullPUIs == null)
		    {
			    return;
		    }
		    foreach (IPrimitiveUpdateItem pui in fullPUIs)
		    {
			    WritePUI(pui, writer);
		    }
	    }

	    protected void WritePUI(IPrimitiveUpdateItem pui, IWriter writer)
	    {
		    if (pui == null)
		    {
			    return;
		    }
		    Object newValue = pui.NewValue;
		    writer.WriteStartElement(pui.MemberName);

			String sValue = ConversionHelper.ConvertValueToType<String>(newValue);

			writer.WriteAttribute("value", sValue == null ? "null" : sValue.Length > 256 ? "[[skipped " + sValue.Length + " chars]]" : sValue);
		    writer.WriteEndElement();
	    }
    }
}