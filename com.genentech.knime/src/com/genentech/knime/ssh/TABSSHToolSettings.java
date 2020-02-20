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
 *   14.10.2008 (ohl): created
 *   8/2012 AG modified form KNIME sources
 */
/**
 * 
 * Mostly copied from the implementation
 * provided by the Knime Remote Execution Node.
 */
package com.genentech.knime.ssh;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.KnimeEncryption;

import com.genentech.knime.Settings;
import com.jcraft.jsch.UserInfo;

/**
 * Container for settings of Tab SSH Node.
 * 
 * Mostly copied from the implementation
 * provided by the Knime Remote Execution Node.
 * 
 * @author ohl, University of Konstanz, AG Genentech SSF
 */
public class TABSSHToolSettings {

   /** default port. */
   public static final int DEFAULT_SSH_PORT = 22;
   public static final String DEFAULT_MYSUB_OPTS = Settings.getMysubOptions();
	   
   public static final String CFG_HOST = "remoteHost";
   public static final String CFG_PORT = "sshPortNumber";
   public static final String CFG_USER = "userName";
   public static final String CFG_PASSWD = "passwd";
   public static final String CFG_KEY_PASSPHRASE = "keyPassphrase";
   public static final String CFG_TIMEOUT = "TimeoutSec";
   public static final String CFG_COMMAND = "remoteCommand";
   public static final String CFG_MYSUB_OPTS = "mysubOpts";
   public static final String CFG_DIRECTORY = "directory";
   private String m_remoteHost;
   private int m_portNumber;
   private String m_user;
   private String m_encryptPassword;
   private String m_encryptKeyPassphrase;
   private int m_timeout;
   private String m_command;
   private String m_mysubOptions;
   private String m_directory;

   /**
    * Default constructor with default settings, possibly invalid settings.
    */
   public TABSSHToolSettings()
   {   this(DEFAULT_MYSUB_OPTS);
   }
   
   
   TABSSHToolSettings(String defaultMysubOpts) {
      m_remoteHost = "";
      m_user = "";
      m_encryptKeyPassphrase = "";
      m_encryptPassword = "";
      m_timeout = 0;
      m_portNumber = DEFAULT_SSH_PORT;
      m_command = "";
      m_mysubOptions = defaultMysubOpts;
      m_directory = "";
   }

   /**
    * Creates a new settings object with values from the object passed.
    * 
    * @param settings
    *           object with the new values to set
    * @param specs 
    * @throws InvalidSettingsException
    *            if settings object is invalid
    */
   public TABSSHToolSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      loadSettings(settings);
   }
   
   
   public void loadSettings(final NodeSettingsRO settings)
      throws InvalidSettingsException {
      m_remoteHost = settings.getString(CFG_HOST);
      m_portNumber = settings.getInt(CFG_PORT);
      m_timeout = settings.getInt(CFG_TIMEOUT);
      m_user = settings.getString(CFG_USER);
      m_encryptPassword = settings.getString(CFG_PASSWD);
      m_encryptKeyPassphrase = settings.getString(CFG_KEY_PASSPHRASE);
      m_command = settings.getString(CFG_COMMAND);
      m_mysubOptions = settings.getString(CFG_MYSUB_OPTS, Settings.getMysubOptions());
      m_directory = settings.getString(CFG_DIRECTORY, "");
   }

   public void validateSettings(final NodeSettingsRO settings)
         throws InvalidSettingsException {
      settings.getString(CFG_HOST);
      settings.getInt(CFG_PORT);
      settings.getInt(CFG_TIMEOUT);
      settings.getString(CFG_USER);
      settings.getString(CFG_PASSWD);
      settings.getString(CFG_KEY_PASSPHRASE);
      settings.getString(CFG_COMMAND);
      settings.getString(CFG_MYSUB_OPTS, Settings.getMysubOptions());
      settings.getString(CFG_DIRECTORY, "");
   }

   /**
    * Create the JSch User info object that returns password and passphrase.
    * 
    * @return a new user info object.
    */
   public UserInfo createJSchUserInfo() {
      return new SettingsUserInfo();
   }

   /**
    * Saves the current values in to the settings object.
    * 
    * @param settings
    *           the config object to store values in
    */
   public void save(final NodeSettingsWO settings) {
      settings.addString(CFG_HOST, m_remoteHost);
      settings.addInt(CFG_PORT, m_portNumber);
      settings.addInt(CFG_TIMEOUT, m_timeout);
      settings.addString(CFG_USER, m_user);
      settings.addString(CFG_PASSWD, m_encryptPassword);
      settings.addString(CFG_KEY_PASSPHRASE, m_encryptKeyPassphrase);
      settings.addString(CFG_COMMAND, m_command);
      settings.addString(CFG_MYSUB_OPTS, m_mysubOptions);
      settings.addString(CFG_DIRECTORY, m_directory);
   }

   /**
    * Returns null if the settings are valid. Otherwise a user message telling
    * which settings are incorrect and why.
    * 
    * @return an error message if settings are invalid, of null, if everything
    *         is allright.
    */
   public String getStatusMsg() {
      if (m_remoteHost.isEmpty()) {
         return "The remote host must be specified";
      }
      if (m_command.isEmpty()) {
         return "The remote command must be specified.";
      }
      return null;
   }

   /**
    * @param remoteHost the remoteHost to set
    */
   public void setRemoteHost(final String remoteHost) {
      if (remoteHost == null) {
         m_remoteHost = "";
      } else {
         m_remoteHost = remoteHost;
      }
   }

   /**
    * @return the remoteHost
    */
   public String getRemoteHost() {
      return m_remoteHost;
   }

   /**
    * @param user
    *           the user to set
    */
   public void setUser(final String user) {
      if (user == null) {
         m_user = "";
      } else {
         m_user = user;
      }
   }

   /**
    * @return the user
    */
   public String getUser() {
      return m_user;
   }

   /**
    * Gets the specified username with the $user§ and $userhome$ reference
    * replaced.
    * 
    * @return the username with $user$ or $userhome$ replaced
    */
   public String getUserResolved() {
      return replaceUserVariable(m_user);
   }

   /**
    * 
    * @return the encrypted password
    */
   public String getEncryptPassword() {
      return m_encryptPassword;
   }

   /**
    * 
    * @param newEncrPasswd
    *           the new encrypted password
    */
   public void setEncryptPassword(final String newEncrPasswd) {
      m_encryptPassword = newEncrPasswd;
   }

   /**
    * @return the encryptedKeyPassphrase
    */
   public String getEncryptKeyPassphrase() {
      return m_encryptKeyPassphrase;
   }

   /**
    * @param encryptedKeyPassphrase
    *           the encryptedKeyPassphrase to set
    */
   public void setEncryptKeyPassphrase(final String encryptedKeyPassphrase) {
      m_encryptKeyPassphrase = encryptedKeyPassphrase;
   }

   /**
    * @param portNumber
    *           the portNumber to set
    */
   public void setPortNumber(final int portNumber) {
      m_portNumber = portNumber;
   }

   /**
    * @return the portNumber
    */
   public int getPortNumber() {
      return m_portNumber;
   }

   /**
    * @param timeout
    *           the timeout to set in seconds
    */
   public void setTimeout(final int timeout) {
      m_timeout = timeout;
   }

   /**
    * @return the timeout in seconds
    */
   public int getTimeout() {
      return m_timeout;
   }

   /**
    * 
    * @return the timeout in milliseconds (or 0 if none is set or not
    *         representable in an int)
    */
   public int getTimeoutMilliSec() {
      int timeoutSec = getTimeout();
      int timeoutMilliSec = 0; // 0 is no timeout
      if (timeoutSec > 0 && timeoutSec < Integer.MAX_VALUE / 1000) {
         timeoutMilliSec = timeoutSec * 1000;
      }
      return timeoutMilliSec;
   }

   /**
    * @return the command
    */
   public String getCommand() {
      return m_command;
   }

   /**
    * @param command the command line to set
    */
   public void setCommand(final String command) {
      if (command == null) {
         m_command = "";
      } else {
         m_command = command;
      }
   }

   /**
    * @return the command
    */
   public String getDirectory() {
      return m_directory;
   }

   /**
    * @param command the command line to set
    */
   public void setDirectory(final String directory) {
      if (directory == null) {
         m_directory = "";
      } else {
         m_directory = directory;
      }
   }

   public void setMysubOptions(String text) {
	  if (text == null)
	      m_mysubOptions = "";
	  else
    	  m_mysubOptions = text;
	}

   public String getMysubOptions() {
	      return m_mysubOptions;
   }

   /**
    * pattern in the string that will be replaced by the current user name (see
    * {@link #replaceUserVariable(String)}).
    */
   public static final String USER_NAME_PATTERN = "$user$";

   /**
    * Pattern in the string that will be replaced by the current user home
    * directory (see {@link #replaceUserVariable(String)}).
    */
   public static final String USER_HOME_PATTERN = "$userhome$";

   /**
    * both patterns must start with the same character and the one must not be a
    * prefix of the other.
    */
   private static final char FIRST_CHAR = USER_HOME_PATTERN.charAt(0);

   /**
    * 
    * @param value
    *           the string to replace the patterns in.
    * @return either the parameter or a new string with the pattern replaced by
    *         the current user
    */
   public static String replaceUserVariable(final String value) {

      if (value == null || value.isEmpty()) {
         return value;
      }

      // this must use the shortest pattern(!!!)
      int lastPosIdx = value.length() - USER_NAME_PATTERN.length();
      if (lastPosIdx < 0) {
         // the shortest pattern doesn't fit in the value
         return value;
      }

      StringBuilder result = new StringBuilder();
      int lastAddedIdx = -1;
      int firstCharIdx = -1;
      while (true) {
         firstCharIdx = value.indexOf(FIRST_CHAR, firstCharIdx + 1);
         if (firstCharIdx < 0 || firstCharIdx > lastPosIdx) {
            break;
         }

         // check user home
         if (firstCharIdx + USER_HOME_PATTERN.length() <= value.length()
               && value.substring(firstCharIdx,
                     firstCharIdx + USER_HOME_PATTERN.length()).equals(
                     USER_HOME_PATTERN)) {
            // append everything up to the pattern
            result.append(value.substring(lastAddedIdx + 1, firstCharIdx));
            // add the replaced pattern
            result.append(System.getProperty("user.home"));
            lastAddedIdx = firstCharIdx + USER_HOME_PATTERN.length() - 1;
            firstCharIdx += USER_HOME_PATTERN.length() - 1;
            // check username (its the shortest pattern - it must fit)
         } else if (value.substring(firstCharIdx,
               firstCharIdx + USER_NAME_PATTERN.length()).equals(
               USER_NAME_PATTERN)) {
            // append everything up to the pattern
            result.append(value.substring(lastAddedIdx + 1, firstCharIdx));
            // add the replaced pattern
            result.append(System.getProperty("user.name"));
            lastAddedIdx = firstCharIdx + USER_NAME_PATTERN.length() - 1;
            firstCharIdx += USER_NAME_PATTERN.length() - 1;
         }
      }

      if (lastAddedIdx == -1) {
         // nothing added to the result - because we didn't find any pattern
         return value;
      } else {
         result.append(value.substring(lastAddedIdx + 1));
      }

      return result.toString();
   }

   private class SettingsUserInfo implements UserInfo {

      /** {@inheritDoc} */
      @Override
      public String getPassphrase() {
         String encryptPassphrase = TABSSHToolSettings.this
               .getEncryptKeyPassphrase();
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
         String encryptPasswd = TABSSHToolSettings.this.getEncryptPassword();
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
