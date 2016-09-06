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
package com.genentech.knime.dynamicNode.generator;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.OptionDocument.Option;

import com.genentech.knime.dynamicNode.AbstractCmdSdfNodeFactory;

/**
 * {@link NodeFactory} for command line nodes with one output port.
 * @author albertgo
 *
 */
public class GeneratorCmdSdfNodeFactory extends AbstractCmdSdfNodeFactory {


    /** {@inheritDoc} */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new GeneratorNodeDialogPane(getCMDProgramDefinition());
    }


    /** {@inheritDoc} */
    @Override
    public GeneratorCmdSdfNodeModel createNodeModel() {
        return new GeneratorCmdSdfNodeModel(getCMDProgramDefinition());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void addNodeDescription(final KnimeNodeDocument doc) {
        super.addNodeDescription(doc);
        FullDescription desc = doc.getKnimeNode().getFullDescription();
        
        Option opt = desc.addNewOption();
        opt.setName("SSH Tab");
        opt.addNewP().newCursor().setTextValue(
            "Options for configuring the remote execution via ssh.");
        opt.addNewP().newCursor().setTextValue(
            "These options will affect all nodes connected via the command line pipe "
           +"following the first generator node.");

        opt = desc.addNewOption();
        opt.setName("Pipe Mode");
        opt.addNewP().newCursor().setTextValue(
             "Decides if environment variables for production or development mode "
            +"are loaded prior to remote command execution.");
        
        opt = desc.addNewOption();
        opt.setName("Remote Directory");
        opt.addNewP().newCursor().setTextValue(
             "Directory on remote host in which the ssh execution will take place.");
        
        opt = desc.addNewOption();
        opt.setName("Error Log File");
        opt.addNewP().newCursor().setTextValue(
             "If specified the stderr of this command is written to this local file. "
            +"Specify a full path. Values of flow variable can be inserted using ${varName}. "
            +"This is especially useful on the server." );
    
        opt = desc.addNewOption();
        opt.setName("Execute in each node");
        opt.addNewP().newCursor().setTextValue(
             "If checked each node directly connected to via a command line pipe "
            +"will be executed separately on node execution. A copy of the output "
            +"data and stderror will be available on the output port and the node "
            +"view. This is slow but ideal for debugging a pipe.");
        opt.addNewP().newCursor().setTextValue(
             "If unchecked no execution takes place on each connected command node. "
            +"Instead you may use an Executer node (cf. consumer folder) to execute "
            +"the pipe as a whole. The unix command to execute the pipe is available "
            +"in each ouput port view.");
    }
}
