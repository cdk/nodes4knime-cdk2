package org.openscience.cdk.knime.nodes.smarts;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.IntCell.IntCellFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.smarts.SmartsPattern;

public class MultipleSmartsMatcher 
{

	private List<SmartsPattern> patterns;
	
	public MultipleSmartsMatcher(List<String> smarts) 
	{
		init(smarts);
	}
	
	
	private void init(List<String> smarts) 
	{
		this.patterns = new ArrayList<>();
		for (String smart : smarts) 
		{
			SmartsPattern pattern = SmartsPattern.create(smart);
			pattern.setPrepare(false);
			patterns.add(pattern);
		}
	}

	/**
	 * Identify it at least one pattern matches
	 * @param container
	 * @return
	 */
	public boolean matches(IAtomContainer container) 
	{
		SmartsPattern.prepare(container);
		
		return patterns.stream().filter(pattern -> pattern.matches(container)).findAny().isPresent();
	}
	
	/**
	 * Get the unique count of the matches for each SMARTS pattern (order preserved)
	 * @param container
	 * @return
	 */
	public List<DataCell> countUnique(IAtomContainer container)
	{
		List<DataCell> cells = new ArrayList<>();
		
		for(SmartsPattern pattern : patterns) 
		{
			Mappings mappings = pattern.matchAll(container);
			
			cells.add(IntCellFactory.create(mappings.countUnique()));
		}
		
		return cells;
	}
	
	/**
	 * Get the mappings for each SMARTS pattern (order preserveD)
	 * @param container
	 * @return
	 */
	public List<Mappings> getMappings(IAtomContainer container)
	{
		List<Mappings> mappings = new ArrayList<>();
		
		for(SmartsPattern pattern : patterns) 
		{
			mappings.add(pattern.matchAll(container));
		}
		
		return mappings;
	}
}
