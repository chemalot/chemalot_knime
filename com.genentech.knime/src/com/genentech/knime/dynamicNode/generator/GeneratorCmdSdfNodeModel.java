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

import java.io.File;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.CommandObject;
import com.genentech.knime.commandLine.SDFCmdPortObject;
import com.genentech.knime.commandLine.SDFCmdPortObjectSpec;
import com.genentech.knime.commandLine.SSHConfiguration;
import com.genentech.knime.commandLine.SSHExecutionResult;
import com.genentech.knime.dynamicNode.AbstractCmdSdfNodeModel;

/**
 * {@link NodeModel} for command line nodes with one output port.
 * @author albertgo
 *
 */
public class GeneratorCmdSdfNodeModel extends AbstractCmdSdfNodeModel {

    public static final String SSH_CONFIGURATION_NAME = "sshConfiguration";

    private SSHConfiguration m_sshConfiguration;


    /**
     * @param programDefintion */
    public GeneratorCmdSdfNodeModel(final CMDProgramDefinition programDefintion) {
        super(programDefintion);
    }


    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_sshConfiguration == null) {
            throw new InvalidSettingsException("No configuration available");
        }
        CommandObject command = createCommandObject();
        return new PortObjectSpec[]{new SDFCmdPortObjectSpec(command, m_sshConfiguration)};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
            throws Exception {
        CommandObject command = createCommandObject();
        SDFCmdPortObjectSpec spec = new SDFCmdPortObjectSpec( command, m_sshConfiguration);
        final File sdfFile;
        if (spec.getSSHConfiguration().isExecuteSSH()) {
            SSHExecutionResult sshRes = runSSHExecute(spec, exec, null);
            sdfFile = sshRes.getStdOut();
        } else {
            sdfFile = null;
        }
        return new PortObject[]{new SDFCmdPortObject(spec, sdfFile)};
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        NodeSettingsRO sshSettings = settings.getNodeSettings(SSH_CONFIGURATION_NAME);
        SSHConfiguration.loadFromModel(sshSettings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        NodeSettingsRO sshSettings = settings.getNodeSettings(SSH_CONFIGURATION_NAME);
        SSHConfiguration sshConfiguration = SSHConfiguration.loadFromModel(sshSettings);
        m_sshConfiguration = sshConfiguration;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        if (m_sshConfiguration != null) {
            NodeSettingsWO sshSettings = settings.addNodeSettings(SSH_CONFIGURATION_NAME);
            m_sshConfiguration.save(sshSettings);
        }
    }
}
