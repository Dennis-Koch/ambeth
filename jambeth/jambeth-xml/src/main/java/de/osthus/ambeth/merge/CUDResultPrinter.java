package de.osthus.ambeth.merge;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.incremental.IncrementalMergeState;
import de.osthus.ambeth.merge.incremental.IncrementalMergeState.StateEntry;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.ICreateOrUpdateContainer;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.xml.DefaultXmlWriter;
import de.osthus.ambeth.xml.IWriter;

public class CUDResultPrinter implements ICUDResultPrinter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected final Comparator<IChangeContainer> changeContainerComparator = new Comparator<IChangeContainer>()
	{
		@Override
		public int compare(IChangeContainer o1, IChangeContainer o2)
		{
			return o1.getReference().getRealType().getName().compareTo(o2.getReference().getRealType().getName());
		}
	};

	@Override
	public CharSequence printCUDResult(ICUDResult cudResult, IIncrementalMergeState state)
	{
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStringBuilder(sb), null);
			writer.setBeautifierActive(true);
			writer.setBeautifierLinebreak("\n");

			writeCUDResult(cudResult, writer, (IncrementalMergeState) state);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	protected void writeCUDResult(ICUDResult cudResult, IWriter writer, IncrementalMergeState state)
	{
		List<IChangeContainer> allChanges = cudResult.getAllChanges();

		ArrayList<CreateContainer> creates = new ArrayList<CreateContainer>();
		ArrayList<UpdateContainer> updates = new ArrayList<UpdateContainer>();
		ArrayList<DeleteContainer> deletes = new ArrayList<DeleteContainer>();

		for (int a = allChanges.size(); a-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			if (changeContainer instanceof CreateContainer)
			{
				creates.add((CreateContainer) changeContainer);
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				updates.add((UpdateContainer) changeContainer);
			}
			else
			{
				deletes.add((DeleteContainer) changeContainer);
			}
		}
		Collections.sort(creates, changeContainerComparator);
		Collections.sort(updates, changeContainerComparator);
		Collections.sort(deletes, changeContainerComparator);

		writer.writeStartElement("CUDResult");
		writer.writeAttribute("size", allChanges.size());
		writer.writeAttribute("creates", creates.size());
		writer.writeAttribute("updates", updates.size());
		writer.writeAttribute("deletes", deletes.size());
		writer.writeStartElementEnd();

		writeChangeContainers(creates, writer, "Creates", state);
		writeChangeContainers(updates, writer, "Updates", state);
		writeChangeContainers(deletes, writer, "Deletes", state);

		writer.writeCloseElement("CUDResult");
	}

	protected void writeChangeContainers(IList<? extends IChangeContainer> changes, IWriter writer, String elementName, IncrementalMergeState state)
	{
		if (changes.size() == 0)
		{
			return;
		}
		writer.writeStartElement(elementName);
		writer.writeStartElementEnd();
		for (int a = 0, size = changes.size(); a < size; a++)
		{
			writeChangeContainer(changes.get(a), writer, state);
		}
		writer.writeCloseElement(elementName);
	}

	protected void writeChangeContainer(IChangeContainer changeContainer, IWriter writer, IncrementalMergeState incrementalState)
	{
		IObjRef objRef = changeContainer.getReference();

		writer.writeStartElement("Entity");
		writer.writeAttribute("type", objRef.getRealType().getName());
		writer.writeAttribute("id", conversionHelper.convertValueToType(CharSequence.class, objRef.getId()));
		if (objRef.getVersion() != null)
		{
			writer.writeAttribute("version", conversionHelper.convertValueToType(CharSequence.class, objRef.getVersion()));
		}
		StateEntry stateEntry = incrementalState.objRefToStateMap.get(objRef);
		if (stateEntry == null)
		{
			throw new IllegalStateException();
		}
		writer.writeAttribute("idx", stateEntry.index);
		if (changeContainer instanceof DeleteContainer)
		{
			writer.writeEndElement();
			return;
		}
		ICreateOrUpdateContainer createOrUpdate = (ICreateOrUpdateContainer) changeContainer;
		writePUIs(createOrUpdate.getFullPUIs(), writer);
		writeRUIs(createOrUpdate.getFullRUIs(), writer, incrementalState);
		writer.writeCloseElement("Entity");
	}

	protected void writeRUIs(IRelationUpdateItem[] fullRUIs, IWriter writer, IncrementalMergeState state)
	{
		if (fullRUIs == null)
		{
			return;
		}
		for (IRelationUpdateItem rui : fullRUIs)
		{
			writeRUI(rui, writer, state);
		}
	}

	protected void writeRUI(IRelationUpdateItem rui, IWriter writer, IncrementalMergeState state)
	{
		if (rui == null)
		{
			return;
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		IObjRef[] removedORIs = rui.getRemovedORIs();

		if (addedORIs != null)
		{
			writer.writeStartElement(rui.getMemberName());
			writer.writeAttribute("add", addedORIs.length);
			writer.writeStartElementEnd();
			writeObjRefs(addedORIs, writer, state);
			writer.writeCloseElement(rui.getMemberName());
		}
		if (removedORIs != null)
		{
			writer.writeStartElement(rui.getMemberName());
			writer.writeAttribute("remove", removedORIs.length);
			writer.writeStartElementEnd();
			writeObjRefs(removedORIs, writer, state);
			writer.writeCloseElement(rui.getMemberName());
		}
	}

	protected void writeObjRefs(IObjRef[] objRefs, IWriter writer, IncrementalMergeState incrementalState)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IObjRef[] clone = Arrays.copyOf(objRefs, objRefs.length);
		Arrays.sort(clone, incrementalState.objRefComparator);
		for (IObjRef item : clone)
		{
			writer.writeStartElement("Entity");
			StateEntry stateEntry = incrementalState.objRefToStateMap.get(item);
			if (stateEntry != null)
			{
				writer.writeAttribute("idx", stateEntry.index);
				writer.writeEndElement();
				continue;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(item.getRealType());
			writer.writeAttribute("type", metaData.getEntityType().getName());
			PrimitiveMember idMember = metaData.getIdMemberByIdIndex(item.getIdNameIndex());
			writer.writeAttribute(idMember.getName(), conversionHelper.convertValueToType(CharSequence.class, item.getId()));
			if (item.getVersion() != null)
			{
				writer.writeAttribute("version", conversionHelper.convertValueToType(CharSequence.class, item.getVersion()));
			}
			writer.writeEndElement();
		}
	}

	protected void writePUIs(IPrimitiveUpdateItem[] fullPUIs, IWriter writer)
	{
		if (fullPUIs == null)
		{
			return;
		}
		for (IPrimitiveUpdateItem pui : fullPUIs)
		{
			writePUI(pui, writer);
		}
	}

	protected void writePUI(IPrimitiveUpdateItem pui, IWriter writer)
	{
		if (pui == null)
		{
			return;
		}
		Object newValue = pui.getNewValue();
		writer.writeStartElement(pui.getMemberName());

		CharSequence sValue = conversionHelper.convertValueToType(CharSequence.class, newValue);

		writer.writeAttribute("value", sValue == null ? "null" : sValue.length() > 256 ? "[[skipped " + sValue.length() + " chars]]" : sValue);
		writer.writeEndElement();
	}
}
