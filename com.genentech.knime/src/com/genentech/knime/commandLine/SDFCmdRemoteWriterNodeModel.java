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

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.genentech.knime.Settings;

/**
 * This is the model implementation of SDFCmdExecutor. 
 * Compile and execute the unix command
 * 
 * @author Man-Ling Lee, Genentech
 */
public class SDFCmdRemoteWriterNodeModel extends AbstractCommandNodeModel {

   private static final String CFG_FULLCOMMAND_LINE = "fullcommandLine";

   private static final NodeLogger LOGGER = NodeLogger.getLogger(SDFCmdRemoteWriterNodeModel.class);

   static final String KEY_UNIX = "outUnixCmdVarTag";
   static final String UNIX_DFT = "unixPipe";
   private String  m_outUnixCmdName = UNIX_DFT;

   static final String KEY_FILE = "outUnixFileTag";
   static final String FILE_DFT = "outFile.sdf";
   private String  m_outRemoteFileName = FILE_DFT;
   
   static final String KEY_NOTEXEC = "doNotExecuteTag";
   static final boolean NOTEXEC_DFT = false;

   public static final String KEY_MYSUBOPTS = "mysubOpts";
   public static final String MYSUBOPTS_DFT = Settings.getMysubOptions();
   private String  m_mysubOptions = MYSUBOPTS_DFT;
   
   private boolean m_doNotExecute = NOTEXEC_DFT;

   private String m_fullCommandLine;

   /**
    * Constructor for the node model.
    */
   protected SDFCmdRemoteWriterNodeModel() {
       super( new PortType[] { SDFCmdPortObject.TYPE }, new PortType[0] );
   }

   /** {@inheritDoc} */
   @Override
   protected PortObject[] execute(final PortObject[] inData,
         final ExecutionContext exec) throws Exception {
       SDFCmdPortObject port = (SDFCmdPortObject) inData[0];
       SDFCmdPortObjectSpec spec = port.getSpec();
       SDFCmdPortObjectSpec outSpec = new SDFCmdPortObjectSpec(spec, createCommandObject());
       pushFlowVariable(outSpec);
       
       if( ! m_doNotExecute ) {
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
                      if( out != null ) 
                          out.delete();
                   }
                   if( tmpFile != null ) 
                       tmpFile.delete();
               } catch (Exception e) {
                   if( mainErr == null ) throw e;
                   LOGGER.error("Error in finally ignored:", e);
                   throw mainErr;
               }
           }
       }
       return null;
   }

   
   /** {@inheritDoc} */
   @Override
   protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
         throws InvalidSettingsException {
       SDFCmdPortObjectSpec inSpec = (SDFCmdPortObjectSpec) inSpecs[0];
       SDFCmdPortObjectSpec outSpec = new SDFCmdPortObjectSpec(inSpec, createCommandObject());
       pushFlowVariable(outSpec);
       return null;
   }
   
   private final CommandObject createCommandObject() throws InvalidSettingsException {
       return CommandList.SDF_REMOTE_WRITER.createComamndObject(
                                           m_outRemoteFileName, m_mysubOptions);
   }
   
   private void pushFlowVariable(final SDFCmdPortObjectSpec spec) {
       m_fullCommandLine = spec.getCommandObject().getCSHPipe(true);
       pushFlowVariableString(m_outUnixCmdName, m_fullCommandLine);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void saveSettingsTo(final NodeSettingsWO settings) {
      settings.addString(KEY_UNIX, m_outUnixCmdName);
      settings.addString(KEY_FILE, m_outRemoteFileName);
      settings.addString(KEY_MYSUBOPTS, m_mysubOptions);
      settings.addBoolean(KEY_NOTEXEC, m_doNotExecute);
      settings.addString(CFG_FULLCOMMAND_LINE, m_fullCommandLine);
   }

   /**
    * after validatedSettings returns without exception.
    */
   @Override
   protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
         throws InvalidSettingsException {
       m_outUnixCmdName = settings.getString(KEY_UNIX, UNIX_DFT);
       m_outRemoteFileName = settings.getString(KEY_FILE, FILE_DFT);
       m_mysubOptions = settings.getString(KEY_MYSUBOPTS, MYSUBOPTS_DFT);
       m_doNotExecute = settings.getBoolean(KEY_NOTEXEC, NOTEXEC_DFT);
       m_fullCommandLine = settings.getString(CFG_FULLCOMMAND_LINE, "");
   }

   /**
    * while configure dialog is closed
    */
   @Override
   protected void validateSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
	   // no op
   }

   public String getFullCommandLine() {
       return m_fullCommandLine;
   }
}
