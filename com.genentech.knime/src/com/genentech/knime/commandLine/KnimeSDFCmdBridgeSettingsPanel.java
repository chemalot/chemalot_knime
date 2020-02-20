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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.knime.chem.types.CtabValue;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.workflow.FlowVariable;

import com.genentech.knime.dynamicNode.GNEFlowVariableModelButton;

/**
 * Settings Panel for Knime To SDF Node.
 * 
 * @author albertgo
 *
 */
public class KnimeSDFCmdBridgeSettingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private final ColumnSelectionComboxBox m_structCol =
       new ColumnSelectionComboxBox((Border)null, SdfValue.class,
               MolValue.class, CtabValue.class);

    private final JTextField m_lExchangeDir = new JTextField(25);
    private final JTextField m_rExchangeDir = new JTextField(25);
    private final JTextField m_mysubOptions = new JTextField(40);

    /**
     * Creates a new tab.
     */
    public KnimeSDFCmdBridgeSettingsPanel(final NodeDialogPane parent) {
        
        m_structCol.setMaximumSize(m_structCol.getPreferredSize());
        m_lExchangeDir.setMaximumSize(m_lExchangeDir.getPreferredSize());
        m_rExchangeDir.setMaximumSize(m_rExchangeDir.getPreferredSize());
        m_mysubOptions.setMaximumSize(m_mysubOptions.getPreferredSize());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Dimension five10 = new Dimension(5,25);
        /*
         * Bordered box for connection controls
         */
        Box optBox = Box.createVerticalBox();
        optBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "KNIME To SDF Command Line Options:"));

        Box tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));// horizontalStrut would expand
        tmpBox.add(new JLabel("Field containing molecule Data:"));
        tmpBox.add(Box.createRigidArea(five10));
        tmpBox.add(m_structCol);
        tmpBox.add(Box.createHorizontalGlue());

        optBox.add(tmpBox);

        //////////////////////////////////////////////////////
        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));// horizontalStrut would expand
        tmpBox.add(new JLabel("Local Exchange Directory:"));
        tmpBox.add(Box.createRigidArea(five10));
        tmpBox.add(m_lExchangeDir);
        final FlowVariableModel fvmLocal = parent.createFlowVariableModel(
        		new String[]{KnimeSDFCMDBridgeNodeModel.CMD_KNIME_BRIDGE_SET, 
        				     KnimeSDFCMDBridgeSettings.CFG_LOCAL_EX_DIR}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        GNEFlowVariableModelButton localSettings = new GNEFlowVariableModelButton(parent, fvmLocal) {
        	@Override
        	public void setTextField(String s) {
        		m_lExchangeDir.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_lExchangeDir.setEditable(bool);
        	}
        };
        tmpBox.add(localSettings);
        tmpBox.add(Box.createHorizontalGlue());
        optBox.add(tmpBox);

        //////////////////////////////////////////////////////
        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));// horizontalStrut would expand
        tmpBox.add(new JLabel("Remote Exchange Directory:"));
        tmpBox.add(Box.createRigidArea(five10));
        tmpBox.add(m_rExchangeDir);
        m_rExchangeDir.setToolTipText(
           "Path to the same network directory as the 'Local Exchange Directory' from remote host.");
        final FlowVariableModel fvmRemote = parent.createFlowVariableModel(
        		new String[]{KnimeSDFCMDBridgeNodeModel.CMD_KNIME_BRIDGE_SET, 
        				     KnimeSDFCMDBridgeSettings.CFG_REMOTE_EX_DIR}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        GNEFlowVariableModelButton remoteSettings = new GNEFlowVariableModelButton(parent, fvmRemote) {
        	@Override
        	public void setTextField(String s) {
        		m_rExchangeDir.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_rExchangeDir.setEditable(bool);
        	}
        };
        tmpBox.add(remoteSettings);
        tmpBox.add(Box.createHorizontalGlue());
        optBox.add(tmpBox);
        
        //////////////////////////////////////////////////////
        tmpBox = Box.createHorizontalBox();
        tmpBox.add(Box.createRigidArea(five10));// horizontalStrut would expand
        tmpBox.add(new JLabel("mysub options:"));
        tmpBox.add(Box.createRigidArea(five10));
        tmpBox.add(m_mysubOptions);
        m_mysubOptions.setToolTipText(
           "Path to the same network directory as the 'Local Exchange Directory' from remote host.");
        final FlowVariableModel fvmMySub = parent.createFlowVariableModel(
        		new String[]{KnimeSDFCMDBridgeNodeModel.CMD_KNIME_BRIDGE_SET, 
        				KnimeSDFCMDBridgeSettings.CFG_MYSUB_OPTS}, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        GNEFlowVariableModelButton mysubSettings = new GNEFlowVariableModelButton(parent, fvmMySub) {
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
        tmpBox.add(Box.createHorizontalGlue());
        optBox.add(tmpBox);
        
        // create the panel
        add(optBox);
    }

    
    /**
     * Called by the parent to load new settings into the tab.
     *
     * @param configuration the new settings to take over
     */
    public void loadSettings(KnimeSDFCMDBridgeSettings setting, DataTableSpec inPortspecs) 
            throws NotConfigurableException {
        m_structCol.update(inPortspecs, setting.getStructColumn());
        m_lExchangeDir.setText(setting.getLocalExchangeDir());
        m_rExchangeDir.setText(setting.getRemoteExchangeDir());
        m_mysubOptions.setText(setting.getMysubOptions());
     }

    /**
     * Called by the parent to get current values saved into the settings
     * object.
     *
     * @param settings the object to write the currently entered values into
     */
    public KnimeSDFCMDBridgeSettings saveSettings() {
        return new KnimeSDFCMDBridgeSettings(m_structCol.getSelectedColumn(),
           m_lExchangeDir.getText(), m_rExchangeDir.getText(), m_mysubOptions.getText());
    }
}
