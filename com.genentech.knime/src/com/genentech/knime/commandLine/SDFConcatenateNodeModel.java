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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.FileUtil;

import com.genentech.knime.Settings;

/**
 * This is the model implementation of "SDF Concatenate" node. 
 * 
 * @author Thomas Gabriel, KNIME.com AG, Zurich
 */
public class SDFConcatenateNodeModel extends AbstractCommandNodeModel {
    
   public static final String KEY_MYSUBOPTS = "mysubOpts";
   public static final String MYSUBOPTS_DFT = Settings.getMysubOptions();
   private String  m_mysubOptions = MYSUBOPTS_DFT;


/**
    * Constructor for the node model.
    */
   protected SDFConcatenateNodeModel() {
       super( new PortType[] { SDFCmdPortObject.TYPE, 
               SDFCmdPortObject.TYPE_OPTIONAL, SDFCmdPortObject.TYPE_OPTIONAL, 
               SDFCmdPortObject.TYPE_OPTIONAL },
              new PortType[] { SDFCmdPortObject.TYPE } );
   }

   /** {@inheritDoc} */
   @Override
   protected PortObject[] execute(final PortObject[] inPorts,
         final ExecutionContext exec) throws Exception {
       SDFCmdPortObjectSpec spec = ((SDFCmdPortObject) inPorts[0]).getSpec();
       final File outFile;
       if (spec.getSSHConfiguration().isExecuteSSH()) {
           outFile = File.createTempFile("SDFCmd_", ".sdf");
           FileOutputStream out = new FileOutputStream(outFile, true);
           for (int i = 0; i < inPorts.length; i++) {
               
               PortObject po = inPorts[i];
               
               if (po != null) {
                   SDFCmdPortObject port = (SDFCmdPortObject) po;
                   final File sdfFile;
                   
                   if (port.getSpec().getSSHConfiguration().isExecuteSSH()) {
                       sdfFile = port.getSDFile();
                   
                   } else {
                       File tmpFile = File.createTempFile("SDFCmd_", ".sdf");
                       SSHExecutionResult sshRes = runSSHExecute(port.getSpec(), exec, tmpFile);
                       sdfFile = sshRes.getStdOut();
                   }
                   FileInputStream in = new FileInputStream(sdfFile);
                   FileUtil.copy(in, out);
                   
                   sdfFile.delete();
               }
           }
       } else {
           outFile = null;
       }
       
       // collect active in ports so that we can pass them to the out port
       // for the pipe compiler
       SDFCmdPortObjectSpec[] sdfSpecs = new SDFCmdPortObjectSpec[inPorts.length];
       for (int i = 0; i < inPorts.length; i++) {
           PortObject po = inPorts[i];
           if (po != null) {
               SDFCmdPortObject port = (SDFCmdPortObject) po;
               sdfSpecs[i] = port.getSpec();
           }
       }
       
       SDFCmdPortObjectSpec outSpec = createOutSpec(sdfSpecs);
       return new PortObject[]{new SDFCmdPortObject(outSpec, outFile)};
   }

   /** {@inheritDoc} */
   @Override
   protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
         throws InvalidSettingsException {
       SDFCmdPortObjectSpec[] sdfSpecs = 
           new SDFCmdPortObjectSpec[inSpecs.length];
       for (int i = 0; i < sdfSpecs.length; i++) {
           sdfSpecs[i] = (SDFCmdPortObjectSpec) inSpecs[i];
       }
       return new PortObjectSpec[]{createOutSpec(sdfSpecs)};
   }
   
   private SDFCmdPortObjectSpec createOutSpec(
           final SDFCmdPortObjectSpec[] specs) {
       SSHConfiguration sshConfig = specs[0].getSSHConfiguration();
       List<CommandObject> commands = new ArrayList<CommandObject>();
       for (SDFCmdPortObjectSpec spec : specs) {
           if (spec != null) {
               commands.add(spec.getCommandObject());
           }
       }
       CommandObject command = CommandList.SDF_CONCATENATER.createCommandObject(
                                                                   commands, "", m_mysubOptions);
       SDFCmdPortObjectSpec outSpec = new SDFCmdPortObjectSpec(
               command, sshConfig);
       return outSpec;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void saveSettingsTo(final NodeSettingsWO settings) {
      settings.addString(KEY_MYSUBOPTS, m_mysubOptions);
   }

   /**
    * after validatedSettings returns without exception.
    */
   @Override
   protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
         throws InvalidSettingsException {
       m_mysubOptions = settings.getString(KEY_MYSUBOPTS, MYSUBOPTS_DFT);
   
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
