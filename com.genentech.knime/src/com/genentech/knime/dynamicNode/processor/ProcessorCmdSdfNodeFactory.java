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
package com.genentech.knime.dynamicNode.processor;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;

import com.genentech.knime.dynamicNode.AbstractCmdSdfNodeFactory;

/**
 * {@link NodeFactory} for command line nodes with one input and one output port.
 * 
 * @author albertgo
 *
 */
public class ProcessorCmdSdfNodeFactory extends AbstractCmdSdfNodeFactory {


    /** {@inheritDoc} */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new ProcessorNodeDialogPane(getCMDProgramDefinition());
    }


    /** {@inheritDoc} */
    @Override
    public ProcessorCmdSdfNodeModel createNodeModel() {
        return new ProcessorCmdSdfNodeModel(getCMDProgramDefinition());
    }
}
