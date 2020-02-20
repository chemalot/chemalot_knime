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

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.genentech.knime.dynamicNode.CmdErrorOutputNodeView;

/**
 * <code>NodeFactory</code> for the "SDF Concatenate" Node.
 *
 * @author Thomas Gabriel, KNIME.com AG, Zurich
 */
public class SDFConcatenateNodeFactory extends NodeFactory<AbstractCommandNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractCommandNodeModel createNodeModel() {
        return new SDFConcatenateNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<AbstractCommandNodeModel> createNodeView(final int viewIndex,
            final AbstractCommandNodeModel nodeModel) {
        return new CmdErrorOutputNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new SDFConcatenateNodeDialog();
    }

}

