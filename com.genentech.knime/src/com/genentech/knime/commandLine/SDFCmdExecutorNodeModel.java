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

import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

/**
 * This is the model implementation of SDFCmdExecutor. 
 * Compile and execute the unix command
 * 
 * @author Man-Ling Lee, Genentech
 */
public class SDFCmdExecutorNodeModel extends AbstractCommandNodeModel {

   private final static String UNIX_DFT = "unixPipe";
   private String m_outUnixCmdName = UNIX_DFT;   

   /**
    * Constructor for the node model.
    */
   protected SDFCmdExecutorNodeModel() {
       super( new PortType[] { SDFCmdPortObject.TYPE },
              new PortType[] { BufferedDataTable.TYPE } );
   }

   /** {@inheritDoc} */
   @Override
   protected PortObject[] execute(final PortObject[] inData,
         final ExecutionContext exec) throws Exception {
       SDFCmdPortObject object = (SDFCmdPortObject) inData[0];
       SDFCmdPortObjectSpec spec = object.getSpec();
       pushFlowVariable(spec);
       if (spec.getSSHConfiguration().isExecuteSSH()) {
           return new PortObject[]{object.getTable()};
       }
       File tmpFile = File.createTempFile("SDFCmd_", ".sdf");
       SSHExecutionResult sshRes = runSSHExecute(spec, exec, tmpFile);

       BufferedDataTable outTable = SDFCmdPortObject.getTable(sshRes.getStdOut());
       sshRes.getStdOut().delete();
       tmpFile.delete();
       return new PortObject[] {outTable};
   }

   /** {@inheritDoc} */
   @Override
   protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
         throws InvalidSettingsException {
       SDFCmdPortObjectSpec spec = (SDFCmdPortObjectSpec) inSpecs[0];
       pushFlowVariable(spec);
       return new PortObjectSpec[]{null};
   }
   
   private void pushFlowVariable(final SDFCmdPortObjectSpec spec) {
       String unixCmdText = spec.getCommandObject().getCSHPipe(true);
       pushFlowVariableString(m_outUnixCmdName, unixCmdText);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void saveSettingsTo(final NodeSettingsWO settings) {
      settings.addString(SDFCmdExecutorNodeDialog.UNIX_KEY, m_outUnixCmdName);
   }
   
   /**
    * after validatedSettings returns without exception.
    */
   @Override
   protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      m_outUnixCmdName = settings.getString(SDFCmdExecutorNodeDialog.UNIX_KEY, UNIX_DFT);
   }

   /**
    * while configure dialog is closed
    */
   @Override
   protected void validateSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      // no op
   }
}
