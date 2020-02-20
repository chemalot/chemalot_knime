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

 * History
 *   05.06.2009 (ohl): created
 *   2012 08 AG meodified from Knime source
 */
package com.genentech.knime.dynamicNode;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.workflow.FlowVariable;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.KnimeSDFCMDBridgeNodeModel;
import com.genentech.knime.commandLine.KnimeSDFCMDBridgeSettings;

/**
 * {@link JPanel} for entering command line options by users of command line nodes.
 *
 * @author albertgo Genentech
 */
public class CmdConfigurationPanel extends JPanel {

//    private static final NodeLogger LOGGER =
//            NodeLogger.getLogger(CmdConfigurationPanel.class);

    private static final long serialVersionUID = 1L;

    private final JTextArea m_userOptions  = new JTextArea(3,50);
    private final JTextField m_mysubOptions = new JTextField(40);

    /**
     * Creates a new tab.
     */
    public CmdConfigurationPanel(final CMDProgramDefinition pDefinition, final NodeDialogPane parent) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Dimension five10 = new Dimension(5,25);
        /*
         * Bordered box for connection controls
         */
        Box optBox = Box.createVerticalBox();
        optBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), pDefinition.getLabel() + ":"));

        Box tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));// horizontalStrut would expand
        tmpBox.add(new JLabel("Fixed IO options: "));
        tmpBox.add(Box.createRigidArea(five10));

        JTextField ioFld = new JTextField(20);
        ioFld.setMaximumSize( ioFld.getPreferredSize());
        ioFld.setEditable(false);
        ioFld.setText(pDefinition.getIOOptions());
        tmpBox.add(ioFld);
        tmpBox.add(Box.createHorizontalGlue());
        optBox.add(tmpBox);

        //////////////////////////////////////////////////////////////////
        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));
        tmpBox.add(new JLabel("Additional Command Line Options:"));
        tmpBox.add(Box.createHorizontalGlue());
        optBox.add(tmpBox);
        
        tmpBox = Box.createHorizontalBox();
        m_userOptions.setBorder(BorderFactory.createEtchedBorder());
        m_userOptions.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tmpBox.add(new JScrollPane(m_userOptions));

        final FlowVariableModel fvm = parent.createFlowVariableModel(
        		new String[]{"cmdConfiguration", "userOptions"}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        GNEFlowVariableModelButton userSettings = new GNEFlowVariableModelButton(parent, fvm) {
        	@Override
        	public void setTextField(String s) {
        		m_userOptions.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_userOptions.setEditable(bool);
        	}
        };
        tmpBox.add(userSettings);
        optBox.add(tmpBox);
        
        //////////////////////////////////////////////////////////////////
        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));
        tmpBox.add(new JLabel("mysub options:"));
        tmpBox.add(Box.createHorizontalGlue());
        optBox.add(tmpBox);
        
        tmpBox = Box.createHorizontalBox();
        m_mysubOptions.setBorder(BorderFactory.createEtchedBorder());
        m_mysubOptions.setAlignmentX(Component.RIGHT_ALIGNMENT);
        m_mysubOptions.setMaximumSize( m_mysubOptions.getPreferredSize());
        tmpBox.add(new JScrollPane(m_mysubOptions));

        final FlowVariableModel fvmms = parent.createFlowVariableModel(
        		new String[]{"cmdConfiguration", "mysubOptions"}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        GNEFlowVariableModelButton mysubSettings = new GNEFlowVariableModelButton(parent, fvmms) {
        	@Override
        	public void setTextField(String s) {
        		m_mysubOptions.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_mysubOptions.setEditable(bool);
        	}
        };
        tmpBox.add(mysubSettings);
        optBox.add(tmpBox);
        
        // create the panel
        add(optBox);
    }

    /**
     * Called by the parent to load new settings into the tab.
     *
     * @param configuration the new settings to take over
     */
    public void loadSettings(final CmdConfiguration configuration) {
        m_userOptions.setText(configuration.getUserOptions());
        m_mysubOptions.setText(configuration.getMysubOptions());
     }

    /**
     * Called by the parent to get current values saved into the settings
     * object.
     *
     * @param settings the object to write the currently entered values into
     */
    public void saveSettings(final CmdConfiguration settings) {
        settings.setUserOptions(m_userOptions.getText());
        settings.setMysubOptions(m_mysubOptions.getText());
    }

}
