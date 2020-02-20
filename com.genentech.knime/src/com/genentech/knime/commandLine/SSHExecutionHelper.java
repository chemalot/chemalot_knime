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

 *
 * History
 *   Jul 13, 2009 (ohl): created
 *   Aug 2012 AG modified from Knime sources
 */
package com.genentech.knime.commandLine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.eclipse.jsch.core.IJSchLocation;
import org.eclipse.jsch.core.IJSchService;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.FlowVariable;

import com.genentech.knime.GNENodeActivator;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * Static Helper Methods that use jcraft SSH package to execute a command via SSH.
 * 
 * @author albertgo @ Genentech
 */
public class SSHExecutionHelper {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(SSHExecutionHelper.class);

    private SSHExecutionHelper() {} // only static methods   

    /**
     * Execute the command in spec.getCommandObject.
     * @param nodeName 
     * @param vars list of flow variables to be set as remote environment variables 
     *
     * @return File[2] pointing to files with stdout and stderr of the command execution.
     * @param tmpInFile file object containing input streamed to stdin of command, may be null.
     */
   public static SSHExecutionResult execute(SDFCmdPortObjectSpec pSpec, 
            Collection<FlowVariable> vars, ExecutionContext exec, File tmpInFile) 
   throws Exception {
      SSHConfiguration sshConfig = pSpec.getSSHConfiguration();
      
      Session session = null;
      try {
         session = getConnectedSession(sshConfig);
         
         // create temporary file to store sdf output.
         File tmpOutFile = File.createTempFile("SDFCmdNodeOutputTable", ".sdf");
         OutputStream tmpOutStrm = new BufferedOutputStream(new FileOutputStream(tmpOutFile));
         
         // create temporary file to store stderr.
         File tmpErrFile = File.createTempFile("SDFCmdNodeOutputTable", ".txt");
         OutputStream tmpErrStrm = new BufferedOutputStream(new FileOutputStream(tmpErrFile));

         InputStream tmpInStrm = null; 
         
         LOGGER.debug("Opening Exec channel");
         ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
         try {
        	CommandObject cmdObj = pSpec.getCommandObject();
            String pipeCommand;
            String mysub;
            if( sshConfig.isExecuteSSH() ) {
                pipeCommand = cmdObj.getMyCommandLine();
            	mysub = cmdObj.getMysubOptions();
            }
            else {    
                // TODO I think using getCSHPipe(true) should work and then we can do things like cat <<COMS>tmp$$.grvy\nsdfGroovy.csh -c $$.grvy
                // cf. /gne/home/albertgo/tmp/testTcshBash.pl
                pipeCommand = cmdObj.getCSHPipe(false);
            	mysub = cmdObj.getRootMysubOptions();
            }

            // Prepend "set flowVariableName=flowVariableValue;" strings
            // so that users can use "$flowVarName" in the command line options 
            // to specify the value of a flow variable
            String env = getEnvCommands(vars);
            
            mysub = String.format("mysub.py -interactive -jobName %s %s -- ", 
            		               "knime_" + cmdObj.getProgramDefintion().getLabel().replace(" ", ""),
            		               mysub);
            String cmd = 
                  "source " + sshConfig.getInitScriptName() + "; "
                + env
                + "cd " + sshConfig.getWorkDirectory() + ";" 
                + mysub
                + '\'' + pipeCommand.replace("'", "'\\''") + '\'';
            // 20160111 we should be able to use "'\''" instead of "'\"'\"'"
            // since this results in exponential lengthening of the string it might be worthwhile
            // cf. bsub command line subnode
            cmd = "/bin/tcsh -fc '" + cmd.replace("'", "'\\''") + "'";
            execChannel.setCommand(cmd);
            
            if (tmpInFile != null) {
                tmpInStrm = new BufferedInputStream(new FileInputStream(tmpInFile));
                execChannel.setInputStream(tmpInStrm);
            }
            execChannel.setErrStream(tmpErrStrm);
            execChannel.setOutputStream(tmpOutStrm);
            // once more before take-off
            exec.checkCanceled();
            exec.setMessage("Executing on host " + sshConfig.getRemoteHost());
            LOGGER.info("Executing node via SSH on " + sshConfig.getRemoteHost());
            LOGGER.debug("Executing remotely command: '" + cmd + "'");
            execChannel.connect(sshConfig.getTimeoutUSec());
            exec.setMessage("Waiting for remote command to finish");
            exec.setProgress(pipeCommand);
            while (!execChannel.isClosed()) {
               exec.checkCanceled();
               Thread.sleep(500);
            }
            //LOGGER.debug("SSH execution finished.");
            exec.checkCanceled();
            
            int status = execChannel.getExitStatus();
            LOGGER.debug("Executing remotely command exit status=" + status);
            
            tmpOutStrm.close();
            tmpErrStrm.close();
            
            // if requested write std error do log dir 
            if( sshConfig.getErrorLogFile().length() > 0 ) {
                writeErrorLog(sshConfig, tmpErrFile, vars);
            }
            
            return new SSHExecutionResult(status, tmpOutFile, tmpErrFile);
            
         } finally {
            if (execChannel != null && execChannel.isConnected()) {
               execChannel.disconnect();
            }
            if (tmpInStrm != null) {
                tmpInStrm.close();
            }
            tmpOutStrm.close();
            tmpErrStrm.close();
         }
         

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

	private static String getEnvCommands(Collection<FlowVariable> vars) {
		StringBuilder env = new StringBuilder(vars.size()*20);
		for(FlowVariable var : vars) {
		    String vName = var.getName();
		    
		    // Only flowVarabiles that are valid unix varaible names
		    if( ! vName.matches("[._\\w]+") ) continue;
		    vName = vName.replace('.', '_');
		    
		    // ignore flow variable containing strings with newlines
		    String value = var.getValueAsString().trim();
		    if( value.indexOf('\n') >-1 || value.indexOf('\r') > -1 ) continue;
		    if( value.length() > 500 )
		    {   LOGGER.warn("Flow Variable not passed to ssh (longer than 500): " + vName);
		        continue;
		    }
		    // 20160111 we should be able to use "'\''" instead of "'\"'\"'"
		    // since this results in exponential lengthening of the string it might be worthwhile
		    // cf. bsub command line subnode
		    value = value.replace("'", "'\"'\"'");
		    env.append("setenv ").append(vName).append(" '").append(value).append("';");
		}
		return env.toString();
	}


    /**
     * Append the contents of tmpErrFile to the errorLogFIle specified in sshConfig.
     * 
     *  @param vars flow variables. replace any occurrence of ${varName} in the filename 
     *              by the variable contents.
     */
    private static void writeErrorLog(SSHConfiguration sshConfig, File tmpErrFile, 
                    Collection<FlowVariable> vars) throws IOException {
        String logFName = sshConfig.getErrorLogFile();
        assert logFName != null && logFName.length() > 0;
        
        // replace ${vName} with flow variable value
        for(FlowVariable var : vars) {
            String vName = var.getName();
            if( ! vName.matches("\\w+") ) continue;
            
            // ignore flow variable containing unexpected characters
            String value = var.getValueAsString().trim();
            if( value.matches("[^A-Za-z_0-1/\\:]") ) continue;
            
            logFName = logFName.replace("${"+vName+'}', value);
            logFName = logFName.replace("$"+vName, value);
        }
        
        File logFile = new File(logFName);
        File dir = logFile.getAbsoluteFile().getParentFile();
        if( dir == null )
            throw new IOException("Could not find directory of: " + logFName);
        if( ! dir.exists() ) dir.mkdirs();
        if( ! dir.isDirectory() )
            throw new IOException(dir.getAbsolutePath() + " is not a directory.");
        
        BufferedWriter out = null;
        BufferedReader in = null;
        try {
            out = new BufferedWriter(new FileWriter(logFile,true));
            in = new BufferedReader(new FileReader(tmpErrFile));
            out.newLine();out.newLine();
            out.append(String.format("Node execution completed on: %tF %1$tT ", new Date()));
            String line;
            while((line=in.readLine()) != null)
                out.append(line).append('\n');
        }finally {
           try {    
               if( out != null) out.close();
               if( in  != null) in.close();
           } catch(IOException e)
           {    e.printStackTrace(System.err);
               // just log error on close
           }
        }
    }

   
   public static synchronized Session getConnectedSession(
           final SSHConfiguration configSettings) throws Exception {

       int port = configSettings.getPortNumber();
       if (port < 0) {
           port = SSHConfiguration.DEFAULTSshPort;
       }

       String remoteHost = configSettings.getRemoteHost();
       String user = configSettings.getUser();
       if (user == null || user.trim().isEmpty()) {
           if( remoteHost.contains("@") ) {
               user = remoteHost.substring(0,remoteHost.indexOf('@'));
               remoteHost = remoteHost.substring(remoteHost.indexOf('@')+1);
           } else {
               user = System.getProperty("user.name");
           }
       }
       
       IJSchService service =
           GNENodeActivator.getDefault().getIJSchService();
       IJSchLocation location = service.getLocation(user, remoteHost, port);
       UserInfo userInfo = configSettings.getSSHUserInfo();

       Session session = null;
       int nRetry = 10;
       Exception firstE = null;
       while( nRetry-- > 0 ) {
           try {
               session = service.createSession(location, userInfo);
               session.connect(configSettings.getTimeoutUSec());
               
               return session;
               
           }catch (Exception e) {
               LOGGER.error("Problem creating ssh connection, retrying: " + e.getMessage());
               if( firstE == null ) firstE = e;
               try {
                   if (session != null && session.isConnected()) 
                       session.disconnect();
               } catch (Exception e2) {
                   LOGGER.error("Problem closing ssh connection, retrying: " + e2.getMessage());
               }
           }
       } 
       
       if (firstE.getMessage() != null && !firstE.getMessage().isEmpty()) {
           throw firstE;
       }
       throw new IllegalStateException("Couldn't establish SSH session.", firstE);
   }

}