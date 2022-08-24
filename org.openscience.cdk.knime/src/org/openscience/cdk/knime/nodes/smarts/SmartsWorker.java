/*
 * Copyright (c) 2014, Stephan Beisken (sbeisken@gmail.com). All rights reserved.
 * 
 * This file is part of the KNIME CDK plugin.
 * 
 * The KNIME CDK plugin is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The KNIME CDK plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with the plugin. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openscience.cdk.knime.nodes.smarts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.knime.base.data.replace.ReplacedColumnsDataRow;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.IntCell.IntCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.MultiThreadWorker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.knime.type.CDKCell3;
import org.openscience.cdk.knime.type.CDKValue;

public class SmartsWorker extends MultiThreadWorker<DataRow, DataRow>
{

	private final static NodeLogger LOGGER = NodeLogger.getLogger(SmartsWorker.class);

	private final ExecutionContext exec;
	private final int columnIndex;
	private final double max;
	private final BufferedDataContainer[] bdc;

	private final boolean count;
	private final boolean matchedPositions;

	private final MultipleSmartsMatcher smarts;
	private final Set<Long> matchedRows;

	public SmartsWorker(final int maxQueueSize, final int maxActiveInstanceSize, final int columnIndex, final long max,
			final List<String> smarts, final boolean count, final boolean matchedPositions, final ExecutionContext exec,
			final BufferedDataContainer[] bdc)
	{

		super(maxQueueSize, maxActiveInstanceSize);
		this.exec = exec;
		this.bdc = bdc;
		this.count = count;
		this.max = max;
		this.smarts = new MultipleSmartsMatcher(smarts);
		this.columnIndex = columnIndex;
		this.matchedRows = Collections.synchronizedSet(new HashSet<Long>());
		this.matchedPositions = matchedPositions;
	}

	@Override
	protected DataRow compute(DataRow row, long index) throws Exception
	{

		DataCell outCell;
		List<DataCell> uniqueCounts = new ArrayList<>();
		DataRow countRow = row;
		if (row.getCell(columnIndex).isMissing() || (((AdapterValue) row.getCell(columnIndex)).getAdapterError(CDKValue.class) != null))
		{
			outCell = DataType.getMissingCell();
		} else
		{
			CDKValue cdkCell = ((AdapterValue) row.getCell(columnIndex)).getAdapter(CDKValue.class);
			IAtomContainer m = cdkCell.getAtomContainer();

			try
			{
				
				if(count || matchedPositions) 
				{
					List<Mappings> mappings = smarts.getMappings(m);
					
					if(mappings.stream().filter(mapping -> mapping.atLeast(1)).findAny().isPresent()) 
					{
						matchedRows.add(index);
						
						for(Mappings mapping : mappings)
						{
							uniqueCounts.add(IntCellFactory.create(mapping.countUnique()));
						}
						
						if(matchedPositions)
						{
							Set<Integer> atoms = new HashSet<>();
							Set<Integer> bonds = new HashSet<>();

							for (Mappings map : mappings)
							{
								for (Map<IAtom, IAtom> current : map.toAtomMap())
								{
									for (IAtom atom : current.values())
									{
										atoms.add(Integer.parseInt(atom.getID()));
									}
								}

								for (Map<IBond, IBond> current : map.toBondMap())
								{
									for (IBond bond : current.values())
									{								
										bonds.add(m.getBondNumber(bond));
									}
								}

							}

							countRow = new AppendedColumnRow(row, CollectionCellFactory.createListCell(uniqueCounts),
									CollectionCellFactory.createListCell(toCells(atoms)),
									CollectionCellFactory.createListCell(toCells(bonds)));
						}
						else
						{
							countRow = new AppendedColumnRow(row, CollectionCellFactory.createListCell(uniqueCounts));
						}
					}
					
				} else if (smarts.matches(m)) 
				{
					matchedRows.add(index);
				}
				
			} catch (ThreadDeath d)
			{
				LOGGER.debug("SMARTS Query failed for row \"" + row.getKey() + "\"");
				throw d;
			} catch (Throwable t)
			{
				LOGGER.error(t.getMessage(), t);
			}

			outCell = CDKCell3.createCDKCell(m);
		}

		return new ReplacedColumnsDataRow(countRow, outCell, columnIndex);
	}

	private List<DataCell> toCells(Collection<Integer> values)
	{
		return values.stream().sorted().map(value -> IntCellFactory.create(value)).collect(Collectors.toList());
	}
	
	@Override
	protected void processFinished(ComputationTask task)
			throws ExecutionException, CancellationException, InterruptedException
	{

		DataRow append = task.get();
		if (matchedRows.contains(task.getIndex()))
		{
			bdc[0].addRowToTable(append);
		} else
		{
			bdc[1].addRowToTable(append);
		}

		exec.setProgress(this.getFinishedCount() / max, this.getFinishedCount() + " (active/submitted: "
				+ this.getActiveCount() + "/" + (this.getSubmittedCount() - this.getFinishedCount()) + ")");

		try
		{
			exec.checkCanceled();
		} catch (CanceledExecutionException cee)
		{
			throw new CancellationException();
		}
	}
}
