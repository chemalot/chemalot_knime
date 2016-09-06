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
package com.genentech.knime.dynamicNode.generator;

import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.workflow.FlowVariable;

import com.genentech.knime.commandLine.SSHConfiguration;
import com.genentech.knime.commandLine.SSHExecutionHelper;
import com.genentech.knime.dynamicNode.GNEFlowVariableModelButton;
import com.jcraft.jsch.Session;

/**
 * {@link Panel} for SSH settings for command line nodes with one output port.
 * 
 * @author albertgo Genentech
 */
public class GeneratorSSHSettingsPanel extends JPanel {

//    private static final NodeLogger LOGGER =
//            NodeLogger.getLogger(GeneratorSSHSettingsPanel.class);

    private static final long serialVersionUID = 1L;

    // button
    private final JTextField m_host = new JTextField(30);

    private final JTextField m_port = new JTextField(5);

    private final JTextField m_timeout = new JTextField(9);

    private final JComboBox<String> m_mode    = new JComboBox<String>(new String[] {"dev", "prd"});
    
    // button
    private final JTextField m_workDir = new JTextField(30);

    // button
    private final JTextField m_errorLogFile = new JTextField(30);

    private final JCheckBox m_executeSSH = new JCheckBox("Execute in each node");
    
    /**
     * Creates a new tab.
     */
    public GeneratorSSHSettingsPanel(final NodeDialogPane parent) {
        m_host.setMaximumSize( m_host.getPreferredSize() );
        m_port.setMaximumSize( m_port.getPreferredSize() );
        m_timeout.setMaximumSize( m_timeout.getPreferredSize() );
        m_mode.setMaximumSize( m_mode.getPreferredSize() );
        m_workDir.setMaximumSize( m_workDir.getPreferredSize() );
        m_errorLogFile.setMaximumSize(  m_errorLogFile.getPreferredSize() );
        

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
        		new String[]{"sshConfiguration", "remote_host"}, FlowVariable.Type.STRING);
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

        hostBox.add(Box.createHorizontalStrut(15));
        hostBox.add(new JLabel("Timeout:"));
        hostBox.add(Box.createHorizontalStrut(3));
        m_timeout.setToolTipText("In seconds");
        hostBox.add(m_timeout);

        JButton checkButton = new JButton("Check");
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                checkConnection();
            }
        });
        hostBox.add(Box.createHorizontalStrut(8));
        hostBox.add(checkButton);

        connectBox.add(hostBox);


        connectBox.add(Box.createVerticalStrut(8));
        Box tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createHorizontalStrut(5));
        tmpBox.add(new JLabel("Please make sure the hostname appears in "
                + "the list of known hosts in the \"SSH2\" preference page"));
        tmpBox.add(Box.createHorizontalGlue());
        connectBox.add(tmpBox);
        // create the panel
        connectBox.add(Box.createGlue());
        add(connectBox);
        
        
        Box optBox = Box.createVerticalBox();
        optBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Pipe Settings:"));
        
        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createHorizontalStrut(5));
        tmpBox.add(new JLabel("Mode: "));
        tmpBox.add(m_mode);
        tmpBox.add(Box.createHorizontalStrut(10));
        
        tmpBox.add(new JLabel("Remote Directory: "));
        tmpBox.add(m_workDir);
        final FlowVariableModel fvmWorkDir = parent.createFlowVariableModel(
        		new String[]{"sshConfiguration", "working-dir"}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton workdirSettings = new GNEFlowVariableModelButton(parent, fvmWorkDir) {
        	@Override
        	public void setTextField(String s) {
        		m_workDir.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_workDir.setEditable(bool);
        	}
        };
        tmpBox.add(workdirSettings);
        tmpBox.add(Box.createGlue());
        optBox.add(tmpBox);

        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createHorizontalStrut(5));
        JLabel lbl = new JLabel("Error Log File: ");
        lbl.setToolTipText("Specify a full path on the local host.");
        tmpBox.add(lbl);
        tmpBox.add(m_errorLogFile);
        final FlowVariableModel fvmError = parent.createFlowVariableModel(
        		new String[]{"sshConfiguration", "m_errLogFile"}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
		final GNEFlowVariableModelButton errorSettings = new GNEFlowVariableModelButton(parent, fvmError) {
        	@Override
        	public void setTextField(String s) {
        		m_errorLogFile.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_errorLogFile.setEditable(bool);
        	}
        };
        tmpBox.add(errorSettings);
        tmpBox.add(Box.createGlue());
        optBox.add(tmpBox);

        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createHorizontalStrut(8));
        tmpBox.add(m_executeSSH);
        tmpBox.add(Box.createGlue());
        optBox.add(tmpBox);

        optBox.add(Box.createGlue());
        
        add(Box.createVerticalStrut(5));
        add(optBox);
        add(Box.createGlue());
    }

    /**
     * Called by the parent to load new settings into the tab.
     *
     * @param settings the new settings to take over
    * @param specs
     */
    public void loadSettings(final SSHConfiguration settings) throws NotConfigurableException {

        m_host.setText(settings.getRemoteHost());

        if (settings.getPortNumber() >= 0) {
            m_port.setText("" + settings.getPortNumber());
        } else {
            m_port.setText("");
        }
        if (settings.getTimeoutSec() > 0) {
            m_timeout.setText("" + settings.getTimeoutSec());
        } else {
            m_timeout.setText("");
        }
        
        m_workDir.setText(settings.getWorkDirectory());
        m_errorLogFile.setText(settings.getErrorLogFile());
        m_mode.setSelectedItem(settings.getRunMode());
        m_executeSSH.setSelected(settings.isExecuteSSH());
     }

    /**
     * Called by the parent to get current values saved into the settings
     * object.
     *
     * @param settings the object to write the currently entered values into
     */
    public SSHConfiguration saveSettings() throws InvalidSettingsException {
   
        String host = m_host.getText().trim();
        
        int portNr = -1;    // will create error in validateSettings
        try {
            String portnumber = m_port.getText().trim();
            if (!portnumber.isEmpty()) {
                portNr = Integer.parseInt(portnumber);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidSettingsException(
                    "Invalid port number (please enter a number).");
        }

        int timeout = -1;
        try {
            String stimeout = m_timeout.getText().trim();
            if (!stimeout.isEmpty()) {
                timeout = Integer.parseInt(stimeout);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidSettingsException(
                    "Invalid timeout (please enter a number).");
        }
        
        String mode = (String) m_mode.getSelectedItem();
        String workDir = m_workDir.getText().trim();
        String errorLogFile = m_errorLogFile.getText().trim();
        
        boolean executeSSH = m_executeSSH.isSelected();
        
        return new SSHConfiguration(host, portNr, "", "", "", timeout, 
                workDir, mode, errorLogFile, executeSSH);
    }

    private void checkConnection() {
        Session con = null;
        try {
            SSHConfiguration s = saveSettings();
            con = SSHExecutionHelper.getConnectedSession(s);
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
        } finally
        {   if( con != null ) {
                try {
                    con.disconnect();
                    con = null;
                } catch( Error e ) {
                    System.err.println(e.getMessage()); // avoid hiding other exceptions
                }
            }
        }
    }
}
