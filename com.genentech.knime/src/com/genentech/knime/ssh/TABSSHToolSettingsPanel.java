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
 *   05.06.2009 (ohl): created
 *   2012 08 AG meodified from Knime source
 */
/**
 * {@link JPanel} to query user for settings in Tab SSH Node.
 * 
 * Mostly copied from the implementation
 * provided by the Knime Remote Execution Node.
 * 
 * @author ohl, University of Konstanz, AG Genentech SSF
 */
package com.genentech.knime.ssh;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.util.KnimeEncryption;

import com.genentech.knime.dynamicNode.GNEFlowVariableModelButton;

/**
 *
 * @author ohl, KNIME.com, Zurich, Switzerland, AG Genentech
 */
public class TABSSHToolSettingsPanel extends JPanel {

    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(TABSSHToolSettingsPanel.class);

    private static final long serialVersionUID = 1L;

    // button
    private final JTextField m_host = new JTextField(30);
    private final JTextField m_port = new JTextField(5);
    private final JTextField m_timeout = new JTextField(5);
    private final JTextField m_mysubOpts = new JTextField(35);
    private final JTextField m_directory = new JTextField(25);
    private final JTextField m_user = new JTextField(30);
    private final JPasswordField m_password = new JPasswordField(30);
    private final JPasswordField m_keyPassphrase = new JPasswordField(30);
    private final JTextArea m_command = new JTextArea(3,50);

    private boolean m_passwordChanged = false;
    private boolean m_keyPassphraseChanged = false;

    /**
     * Creates a new tab.
     */
    TABSSHToolSettingsPanel(final NodeDialogPane parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        /*
         * Bordered box for connection controls
         */
        Box connectBox = Box.createVerticalBox();
        connectBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Connect Info:"));

        // Host + Port + Timeout
        Box hostBox = Box.createHorizontalBox();
        hostBox.add(new JLabel("Host:"));
        hostBox.add(Box.createHorizontalStrut(3));
        hostBox.add(m_host);
        m_host.setToolTipText("Enter hostname or IP address");
        final FlowVariableModel fvmHost = parent.createFlowVariableModel(
        		TABSSHToolSettings.CFG_HOST, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton hostSettings = new GNEFlowVariableModelButton(parent, fvmHost) {
        	@Override
        	public void setTextField(String s) {
        		m_host.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_host.setEditable(bool);
        	}
        };
        hostBox.add(hostSettings);
        hostBox.add(Box.createHorizontalStrut(15));
        hostBox.add(new JLabel("Port:"));
        hostBox.add(Box.createHorizontalStrut(3));
        hostBox.add(m_port);
        m_port.setToolTipText("Leave empty for default port #"
                + TABSSHToolSettings.DEFAULT_SSH_PORT);
        hostBox.add(Box.createHorizontalStrut(15));
        hostBox.add(new JLabel("Timeout (sec):"));
        hostBox.add(Box.createHorizontalStrut(3));
        hostBox.add(m_timeout);
        hostBox.add(Box.createHorizontalGlue());

        connectBox.add(Box.createVerticalStrut(5));
        connectBox.add(hostBox);

        // User + password
        Box userBox = Box.createHorizontalBox();
        userBox.add(new JLabel("Username:"));
        userBox.add(Box.createHorizontalStrut(3));
        userBox.add(m_user);
        m_user.setToolTipText("Leave empty for current user");
        final FlowVariableModel fvmUser = parent.createFlowVariableModel(
        		TABSSHToolSettings.CFG_USER, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton userSettings = new GNEFlowVariableModelButton(parent, fvmUser) {
        	@Override
        	public void setTextField(String s) {
        		m_user.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_user.setEditable(bool);
        	}
        };
        userBox.add(userSettings);
        userBox.add(Box.createHorizontalStrut(15));
        userBox.add(new JLabel("Password:"));
        userBox.add(Box.createHorizontalStrut(3));
        userBox.add(m_password);
        m_password.addFocusListener(new FocusAdapter() {
            /** {@inheritDoc} */
            @Override
            public void focusGained(final FocusEvent e) {
                m_password.setText("");
                m_passwordChanged = true;
            }
        });
        m_password.setToolTipText("Leave empty if not needed");
        userBox.add(Box.createHorizontalGlue());

        connectBox.add(Box.createVerticalStrut(5));
        connectBox.add(userBox);

        // passphrase + check button
        Box phraseBox = Box.createHorizontalBox();
        phraseBox.add(new JLabel("Passphrase for private key file:"));
        phraseBox.add(Box.createHorizontalStrut(3));
        phraseBox.add(m_keyPassphrase);
        m_keyPassphrase.addFocusListener(new FocusAdapter() {
            /** {@inheritDoc} */
            @Override
            public void focusGained(final FocusEvent e) {
                m_keyPassphrase.setText("");
                m_keyPassphraseChanged = true;
            }
        });

        m_keyPassphrase.setToolTipText("Leave empty if not used or required");
        phraseBox.add(Box.createHorizontalGlue());

        phraseBox.add(Box.createHorizontalGlue());
        phraseBox.add(Box.createHorizontalGlue());

        JButton checkButton = new JButton("Check Connection");
        checkButton.setPreferredSize(new Dimension(120, 25));
        checkButton.setMaximumSize(new Dimension(120, 25));
        checkButton.setMinimumSize(new Dimension(120, 25));
        checkButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                checkConnection();
            }
        });
        phraseBox.add(checkButton);

        connectBox.add(Box.createVerticalStrut(5));
        connectBox.add(phraseBox);

        connectBox.add(Box.createVerticalStrut(8));
        connectBox.add(new JLabel("Please make sure the hostname appears in "
                + "the list of known hosts in the \"SSH2\" preference page"));
        /*
         * Bordered box containing all remote command controls
         */
        Box commandBox = Box.createVerticalBox();
        commandBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Remote Command:"));

        // command line
        Box cmdBox = Box.createHorizontalBox();
        cmdBox.add(m_command);

        final FlowVariableModel fvmCommand = parent.createFlowVariableModel(
        		TABSSHToolSettings.CFG_COMMAND, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton commandSettings = new GNEFlowVariableModelButton(parent, fvmCommand) {
        	@Override
        	public void setTextField(String s) {
        		m_command.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_command.setEditable(bool);
        	}
        };
        cmdBox.add(commandSettings);

        m_command.setToolTipText("Command plus arguments.\n"+
                                 "Command gets input passed to stdIn outptu is read from sdtOut.");
        cmdBox.add(Box.createHorizontalGlue());

        commandBox.add(Box.createVerticalStrut(5));

        commandBox.add(cmdBox);
        commandBox.add(Box.createVerticalStrut(5));
        commandBox.add(Box.createHorizontalGlue());
        
        
        Box dirBox = Box.createHorizontalBox();
        dirBox.add(new JLabel("Working Directory:"));
        dirBox.add(Box.createHorizontalStrut(3));
        dirBox.add(m_directory);
        m_directory.setToolTipText("Directory to issue command: Default: $HOME)");
        final FlowVariableModel fvmDir = parent.createFlowVariableModel(
        		TABSSHToolSettings.CFG_DIRECTORY, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton dirSettings = new GNEFlowVariableModelButton(parent, fvmDir) {
        	@Override
        	public void setTextField(String s) {
        		m_directory.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_directory.setEditable(bool);
        	}
        };
        dirBox.add(dirSettings);

        Box mysubBox = Box.createHorizontalBox();
        mysubBox.add(new JLabel("mysub options:"));
        mysubBox.add(Box.createHorizontalStrut(3));
        mysubBox.add(m_mysubOpts);
        m_mysubOpts.setToolTipText("Enter options for queuing system");
        final FlowVariableModel fvmMysub = parent.createFlowVariableModel(
        		TABSSHToolSettings.CFG_MYSUB_OPTS, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton mysubSettings = new GNEFlowVariableModelButton(parent, fvmMysub) {
        	@Override
        	public void setTextField(String s) {
        		m_mysubOpts.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_mysubOpts.setEditable(bool);
        	}
        };
        mysubBox.add(mysubSettings);

        ///////////////////////////////////// mysub
        add(Box.createVerticalStrut(5));
        add(connectBox);
        add(Box.createVerticalStrut(10));
        add(commandBox);
        add(Box.createVerticalStrut(5));
        add(dirBox);
        add(Box.createVerticalStrut(5));
        add(mysubBox);
                
    }

    /**
     * Called by the parent to load new settings into the tab.
     *
     * @param settings the new settings to take over
    * @param specs 
     */
    void loadSettings(final TABSSHToolSettings settings, DataTableSpec specs) 
       throws NotConfigurableException {
        transferSettingsIntoComponents(settings, specs);
    }

    /**
     * Called by the parent to get current values saved into the settings
     * object.
     *
     * @param settings the object to write the currently entered values into
     */
    void saveSettings(final TABSSHToolSettings settings)
            throws InvalidSettingsException {
        transferComponentsValuesIntoSettings(settings);
    }

    /**
     * Transfers the currently entered values from this tab's components into
     * the provided settings object.
     */
    private void transferComponentsValuesIntoSettings(
            final TABSSHToolSettings settings) throws InvalidSettingsException {

        settings.setRemoteHost(m_host.getText().trim());

        try {
            String portnumber = m_port.getText().trim();
            if (!portnumber.isEmpty()) {
                int portNr = Integer.parseInt(portnumber);
                settings.setPortNumber(portNr);
            } else {
                settings.setPortNumber(-1);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidSettingsException(
                    "Invalid port number (please enter a number).");
        }

        try {
            String timeout = m_timeout.getText().trim();
            if (!timeout.isEmpty()) {
                int t = Integer.parseInt(timeout);
                settings.setTimeout(t);
            } else {
                settings.setTimeout(-1);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidSettingsException(
                    "Invalid timeout (please enter a number).");
        }

        settings.setUser(m_user.getText().trim());
        if (!m_passwordChanged) {
            String pass =
                    m_password.getPassword().length == 0 ? null : new String(
                            m_password.getPassword());
            settings.setEncryptPassword(pass);
        } else {
            try {
                char[] pass = m_password.getPassword();
                if (pass == null || pass.length == 0) {
                    settings.setEncryptPassword(null);
                } else {
                    settings.setEncryptPassword(KnimeEncryption.encrypt(pass));
                }
            } catch (Exception e) {
                String msg = "<no details>";
                if (e.getMessage() != null) {
                    msg = e.getMessage();
                }
                LOGGER.error("Encryption of password failed. Not stored! ("
                        + msg + ")", e);
                settings.setEncryptPassword(null);
            }
        }
        if (!m_keyPassphraseChanged) {
            String keyPhrase =
                    m_keyPassphrase.getPassword().length == 0 ? null
                            : new String(m_keyPassphrase.getPassword());
            settings.setEncryptKeyPassphrase(keyPhrase);
        } else {
            try {
                char[] pass = m_keyPassphrase.getPassword();
                if (pass == null || pass.length == 0) {
                    settings.setEncryptKeyPassphrase(null);
                } else {
                    settings.setEncryptKeyPassphrase(KnimeEncryption
                            .encrypt(pass));
                }
            } catch (Exception e) {
                String msg = "<no details>";
                if (e.getMessage() != null) {
                    msg = e.getMessage();
                }
                LOGGER.error("Encryption of passphrase failed. Not stored! ("
                        + msg + ")", e);
                settings.setEncryptKeyPassphrase(null);
            }
        }

        settings.setCommand(m_command.getText());
        settings.setDirectory(m_directory.getText());
        settings.setMysubOptions(m_mysubOpts.getText());
    }

    /**
     * Simply reads all values from the settings object and transfers them into
     * the dialog's components.
     *
     * @param settings the settings values to display
    * @param specs 
    * @throws NotConfigurableException 
     */
    private void transferSettingsIntoComponents(
            final TABSSHToolSettings settings, DataTableSpec specs) 
    throws NotConfigurableException {

        m_host.setText(settings.getRemoteHost());

        if (settings.getPortNumber() >= 0) {
            m_port.setText("" + settings.getPortNumber());
        } else {
            m_port.setText("");
        }
        if (settings.getTimeout() > 0) {
            m_timeout.setText("" + settings.getTimeout());
        } else {
            m_timeout.setText("");
        }
        m_user.setText(settings.getUser());
        m_password.setText(settings.getEncryptPassword());
        m_passwordChanged = false;
        m_keyPassphrase.setText(settings.getEncryptKeyPassphrase());
        m_keyPassphraseChanged = false;
        m_command.setText(settings.getCommand());
        m_directory.setText(settings.getDirectory());
        m_mysubOpts.setText(settings.getMysubOptions());
     }

    private void checkConnection() {
        TABSSHToolSettings s = new TABSSHToolSettings();
        try {
            transferComponentsValuesIntoSettings(s);
            SSHUtil.getConnectedSession(s);
            JOptionPane.showMessageDialog(this, "Looks good.");
        } catch (InvalidSettingsException ise) {
            JOptionPane.showMessageDialog(this,
                    "Can't connect - invalid settings: " + ise.getMessage());
        } catch (Throwable t) {
            String msg = "<no details>";
            if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                msg = t.getMessage();
            }
            JOptionPane.showMessageDialog(this, "Connection failed:\n" + msg);
        }
    }

}
