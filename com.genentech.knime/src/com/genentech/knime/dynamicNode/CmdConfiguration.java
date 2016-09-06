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


 * History
 *   Sep 11, 2012 (wiswedel): created
 */
package com.genentech.knime.dynamicNode;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.genentech.knime.commandLine.CMDProgramDefinition;

/**
 * Stores settings for command line option of command line nodes.
 * 
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
public final class CmdConfiguration {

    private final CMDProgramDefinition m_programDefinition;
    private String m_userOptions;

    /**
     *  */
    public CmdConfiguration(final CMDProgramDefinition programDefinition) {
        m_programDefinition = programDefinition;
    }

    /** @return the userOptions */
    public String getUserOptions() {
        return m_userOptions;
    }
    
    /** @return the program definition */
    public CMDProgramDefinition getProgramDefinition() {
        return m_programDefinition;
    }
    
    /** @param userOptions the userOptions to set */
    public void setUserOptions(final String userOptions) {
        m_userOptions = userOptions;
    }

    public void loadInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_userOptions = settings.getString("userOptions");
    }

    public void loadInDialog(final NodeSettingsRO settings) {
        m_userOptions = settings.getString("userOptions",
                m_programDefinition.getDefaultOpts());
    }

    public void save(final NodeSettingsWO settings) {
        settings.addString("userOptions", m_userOptions);
    }

}
