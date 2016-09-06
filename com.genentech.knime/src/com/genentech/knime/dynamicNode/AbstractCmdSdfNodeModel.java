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

*/
package com.genentech.knime.dynamicNode;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;

import com.genentech.knime.commandLine.AbstractCommandNodeModel;
import com.genentech.knime.commandLine.CMDPortType;
import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.CommandObject;
import com.genentech.knime.commandLine.SDFCmdPortObject;

/**
 * Abstract {@link NodeModel} for command line nodes.
 * 
 * Provides common implementation across all command line nodes.
 * 
 * @author albertgo
 *
 */
public class AbstractCmdSdfNodeModel extends AbstractCommandNodeModel {
    
    public static final String CMD_CONFIGURATION_NAME = "cmdConfiguration";

    private final CMDProgramDefinition m_programDefintion;

    private CmdConfiguration m_cmdSettings;

    /**
     *  */
    public AbstractCmdSdfNodeModel(final CMDProgramDefinition programDefintion) {
        super(getPortTypes(programDefintion.getInPorts()),
              getPortTypes(programDefintion.getOutPorts()));
        m_programDefintion = programDefintion;
    }

    protected static PortType[] getPortTypes(final CMDPortType[] cmdPortTypes) {
        PortType[] result = new PortType[cmdPortTypes.length];
        for (int i = 0; i < cmdPortTypes.length; i++) {
            switch (cmdPortTypes[i]) {
            case SDF:
                result[i] = SDFCmdPortObject.TYPE;
                break;
            default:
                throw new IllegalStateException(
                        "Port type " + cmdPortTypes[i] + " not implemented");
            }
        }
        return result;
    }
    
    protected final CommandObject createCommandObject() throws InvalidSettingsException {
        CmdConfiguration cmdConfiguration = getCmdConfiguration();
        if (cmdConfiguration == null) {
            throw new InvalidSettingsException("No configuration available");
        }
        return getCMDProgramDefintion().createComamndObject(getUserOptions());
    }

    /** @return the cmdSettings */
    protected final CmdConfiguration getCmdConfiguration() {
        return m_cmdSettings;
    }

    /** @return the programDefintion */
    protected final CMDProgramDefinition getCMDProgramDefintion() {
        return m_programDefintion;
    }
    
    /** @return the user options */
    protected final String getUserOptions() {
        return m_cmdSettings.getUserOptions();
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        NodeSettingsRO childSettings = settings.getNodeSettings(CMD_CONFIGURATION_NAME);
        new CmdConfiguration(m_programDefintion).loadInModel(childSettings);
    }


    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        NodeSettingsRO childSettings = settings.getNodeSettings(CMD_CONFIGURATION_NAME);
        CmdConfiguration cmdSettings = new CmdConfiguration(m_programDefintion);
        cmdSettings.loadInModel(childSettings);
        m_cmdSettings = cmdSettings;
    }


    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_cmdSettings != null) {
            NodeSettingsWO childSettings = settings.addNodeSettings(CMD_CONFIGURATION_NAME);
            m_cmdSettings.save(childSettings);
        }
    }

}
