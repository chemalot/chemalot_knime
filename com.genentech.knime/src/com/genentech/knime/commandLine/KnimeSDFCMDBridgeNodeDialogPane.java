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
package com.genentech.knime.commandLine;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.genentech.knime.dynamicNode.generator.GeneratorCmdSdfNodeModel;
import com.genentech.knime.dynamicNode.generator.GeneratorSSHSettingsPanel;

/**
 * NodeDialog Pane for KnimeToSDF Node.
 * 
 * @author albertgo
 *
 */
public class KnimeSDFCMDBridgeNodeDialogPane extends NodeDialogPane {
// GeneratorNodeDialogPane
    
    private GeneratorSSHSettingsPanel m_sshConfigurationPanel;
    private KnimeSDFCmdBridgeSettingsPanel m_settingsPanel;

    protected KnimeSDFCMDBridgeNodeDialogPane() {
        m_sshConfigurationPanel = new GeneratorSSHSettingsPanel(this);
        m_settingsPanel = new KnimeSDFCmdBridgeSettingsPanel(this);
        addTab("Configuration",  m_settingsPanel);
        addTab("SSH Connection", m_sshConfigurationPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        NodeSettingsWO nodeSettings = settings.addNodeSettings(KnimeSDFCMDBridgeNodeModel.CMD_KNIME_BRIDGE_SET);
        KnimeSDFCMDBridgeSettings kSettings = m_settingsPanel.saveSettings();
        kSettings.save(nodeSettings);

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
        
        NodeSettingsRO nodeSettings;
        try {
            nodeSettings = settings.getNodeSettings(KnimeSDFCMDBridgeNodeModel.CMD_KNIME_BRIDGE_SET);
        } catch (InvalidSettingsException e) {
            nodeSettings = new NodeSettings("empty");
        }
        KnimeSDFCMDBridgeSettings kSettings 
                        = KnimeSDFCMDBridgeSettings.loadFromDialog(nodeSettings);
        m_settingsPanel.loadSettings(kSettings, specs[0]);

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
