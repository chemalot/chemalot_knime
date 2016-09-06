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
package com.genentech.knime.dynamicNode.generator;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.SSHConfiguration;
import com.genentech.knime.dynamicNode.CmdConfiguration;
import com.genentech.knime.dynamicNode.CmdConfigurationPanel;

/**
 * {@link NodeDialogPane} for command line nodes with one output port.
 * @author albertgo
 *
 */
public class GeneratorNodeDialogPane extends NodeDialogPane {

    private final CMDProgramDefinition m_cmdProgramDefinition;
    private final CmdConfigurationPanel m_cmdConfigurationPanel;
	private final GeneratorSSHSettingsPanel m_sshConfigurationPanel;

	/**
	 * The constructor of this class with no arguments and a very long java doc
	 * comment.
	 */
	public GeneratorNodeDialogPane(final CMDProgramDefinition programDefinition) {
	    m_cmdProgramDefinition = programDefinition;
	    m_cmdConfigurationPanel = new CmdConfigurationPanel(programDefinition, this);
	    m_sshConfigurationPanel = new GeneratorSSHSettingsPanel(this);
	    addTab("Command Configuration", m_cmdConfigurationPanel);
		addTab("SSH Connection", m_sshConfigurationPanel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
	    NodeSettingsWO cmdSettings = settings.addNodeSettings(GeneratorCmdSdfNodeModel.CMD_CONFIGURATION_NAME);
	    CmdConfiguration cmdConfiguraton = new CmdConfiguration(m_cmdProgramDefinition);
	    m_cmdConfigurationPanel.saveSettings(cmdConfiguraton);
	    cmdConfiguraton.save(cmdSettings);


	    NodeSettingsWO sshSettings = settings.addNodeSettings(GeneratorCmdSdfNodeModel.SSH_CONFIGURATION_NAME);
	    SSHConfiguration sshConfiguration = m_sshConfigurationPanel.saveSettings();
	    sshConfiguration.save(sshSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {
        CmdConfiguration cmdConfiguraton = new CmdConfiguration(m_cmdProgramDefinition);
        NodeSettingsRO cmdSettings;
        try {
            cmdSettings = settings.getNodeSettings(GeneratorCmdSdfNodeModel.CMD_CONFIGURATION_NAME);
        } catch (InvalidSettingsException e) {
            cmdSettings = new NodeSettings("empty");
        }
        cmdConfiguraton.loadInDialog(cmdSettings);
        m_cmdConfigurationPanel.loadSettings(cmdConfiguraton);

        NodeSettingsRO sshSettings;
        try {
            sshSettings = settings.getNodeSettings(GeneratorCmdSdfNodeModel.SSH_CONFIGURATION_NAME);
        } catch (InvalidSettingsException e) {
            sshSettings = new NodeSettings("empty");
        }
        SSHConfiguration sshConfiguration = SSHConfiguration.loadFromDialog(sshSettings);
        m_sshConfigurationPanel.loadSettings(sshConfiguration);
	}
}
