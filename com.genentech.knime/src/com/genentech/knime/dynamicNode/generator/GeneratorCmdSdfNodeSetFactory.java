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
 *   Sep 10, 2012 (wiswedel): created
 */
package com.genentech.knime.dynamicNode.generator;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSetFactory;

import com.genentech.knime.commandLine.CommandType;
import com.genentech.knime.dynamicNode.AbstractCmdSdfNodeSetFactory;

/**
 * {@link NodeSetFactory} for command line nodes with one output port.
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
public class GeneratorCmdSdfNodeSetFactory extends AbstractCmdSdfNodeSetFactory {

    /**
     *  */
    public GeneratorCmdSdfNodeSetFactory() {
        super(CommandType.GENERATOR);
    }

    /** {@inheritDoc} */
    @Override
    public String getCategoryPathRoot(final String id) {
        return "/Genentech/GNESdfCmdLine";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends NodeFactory<? extends NodeModel>> getNodeFactory(
            final String id) {
        return (Class<? extends NodeFactory<? extends NodeModel>>)GeneratorCmdSdfNodeFactory.class;
    }

}
