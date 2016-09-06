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

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

import com.genentech.knime.dynamicNode.GNEFlowVariableModelButton;

/**
 * <code>NodeDialog</code> for the SDFCmdExecutor Node.
 * 
 * @author Man-Ling Lee, Genentech
 */
public class SDFCmdRemoteWriterNodeDialog extends NodeDialogPane {
	
	final JTextField m_remoteFile = new JTextField(25);
	final JTextField m_unixCommand = new JTextField(25);
	final JCheckBox  m_createCommand = new JCheckBox();
	/**
     * New pane for configuring LigandESDFCmdRemoteWriter node.
     */
    protected SDFCmdRemoteWriterNodeDialog() {
        final FlowVariableModel fileModel = createFlowVariableModel(SDFCmdRemoteWriterNodeModel.KEY_FILE, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton fileButton = new GNEFlowVariableModelButton(this, fileModel) {
        	@Override
        	public void setTextField(String s) {
        		m_remoteFile.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_remoteFile.setEditable(bool);
        	}
        };
        final JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("Remote Filename: "));
        filePanel.add(m_remoteFile);
        filePanel.add(fileButton);
        
        final FlowVariableModel unixModel = createFlowVariableModel(SDFCmdRemoteWriterNodeModel.KEY_UNIX, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton unixButton = new GNEFlowVariableModelButton(this, unixModel) {
        	@Override
        	public void setTextField(String s) {
        		m_unixCommand.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_unixCommand.setEditable(bool);
        	}
        };
        final JPanel unixPanel = new JPanel();
        unixPanel.add(new JLabel("Unix Command Flow Variable:"));
        unixPanel.add(m_unixCommand);
        unixPanel.add(unixButton);
        
        final JPanel noexecPanel = new JPanel();
        noexecPanel.add(new JLabel("Only Create Command (do not execute):"));
        noexecPanel.add(m_createCommand);
        
        final JPanel p = new JPanel(new GridLayout(3, 1));
        p.add(filePanel);
        p.add(unixPanel);
        p.add(noexecPanel);
        addTab("Options", p);
    }
    
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) 
    		throws NotConfigurableException {
        m_unixCommand.setText(settings.getString(SDFCmdRemoteWriterNodeModel.KEY_UNIX, 
        		SDFCmdRemoteWriterNodeModel.UNIX_DFT));
        m_remoteFile.setText(settings.getString(SDFCmdRemoteWriterNodeModel.KEY_FILE, 
        		SDFCmdRemoteWriterNodeModel.FILE_DFT));
        m_createCommand.setSelected(settings.getBoolean(SDFCmdRemoteWriterNodeModel.KEY_NOTEXEC, 
        		SDFCmdRemoteWriterNodeModel.NOTEXEC_DFT));
    }
    
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        settings.addString(SDFCmdRemoteWriterNodeModel.KEY_UNIX, m_unixCommand.getText());
        settings.addString(SDFCmdRemoteWriterNodeModel.KEY_FILE, m_remoteFile.getText());
        settings.addBoolean(SDFCmdRemoteWriterNodeModel.KEY_NOTEXEC, m_createCommand.isSelected());
    }
}

