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
package com.genentech.knime.dynamicNode.consumer;

import java.io.File;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.NodeMessage;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.SDFCmdPortObject;
import com.genentech.knime.commandLine.SDFCmdPortObjectSpec;
import com.genentech.knime.commandLine.SDFCmdRemoteWriterNodeModel;
import com.genentech.knime.commandLine.SSHExecutionResult;
import com.genentech.knime.dynamicNode.AbstractCmdSdfNodeModel;

/**
 * {@link NodeMessage} for command line nodes with one input port.
 * 
 * @author albertgo
 *
 */
public class ConsumerCmdSdfNodeModel extends AbstractCmdSdfNodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(SDFCmdRemoteWriterNodeModel.class);

    // example value: the models count variable filled from the dialog
    // and used in the models execution method. The default components of the
    // dialog work with "SettingsModels".
    private final SettingsModelString m_outUnixCmdName = createUnixCommandVar();
    private final SettingsModelBoolean m_doNotExecute = createDoNotExecteVar();
    private String m_fullCommandLine;

    /**
     * @param programDefinition */
    public ConsumerCmdSdfNodeModel(final CMDProgramDefinition programDefinition) {
        super(programDefinition);
    }


    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
          SDFCmdPortObject port = (SDFCmdPortObject) inData[0];
          SDFCmdPortObjectSpec spec = port.getSpec();
          SDFCmdPortObjectSpec outSpec = new SDFCmdPortObjectSpec(spec, createCommandObject());
          String m_fullCommandLine = spec.getCommandObject().getCSHPipe(true);
          pushFlowVariableString(m_outUnixCmdName.getStringValue(), m_fullCommandLine);
          pushFlowVariable(outSpec);
          
          if( ! m_doNotExecute.getBooleanValue() ) {
              SSHExecutionResult sshRes = null;
              File tmpFile = null;
              RuntimeException mainErr = null;
              try{
                  if (spec.getSSHConfiguration().isExecuteSSH()) {
                      sshRes = runSSHExecute(outSpec, exec, port.getSDFile());
                  } else {
                      tmpFile  = File.createTempFile("SDFCmd_", ".sdf");
                      sshRes = runSSHExecute(outSpec, exec, tmpFile);
                  }
              } catch( RuntimeException e ) {
                  mainErr = e;
              } finally {
                  try {
                      if( sshRes != null ) { 
                         File out = sshRes.getStdOut();
                         if( out != null ) out.delete();
                      }
                      if( tmpFile != null ) tmpFile.delete();
                  } catch (Exception e) {
                      if( mainErr == null ) throw e;
                      LOGGER.error("Error in finally igonred:", e);
                      throw mainErr;
                  }
              }
          }
          return null;
      }

    
    
    static SettingsModelString createUnixCommandVar() {
        return new SettingsModelString("outUnixCmdVarTag", "unixPipe");
    }

    static SettingsModelString createRemoteFilenameVar() {
        return new SettingsModelString("outUnixFileTag", "outFile.sdf");
    }

    static SettingsModelBoolean createDoNotExecteVar() {
        return new SettingsModelBoolean("doNotExecuteTag", false);
    }
    
    private void pushFlowVariable(final SDFCmdPortObjectSpec spec) {
        m_fullCommandLine = spec.getCommandObject().getCSHPipe(true);
        pushFlowVariableString(m_outUnixCmdName.getStringValue(), m_fullCommandLine);
    }
}
