/* ------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2016
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Jul 13, 2009 (ohl): created
 *   Aug 2012 AG modified from Knime sources
 */
package com.genentech.knime.ssh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.knime.base.node.io.filereader.FileReaderNodeSettings;
import org.knime.base.node.io.filereader.FileTable;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import org.knime.core.node.property.hilite.HiLiteHandler;

import com.genentech.knime.Settings;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

/**
 * Common Methods for the SDFSSHNode and TABSSSHNode.
 * 
 * @author albertgo @ Genentech
 */
public abstract class AbstractSSHNodeModel extends NodeModel {

   private static final NodeLogger LOGGER = NodeLogger
         .getLogger(AbstractSSHNodeModel.class);

   private static final  DataTableSpec STDERRTableSpec = 
      new DataTableSpec("GNE SSH TOOL StdErr", 
         new String[] {"Output"}, new DataType[] {StringCell.TYPE} );


   private TABSSHToolSettings configSettings = createSettings();
   
   // hard coded to prd because SDF/TABSSH nodes do not have dialog
   private static final String CSHSettingsFile = Settings.SSHInitFileTemplate.replaceAll("\\$mode", "prd");

   
   /** Create node with one input and two output ports
     *
     */
   public AbstractSSHNodeModel() {
      super(new PortType[]{BufferedDataTable.TYPE_OPTIONAL}, 
    		new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE});      
   }

  /**
    * Overwrite returning new configSettings object
    */
   public abstract TABSSHToolSettings createSettings();
   
   /**
    * Return configuration settings. Overwrite if customized dialog is used.
    */
   public TABSSHToolSettings getConfigSettings() {
      return configSettings;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void loadInternals(final File nodeInternDir,
         final ExecutionMonitor exec) throws IOException,
         CanceledExecutionException {
      // TODO Auto-generated method stub
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      configSettings.loadSettings(settings);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void reset() {
      // nothing to reset
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void saveInternals(final File nodeInternDir,
         final ExecutionMonitor exec) throws IOException,
         CanceledExecutionException {
      // TODO Auto-generated method stub
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void saveSettingsTo(final NodeSettingsWO settings) {
      configSettings.save(settings);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void validateSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      configSettings.validateSettings(settings);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
         final ExecutionContext exec) throws Exception {

      Session session = null;
      try {
         session = SSHUtil.getConnectedSession(configSettings);
         
         File tmpInFile = null;
         InputStream tmpInStrm = null;
         if (inData[0] != null) {
        	 tmpInFile = writeTMPInputFile(exec, inData[0]);
        	 tmpInFile.deleteOnExit();
        	 tmpInStrm = new BufferedInputStream(new FileInputStream(tmpInFile)); 
         }
         File tmpOutFile = File.createTempFile("ExtSSHNodeOutputTable", getOutFileExtention());
         OutputStream tmpOutStrm = new BufferedOutputStream(new FileOutputStream(tmpOutFile));
         
         File tmpErrFile = File.createTempFile("ExtSSHNodeErrTile", "txt");
         OutputStream tmpErrStrm = new BufferedOutputStream(new FileOutputStream(tmpErrFile));

         
         LOGGER.debug("Opening Exec channel");
         File tempFile = null;
         ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
         try {
            String dir = configSettings.getDirectory().trim();
            if( dir.length() == 0 ) dir = ".";
            dir = String.format("cd \"%s\"", dir);
            
            String cmd = configSettings.getCommand();
            tempFile = File.createTempFile("knimeSSH"+System.currentTimeMillis(), ".csh", 
            		                            new File(Settings.getExchangeLocalDir()));
            
            // using chmod in the command is not an option since the remote user might not have permission
            // the following has no effect when run from windows so it is no use:
            tempFile.setExecutable(true);
            
            FileWriter out = new FileWriter(tempFile);
            out.write(cmd.replaceAll("\n\r", "\n"));
            out.write('\n');
            out.close();
            tempFile.setExecutable(true);
            String remoteFile = Settings.getExchangeRemoteDir() + '/' + tempFile.getName();
            String mysub = String.format("%s;tcsh -fc 'source %s; mysub.py -interactive -jobName knime_%s %s -- tcsh -f %s'", 
		               dir, CSHSettingsFile, 
		               this.getClass().getName().substring(0, 3), 
		               configSettings.getMysubOptions(),
		               remoteFile);
            
            // replace $inFile and $outFile with paths
            execChannel.setCommand(mysub);
            if (tmpInStrm != null) {
            execChannel.setInputStream(tmpInStrm);
            }
            execChannel.setErrStream(tmpErrStrm);
            execChannel.setOutputStream(tmpOutStrm);
            // once more before take-off
            exec.checkCanceled();
            exec.setMessage("Executing on host " + configSettings.getRemoteHost());
            LOGGER.info("Executing node via SSH on "
                  + configSettings.getRemoteHost());
            LOGGER.debug("Executing remotely command: '" + cmd + "'");
            execChannel.connect(configSettings.getTimeoutMilliSec());
            exec.setMessage("Waiting for remote command to finish");
            while (!execChannel.isClosed()) {
               exec.checkCanceled();
               Thread.sleep(500);
            }
            LOGGER.debug("SSH execution finished.");
            exec.checkCanceled();

         } finally {
            if (execChannel != null && execChannel.isConnected()) {
               execChannel.disconnect();
            }
            if (tmpInStrm != null) {
            tmpInStrm.close();
            tmpInFile.delete();
            if( tempFile != null) tempFile.delete();
         }
            tmpOutStrm.close();
         }
         
         return readOutputFiles(exec, tmpOutFile, tmpErrFile);

      } catch (Exception e) {
         if ((!(e instanceof CanceledExecutionException))
               && e.getMessage() != null && !e.getMessage().isEmpty()) {
            LOGGER.error("Job submission failed: " + e.getMessage());
         }
         throw e;

      } finally {
         if (session != null && session.isConnected()) {
            session.disconnect();
         }
      }

   }

   /**
    * Read the input {@see BufferedDataTable} and write it into a tmp file.
    * 
    * The file will be streamed to the ssh command
    */
   private File writeTMPInputFile(final ExecutionContext exec,
         final BufferedDataTable inData) throws IOException,
         CanceledExecutionException {

      File tmpInFile = File.createTempFile("ExtSSHNodeInputTable", getInFileExtention());

      writeTMPInFile(exec, inData, tmpInFile);

      LOGGER.debug("Wrote input table to " + tmpInFile.getAbsolutePath());
      exec.checkCanceled();
      return tmpInFile;
   }

   /**
    * ssh call has resulted in the stdout and stderr to be written into tmp files.
    * 
    * Read the files and create {@see BufferedDataTable}s.
    * @param tmpOutFile file containing the stdout
    * @param tmpErrFile file containing the stderr
    */
   private BufferedDataTable[] readOutputFiles(
         final ExecutionContext exec, File tmpOutFile, File tmpErrFile) 
   throws MalformedURLException, IOException, CanceledExecutionException {
      
      BufferedDataTable[] result = new BufferedDataTable[2];
      exec.setMessage("Analyzing result file...");
      exec.checkCanceled();

      result[0] = parseOutput(exec, tmpOutFile);
      
      exec.setMessage("Reading SdtErr... ");
      FileReaderNodeSettings frns = new FileReaderNodeSettings();
      frns.setDataFileLocationAndUpdateTableName(tmpErrFile.toURI().toURL());
      frns.setFileHasColumnHeaders(false);   frns.setFileHasColumnHeadersUserSet(true);
      frns.setFileHasRowHeaders(false);      frns.setFileHasRowHeadersUserSet(true);
      
      FileTable ft = new FileTable(STDERRTableSpec, frns, exec.createSubExecutionContext(0));
      result[1] = exec.createBufferedDataTables(new DataTable[] { ft }, exec)[0];
      exec.checkCanceled();
   
      tmpOutFile.delete();
      tmpErrFile.delete();
      return result;
   }

   /** return the file extension for the files sent to the ssh program on stdin
    */
   public abstract String getInFileExtention();

   /** return the file extension for the files sent to the ssh program on stdin
    */
   public abstract String getOutFileExtention();

   /** Write data in bufferedDataTable into the temporary so that it can be 
    * sent to the stdin of the ssh command.
    * @param tmpInFile empty file in tmp folder.
    */
   public abstract void writeTMPInFile(ExecutionContext exec, 
         BufferedDataTable bufferedDataTable, File tmpInFile) throws IOException, CanceledExecutionException;

   /** Parse the stdout put of the ssh command and create the {@see BufferedDataTable}
    * which is passed from the first output port.
    * @param tmpOutFile tmp file containing the stdout. 
    * @throws IOException 
    * @throws CanceledExecutionException 
    */
   public abstract BufferedDataTable parseOutput(ExecutionContext exec, File tmpOutFile) throws IOException, CanceledExecutionException;

   /**
    * {@inheritDoc}
    */
   @Override
   protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
         throws InvalidSettingsException {
      String msg = configSettings.getStatusMsg();
      if (msg != null) {
         throw new InvalidSettingsException(msg);
      }
      
      // we never know what is coming back from the external tool
      return new DataTableSpec[] { null,  STDERRTableSpec };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected HiLiteHandler getOutHiLiteHandler(final int outIndex) {
      // hiliting doesn't work through this node.
      return new HiLiteHandler();
   }
}
