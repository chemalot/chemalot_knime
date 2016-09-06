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
 *   Jul 13, 2009 (ohl): created
 *   Aug 2012 modified from Knime sources 
 */
package com.genentech.knime.ssh;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SDFSSH" Node.
 * run SSH with stdin and out
 *
 * @author Genentech
 */
public class SDFSSHNodeFactory 
        extends NodeFactory<SDFSSHNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new SDFSSHNodeDialogPane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SDFSSHNodeModel createNodeModel() {
        return new SDFSSHNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<SDFSSHNodeModel> createNodeView(final int viewIndex,
            final SDFSSHNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }
}

