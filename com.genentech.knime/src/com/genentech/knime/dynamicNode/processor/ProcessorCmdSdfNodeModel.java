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

import java.io.File;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.CommandObject;
import com.genentech.knime.commandLine.SDFCmdPortObject;
import com.genentech.knime.commandLine.SDFCmdPortObjectSpec;
import com.genentech.knime.commandLine.SSHExecutionResult;
import com.genentech.knime.dynamicNode.AbstractCmdSdfNodeModel;

/**
 * {@link NodeModel} for command line nodes with one input and one output port.
 * 
 * @author albertgo
 *
 */
public class ProcessorCmdSdfNodeModel extends AbstractCmdSdfNodeModel {

    /**
     * @param programDefintion */
    public ProcessorCmdSdfNodeModel(final CMDProgramDefinition programDefintion) {
        super(programDefintion);
    }


    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        SDFCmdPortObjectSpec spec = (SDFCmdPortObjectSpec) inSpecs[0];
        CommandObject command = createCommandObject();
        return new PortObjectSpec[]{new SDFCmdPortObjectSpec(spec, command)};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
            throws Exception {
        SDFCmdPortObject port = (SDFCmdPortObject) inObjects[0];
        SDFCmdPortObjectSpec spec = port.getSpec();
        SDFCmdPortObjectSpec outSpec = new SDFCmdPortObjectSpec(spec,
                createCommandObject());
        File sdfFile = null;
        if (spec.getSSHConfiguration().isExecuteSSH()) {
            
            SSHExecutionResult sshRes = runSSHExecute(outSpec, exec, port.getSDFile());
            sdfFile = sshRes.getStdOut();
        }
        return new PortObject[]{new SDFCmdPortObject(outSpec, sdfFile)};
    }

}
