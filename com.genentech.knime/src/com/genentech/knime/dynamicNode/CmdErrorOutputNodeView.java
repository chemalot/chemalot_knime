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
package com.genentech.knime.dynamicNode;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.knime.core.node.NodeView;

import com.genentech.knime.commandLine.AbstractCommandNodeModel;

/**
 * {@link NodeView} to display stderr output from command line node execution.
 * 
 * @author albertgo
 *
 */
public class CmdErrorOutputNodeView extends NodeView<AbstractCommandNodeModel> {
    
    private final JTabbedPane m_tabs = new JTabbedPane();

    public CmdErrorOutputNodeView(final AbstractCommandNodeModel model) {
        super(model);
        JScrollPane jsp = new JScrollPane(m_tabs);
        jsp.setPreferredSize(new Dimension(600, 400));
        setComponent(jsp);
    }
    
    @Override
    protected void modelChanged() {
        m_tabs.removeAll();
        String error = getNodeModel().getErrorOutput();
        JTextArea area = new JTextArea();
        area.setBorder(BorderFactory.createTitledBorder(
            " Command Error Output "));
        area.setEditable(false);
        area.setText(error);
        m_tabs.add("Error", area);
    }

    @Override
    protected void onClose() {
        
    }

    @Override
    protected void onOpen() {
        
    }
}
