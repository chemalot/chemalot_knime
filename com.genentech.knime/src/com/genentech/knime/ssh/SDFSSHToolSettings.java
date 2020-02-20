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
 *   14.10.2008 (ohl): created
 *   8/2012 AG modified form KNIME sources
 */
package com.genentech.knime.ssh;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.genentech.knime.Settings;

/**
 * SSH Settings for SDF SSH Node. 
 * 
 * Mostly copied from the implementation
 * provided by the Knime Remote Execution Node.
 * 
 * @author ohl, University of Konstanz, AG Genentech SSF
 */
public class SDFSSHToolSettings extends TABSSHToolSettings{

   public static final String DEFAULT_MYSUB_OPTS = Settings.getMysubOptions();
   private static final String CFG_STRUCT_COL = "structColumn";

   private String m_StructColumn;

   /**
    * Default constructor with default settings, possibly invalid settings.
    */
   public SDFSSHToolSettings() {
      super(DEFAULT_MYSUB_OPTS);
      m_StructColumn = "";
   }

   /**
    * Creates a new settings object with values from the object passed.
    * 
    * @param settings
    *           object with the new values to set
    * @param specs 
    * @throws InvalidSettingsException
    *            if settings object is invalid
    */
   public SDFSSHToolSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      loadSettings(settings);
   }
   
   
   public void loadSettings(final NodeSettingsRO settings)
      throws InvalidSettingsException {
      super.loadSettings(settings);
      m_StructColumn = settings.getString(CFG_STRUCT_COL);
   }

   public void validateSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      super.validateSettings(settings);
      settings.getString(CFG_STRUCT_COL);
   }

   public void save(final NodeSettingsWO settings) {
      super.save(settings);
      settings.addString(CFG_STRUCT_COL, m_StructColumn);
   }

   public void setStructColumn(final String structCol) {
      m_StructColumn = structCol;
   }

   public String getStructColumn() {
      return m_StructColumn;
   }
}
