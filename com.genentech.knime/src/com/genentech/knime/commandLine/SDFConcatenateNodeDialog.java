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
public class SDFConcatenateNodeDialog extends NodeDialogPane {

    private final JTextField m_mysubOptions = new JTextField(30);

	
    /**
     * New pane for configuring SDFConcatenateNodeDialog node.
     */
    protected SDFConcatenateNodeDialog() {
        ////////////////////////////////
        final FlowVariableModel mysubModel = createFlowVariableModel(SDFConcatenateNodeModel.KEY_MYSUBOPTS, FlowVariable.Type.STRING);
        @SuppressWarnings("serial")
        final GNEFlowVariableModelButton mysubOptButton = new GNEFlowVariableModelButton(this, mysubModel) {
        	@Override
        	public void setTextField(String s) {
        		m_mysubOptions.setText(s);
        	}
        	@Override
        	public void setFieldEditable(boolean bool) {
        		m_mysubOptions.setEditable(bool);
        	}
        };
        final JPanel mysubPanel = new JPanel();
        mysubPanel.add(new JLabel("mysub options:"));
        mysubPanel.add(m_mysubOptions);
        mysubPanel.add(mysubOptButton);
        
        
        final JPanel p = new JPanel(new GridLayout(4, 1));
        p.add(mysubPanel);
        addTab("Options", p);
    }
    
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) 
    		throws NotConfigurableException {
        m_mysubOptions.setText(settings.getString(SDFConcatenateNodeModel.KEY_MYSUBOPTS, 
        		SDFConcatenateNodeModel.MYSUBOPTS_DFT));
    }
    
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        settings.addString(SDFConcatenateNodeModel.KEY_MYSUBOPTS, m_mysubOptions.getText());
    }
}

