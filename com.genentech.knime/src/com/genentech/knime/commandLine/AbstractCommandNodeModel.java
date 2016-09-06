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

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.util.FileUtil;

/**
 * Abstract class for a KNIME NodeModel providing genral functionalty to execute
 * command line programs via SSH.
 *  
 * @author albertgo
 *
 */
public abstract class AbstractCommandNodeModel extends NodeModel {
    protected static final NodeLogger LOGGER = NodeLogger.getLogger(AbstractCommandNodeModel.class);

    private File m_errorOutFile = null;
    
    public AbstractCommandNodeModel(final PortType[] inPorts,
            final PortType[] outPorts) {
        super(inPorts, outPorts);
    }
    
    /**
     * Execute the command in spec.getCommandObject and return File containing sdtOut.
     * 
     * The stdErr is written into m_errorOutFile.
     * @param tmpInFile file object containing input streamed to stdin of command, may be null.
     */
    protected SSHExecutionResult runSSHExecute(final SDFCmdPortObjectSpec spec, final ExecutionContext exec,
            final File tmpInFile) throws Exception {
        Collection<FlowVariable> vars = getAvailableInputFlowVariables().values();
        SSHExecutionResult sshRes = SSHExecutionHelper.execute(spec, vars, exec, tmpInFile);
        if( m_errorOutFile != null ) 
            m_errorOutFile.delete();
        m_errorOutFile = sshRes.getStdErr();

        if( sshRes.getStatus() != 0 ) {
            String msg = String.format(
                    "SSH command returned status %d. Check error output for error messages.", 
                    sshRes.getStatus()); 
            this.setWarningMessage(msg);
        }
        
        return sshRes;
    }


    @Override
    protected void reset() {
        if(m_errorOutFile != null) 
            m_errorOutFile.delete();
        m_errorOutFile = null;
    }
    

    /**
     * Read the stderr produced by the remote program and return it as a string.
     */
    public String getErrorOutput() {
        String error = null;
        if (m_errorOutFile == null) {
            error = "SSH execution in each genentech node is not enabled. Check first pipe node in this section";
        } else {
            try {
                FileInputStream fis = new FileInputStream(m_errorOutFile);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(fis));
                StringBuilder buf = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    buf.append(line);
                    buf.append("\n");
                }
                error = buf.toString();
                reader.close();
                fis.close();
                
            } catch (Exception e) {
                error = "Error while parsing error output \""
                        + m_errorOutFile.getAbsolutePath() + "\", reason:\n" 
                        + e.getMessage(); 
            }
        }
        return error;
    }
    
    private static final String KEY_ERROR_FILE = "command_error_file.txt";
    
    /**
     * Copy internal copy of error output file to tmp file and store reference in
     * list of error files for display in node view.
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        File errorFile = new File(nodeInternDir, KEY_ERROR_FILE);
        if (errorFile.exists()) {
            if( m_errorOutFile != null ) 
                m_errorOutFile.delete();
            File errorOutFile = File.createTempFile("CmdError_", ".txt");
            FileUtil.copy(errorFile, errorOutFile);
            m_errorOutFile = errorOutFile;
        }
    }


    /**
     * Persist tmp error file to node internal copy.
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        if (m_errorOutFile != null) {
            File errorFile = new File(nodeInternDir, KEY_ERROR_FILE);
            FileUtil.copy(m_errorOutFile, errorFile);
        }
    }
    
    
    @Override
    public void finalize(){
        if( m_errorOutFile != null )
            m_errorOutFile.delete();
    }
}
