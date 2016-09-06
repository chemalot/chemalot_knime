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
package com.genentech.knime.commandLine;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.KnimeEncryption;

import com.genentech.knime.Settings;
import com.jcraft.jsch.UserInfo;



/**
 * Container with ssh configuration for the command line nodes
 * 
 * @author Man-Ling Lee, Genentech
 */
public class SSHConfiguration {

    public static final int DEFAULTSshPort = 22;
    private static final int DEFAULTSshTimeoutSec = Settings.SSHTimeout;
    private static final String DEFAULTSshHost = Settings.SSHRemoteHost;
    private static final String DEFAULTRunMode = "prd";
    private final String m_remoteHost;
    private final int m_portNumber;
    private final String m_user;
    private final String m_encryptPassword;
    private final String m_encryptKeyPassphrase;
    private final int m_timeoutSec;
    private final String m_workDirectory;
    private final String m_runMode;
    private final String m_initScriptName;
    
    /** executeInEachNode */
    private final boolean m_executeSSH;
    
    /** File to write error logs if requested */
    private final String m_errLogFile;
    

    public SSHConfiguration( String remoteHost, int portNumber, String user, 
            String encryptPassword, String encryptKeyPassphrase, int timeoutSec,
            String workDirectory, String runMode, String errLogFile, boolean executeSSH )
    {
        m_remoteHost = remoteHost;
        m_portNumber = portNumber;
        m_user = user;
        m_encryptPassword = encryptPassword;
        m_encryptKeyPassphrase = encryptKeyPassphrase;
        m_timeoutSec = timeoutSec;
        m_workDirectory = workDirectory;
        m_runMode = runMode;
        m_errLogFile = errLogFile;
        m_executeSSH = executeSSH;
        m_initScriptName = Settings.SSHInitFileTemplate.replaceAll("\\$mode", runMode);
    }
    
    public String getRemoteHost()
    {   return m_remoteHost; }
    
    public int getPortNumber()
    {   return m_portNumber; }
    
    public String getUser()
    {   return m_user; }
    
    public String getEncryptPassword()
    {   return m_encryptPassword; }
    
    public String getEncryptKeyPassPhrase()
    {   return m_encryptKeyPassphrase; }
    
    /** in milliseconds */
    public int getTimeoutSec()
    {   return m_timeoutSec; }
    
    public int getTimeoutUSec()
    {   return m_timeoutSec * 1000; }
    
    public String getWorkDirectory()
    {   return m_workDirectory; }
    
    /** dev or prd */
    public String getRunMode()
    {   return m_runMode; }
    
    public String getErrorLogFile()
    {   return m_errLogFile; }
    
    /** @return true if the SSH command should be executed during execute (executeInEachNode)*/
    public boolean isExecuteSSH() {
        return m_executeSSH;
    }
    
    public String getInitScriptName()
    {   return m_initScriptName; }
    
    public UserInfo getSSHUserInfo()
    {   return new SettingsUserInfo();
    }
    
    public static SSHConfiguration loadFromModel(final NodeSettingsRO settings) 
            throws InvalidSettingsException {
        String remoteHost = settings.getString("remote_host");
        int portNumber = settings.getInt("port-number");
        String user = settings.getString("user");
        String encryptPassword = settings.getString("password");
        String encryptKeyPassphrase = settings.getString("passphrase");
        int timeout = settings.getInt("timeout");
        String workDirectory = settings.getString("working-dir");
        String runMode = settings.getString("run-mode");
        String m_errLogFile = settings.getString("m_errLogFile", "");
        boolean executeSSH = settings.getBoolean("executeInEachNode", true);
        return new SSHConfiguration(remoteHost, portNumber, user, encryptPassword, 
                encryptKeyPassphrase, timeout, workDirectory, runMode, m_errLogFile, executeSSH);
    }

    public static SSHConfiguration loadFromDialog(final NodeSettingsRO settings) {
        String remoteHost = settings.getString("remote_host", DEFAULTSshHost);
        int portNumber = settings.getInt("port-number", DEFAULTSshPort);
        String user = settings.getString("user", "");
        String encryptPassword = settings.getString("password", "");
        String encryptKeyPassphrase = settings.getString("passphrase", "");
        int timeout = settings.getInt("timeout", DEFAULTSshTimeoutSec);
        String workDirectory = settings.getString("working-dir", "");
        String runMode = settings.getString("run-mode", DEFAULTRunMode);
        String m_errLogFile = settings.getString("m_errLogFile", "");
        boolean executeSSH = settings.getBoolean("executeInEachNode", true);        
        return new SSHConfiguration(remoteHost, portNumber, user, encryptPassword, 
                encryptKeyPassphrase, timeout, workDirectory, runMode, m_errLogFile, executeSSH);
    }
    
    public void save(final NodeSettingsWO settings) {
        settings.addString("remote_host", getRemoteHost());
        settings.addInt("port-number", getPortNumber());
        settings.addString("user", getUser());
        settings.addString("password", getEncryptPassword());
        settings.addString("passphrase", getEncryptKeyPassPhrase());
        settings.addInt("timeout", getTimeoutSec());
        settings.addString("working-dir", getWorkDirectory());
        settings.addString("run-mode", getRunMode());
        settings.addString("m_errLogFile", getErrorLogFile());
        settings.addBoolean("executeInEachNode", isExecuteSSH());
    }

    private class SettingsUserInfo implements UserInfo {

        /** {@inheritDoc} */
        @Override
        public String getPassphrase() {
           String encryptPassphrase = SSHConfiguration.this.getEncryptKeyPassPhrase();
           if (encryptPassphrase == null || encryptPassphrase.length() == 0) {
              return null;
           }
           try {
              return KnimeEncryption.decrypt(encryptPassphrase);
           } catch (Exception e) {
              throw new RuntimeException("Unable to decrypt password: "
                    + e.getMessage(), e);
           }
        }

        /** {@inheritDoc} */
        @Override
        public String getPassword() {
           String encryptPasswd = SSHConfiguration.this.getEncryptPassword();
           if (encryptPasswd == null || encryptPasswd.length() == 0) {
              return null;
           }
           try {
              return KnimeEncryption.decrypt(encryptPasswd);
           } catch (Exception e) {
              throw new RuntimeException("Unable to decrypt password: "
                    + e.getMessage(), e);
           }
        }

        /** {@inheritDoc} */
        @Override
        public boolean promptPassphrase(final String arg0) {
           return true;
        }

        /** {@inheritDoc} */
        @Override
        public boolean promptPassword(final String arg0) {
           return true;
        }

        /** {@inheritDoc} */
        @Override
        public boolean promptYesNo(final String arg0) {
           return false;
        }

        /** {@inheritDoc} */
        @Override
        public void showMessage(final String arg0) {
           // empty
        }
     }
}
