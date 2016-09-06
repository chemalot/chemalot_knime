/*
    The chemalot-knime package provides a framework to execute commandline
    programs that read and wrie SDF files on a remote host from the KNIME
    graphical pipelining platform. 
    Copyright (C) 2016 Genentech Inc.

    This file is part of chemalot-knime.

    chemalot-knime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    chemalot-knime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with chemalot-knime.  If not, see <http://www.gnu.org/licenses/>.

 *
 * History
 *   05.06.2009 (ohl): created
 *   2012 08 AG meodified from Knime source
 */
package com.genentech.knime.ssh;

import javax.swing.Box;
import javax.swing.border.Border;

import org.knime.chem.types.CtabValue;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;

/**
 * SSH Settings Panel.
 * 
 * Mostly copied from the implementation
 * provided by the Knime Remote Execution Node.
 * 
 * @author ohl, KNIME.com, Zurich, Switzerland, AG Genentech
 */
public class SDFSSHToolSettingsPanel extends TABSSHToolSettingsPanel {

   private static final long serialVersionUID = 1L;
   
   @SuppressWarnings("unchecked")
   private final ColumnSelectionComboxBox m_structCol =
      new ColumnSelectionComboxBox((Border)null, SdfValue.class,
              MolValue.class, CtabValue.class);


    /**
     * Creates a new tab.
     */
    SDFSSHToolSettingsPanel(final NodeDialogPane parent) {
    	super(parent);
        add(Box.createVerticalStrut(5));
        add(m_structCol);
        add(Box.createHorizontalGlue());
    }

    /**
     * Called by the parent to load new settings into the tab.
     *
     * @param settings the new settings to take over
    * @param specs 
     */
    @Override
    void loadSettings(final TABSSHToolSettings settings, DataTableSpec specs) 
       throws NotConfigurableException {
       super.loadSettings(settings, specs); 
       if (specs != null) {
            m_structCol.update(specs, ((SDFSSHToolSettings)settings).getStructColumn());
       }
    }

    /**
     * Called by the parent to get current values saved into the settings
     * object.
     *
     * @param settings the object to write the currently entered values into
     */
    void saveSettings(final TABSSHToolSettings settings)
            throws InvalidSettingsException {
       super.saveSettings(settings);
       
       ((SDFSSHToolSettings)settings)
          .setStructColumn(m_structCol.getSelectedColumn());
    }

}
