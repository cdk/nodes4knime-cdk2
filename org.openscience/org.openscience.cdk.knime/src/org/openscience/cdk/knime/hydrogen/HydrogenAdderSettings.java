/* Created on 30.01.2007 15:01:38 by thor
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------- * 
 */
package org.openscience.cdk.knime.hydrogen;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This class holds all settings for the hydrogen adder node.
 * 
 * @author Thorsten Meinl, University of Konstanz
 */
public class HydrogenAdderSettings {
    private String m_molColumnName;

    private boolean m_replaceColumn = true;
    private String m_appendColumnName;

    private boolean m_explicitHydrogens = true;

    /**
     * Returns if explicit hydrogens are added to the molecules or only
     * implicit ones.
     * 
     * @return <code>true</code> if explicit hydrogens should be added,
     *         <code>false</code> if only implicit ones should be added
     */
    public boolean explicitHydrogens() {
        return m_explicitHydrogens;
    }

    /**
     * Sets if explicit hydrogens are added to the molecules or only implicit
     * ones.
     * 
     * @param explicit <code>true</code> if explicit hydrogens should be
     *            added, <code>false</code> if only implicit ones should be
     *            added
     */
    public void explicitHydrogens(final boolean explicit) {
        m_explicitHydrogens = explicit;
    }

    /**
     * Returns the name of the column containing the molecules.
     * 
     * @return the molecules' column name
     */
    public String molColumnName() {
        return m_molColumnName;
    }

    /**
     * Sets the name of the column containing the molecules.
     * 
     * @param colName the molecules' column name
     */
    public void molColumnName(final String colName) {
        m_molColumnName = colName;
    }

    /**
     * Returns if the molecule column should be replaced or if a new column
     * should be appended.
     * 
     * @return <code>true</code> if the column should be replaced,
     *         <code>false</code> if a new column should be appended
     */
    public boolean replaceColumn() {
        return m_replaceColumn;
    }

    /**
     * Sets if the molecule column should be replaced or if a new column should
     * be appended.
     * 
     * @param replace <code>true</code> if the column should be replaced,
     *            <code>false</code> if a new column should be appended
     */
    public void replaceColumn(final boolean replace) {
        m_replaceColumn = replace;
    }

    /**
     * @return the appendColumnName
     */
    public String appendColumnName() {
        return m_appendColumnName;
    }

    /**
     * @param appendColumnName the appendColumnName to set
     */
    public void appendColumnName(final String appendColumnName) {
        m_appendColumnName = appendColumnName;
    }

    /**
     * Saves the settings into the given node settings object.
     * 
     * @param settings a node settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString("molColumn", m_molColumnName);
        settings.addBoolean("replaceColumn", m_replaceColumn);
        settings.addBoolean("explicitHydrogens", m_explicitHydrogens);
        settings.addString("appendColName", appendColumnName());
    }

    /**
     * Loads the settings from the given node settings object.
     * 
     * @param settings a node settings object
     * @throws InvalidSettingsException if not all required settings are
     *             available
     */
    public void loadSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_molColumnName = settings.getString("molColumn");
        m_replaceColumn = settings.getBoolean("replaceColumn");
        // this setting was not available in 1.2.0
        m_appendColumnName = settings.getString("appendColName", 
            m_molColumnName + " (H)");
        m_explicitHydrogens = settings.getBoolean("explicitHydrogens");
    }
}
