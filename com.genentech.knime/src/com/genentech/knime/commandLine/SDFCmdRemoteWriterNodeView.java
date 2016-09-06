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

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.knime.core.node.NodeView;

/**
 * {@link NodeView} for RemoteWriter Node.
 * 
 * @author albertgo
 *
 */
public class SDFCmdRemoteWriterNodeView extends NodeView<AbstractCommandNodeModel> {
    
    private JTextArea m_textArea;

    public SDFCmdRemoteWriterNodeView(final SDFCmdRemoteWriterNodeModel model) {
        super(model);
        
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.setName("SDF Command");
        panel.setBorder(BorderFactory.createTitledBorder(" SDF Command Line "));
        m_textArea = new JTextArea();
        m_textArea.setEditable(false);
        m_textArea.setText(model.getFullCommandLine());
        final JScrollPane jsp = new JScrollPane(m_textArea);
        jsp.setPreferredSize(new Dimension(800, 400));
        panel.add(jsp);
        
        setComponent(jsp);
        
        setShowNODATALabel(false);  // show commandLine even if not executed
    }
    
    @Override
    protected void modelChanged() {
        SDFCmdRemoteWriterNodeModel model = (SDFCmdRemoteWriterNodeModel)getNodeModel();
        m_textArea.setText(model.getFullCommandLine());
    }

    @Override
    protected void onClose() {
    }

    @Override
    protected void onOpen() {
        
    }
}
