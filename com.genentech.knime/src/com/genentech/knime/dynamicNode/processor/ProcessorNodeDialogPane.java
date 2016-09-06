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
package com.genentech.knime.dynamicNode.processor;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.dynamicNode.CmdConfiguration;
import com.genentech.knime.dynamicNode.CmdConfigurationPanel;

/**
 * {@link NodeDialogPane} for command line nodes with one input and one output port.
 * @author albertgo
 *
 */
public class ProcessorNodeDialogPane extends NodeDialogPane {

    private final CMDProgramDefinition m_cmdProgramDefinition;
    private final CmdConfigurationPanel m_cmdConfigurationPanel;

	/**
	 * The constructor of this class with no arguments and a very long java doc
	 * comment.
	 */
	public ProcessorNodeDialogPane(final CMDProgramDefinition programDefinition) {
	    m_cmdProgramDefinition = programDefinition;
	    m_cmdConfigurationPanel = new CmdConfigurationPanel(programDefinition, this);
	    addTab("Command Configuration", m_cmdConfigurationPanel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
	    NodeSettingsWO cmdSettings = settings.addNodeSettings(ProcessorCmdSdfNodeModel.CMD_CONFIGURATION_NAME);
	    CmdConfiguration cmdConfiguraton = new CmdConfiguration(m_cmdProgramDefinition);
	    m_cmdConfigurationPanel.saveSettings(cmdConfiguraton);
	    cmdConfiguraton.save(cmdSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
        CmdConfiguration cmdConfiguraton = new CmdConfiguration(m_cmdProgramDefinition);
        NodeSettingsRO cmdSettings;
        try {
            cmdSettings = settings.getNodeSettings(ProcessorCmdSdfNodeModel.CMD_CONFIGURATION_NAME);
        } catch (InvalidSettingsException e) {
            cmdSettings = new NodeSettings("empty");
        }
        cmdConfiguraton.loadInDialog(cmdSettings);
        m_cmdConfigurationPanel.loadSettings(cmdConfiguraton);

	}
}
