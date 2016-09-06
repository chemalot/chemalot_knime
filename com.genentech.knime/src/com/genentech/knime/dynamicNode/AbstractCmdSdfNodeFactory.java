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
package com.genentech.knime.dynamicNode;

import org.apache.xmlbeans.XmlString;
import org.knime.core.node.DynamicNodeFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeView;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.InPortDocument.InPort;
import org.knime.node2012.IntroDocument.Intro;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.KnimeNodeDocument.KnimeNode.Type;
import org.knime.node2012.OptionDocument.Option;
import org.knime.node2012.OutPortDocument.OutPort;
import org.knime.node2012.PortsDocument.Ports;
import org.knime.node2012.ViewDocument.View;
import org.knime.node2012.ViewsDocument.Views;

import com.genentech.knime.commandLine.AbstractCommandNodeModel;
import com.genentech.knime.commandLine.CMDPortType;
import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.CommandList;

/**
 * {@link DynamicNodeFactory} for command line nodes.
 * 
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
public abstract class AbstractCmdSdfNodeFactory extends DynamicNodeFactory<AbstractCommandNodeModel> {

    private CMDProgramDefinition m_programDefinition;

    /** @return the programDefinition */
    public final CMDProgramDefinition getCMDProgramDefinition() {
        return m_programDefinition;
    }

    /** {@inheritDoc} */
    @Override
    public void loadAdditionalFactorySettings(final ConfigRO config)
            throws InvalidSettingsException {
        CMDProgramDefinition programDefinition = null;
        String programName = config.getString("programName");
        programDefinition = CommandList.DEFAULT.getDefinition(programName);
        
        if (programDefinition == null) {
            throw new InvalidSettingsException(
                    "Invalid program definition, not in CommandList: " + programName);
        }
        m_programDefinition = programDefinition;
        super.loadAdditionalFactorySettings(config);
    }

    /** {@inheritDoc} */
    @Override
    public void saveAdditionalFactorySettings(final ConfigWO config) {
        config.addString("programName", m_programDefinition.getName());
        super.saveAdditionalFactorySettings(config);
    }

    /** {@inheritDoc} */
    @Override
    protected void addNodeDescription(final KnimeNodeDocument doc) {
        KnimeNode knimeNode = doc.addNewKnimeNode();
        knimeNode.setName(m_programDefinition.getLabel());
        if(m_programDefinition.getInPorts().length == 0)
            knimeNode.setType(Type.SOURCE);
        else {
            if( m_programDefinition.getOutPorts().length == 0)
                knimeNode.setType(Type.SINK);
            else
                knimeNode.setType(Type.MANIPULATOR);
        }
        //knimeNode.setType(m_programDefinition.getInPorts().length == 0 ? Type.SOURCE : Type.MANIPULATOR);
        knimeNode.setIcon("../../../icons/dna3.png");
        knimeNode.setShortDescription(m_programDefinition.getName());
        FullDescription fullDescription = knimeNode.addNewFullDescription();
        Intro intro = fullDescription.addNewIntro();
        XmlString pre = intro.addNewPre();
        pre.setStringValue(m_programDefinition.getHelpTxt());
        Option newOption = fullDescription.addNewOption();
        newOption.setName("input options");
        String s = "input";
        if( knimeNode.getType() == Type.MANIPULATOR )
            s = s + "/output";
        newOption.addNewP().newCursor().setTextValue("mandatory " + s + " options ("
                + m_programDefinition.getInOptions() + ")");
        Option otherOption = fullDescription.addNewOption();
        otherOption.setName("other options");
        otherOption.addNewP().newCursor().setTextValue("additional user options (see help text for details)");
        Views views = knimeNode.addNewViews();
        View view = views.addNewView();
        view.setIndex(0);
        view.setName("Command Error Output");
        view.newCursor().setTextValue("Shows the stderr of the (ssh) command if each node is executed via ssh.");
        Ports ports = knimeNode.addNewPorts();
        int inportIndex = 0;
        for (CMDPortType pt : m_programDefinition.getInPorts()) {
            InPort inPort = ports.addNewInPort();
            inPort.setName(pt.name());
            inPort.setIndex(inportIndex++);
            inPort.newCursor().setTextValue("...");
        }
        int outportIndex = 0;
        for (CMDPortType pt : m_programDefinition.getOutPorts()) {
            OutPort outPort = ports.addNewOutPort();
            outPort.setName(pt.name());
            outPort.setIndex(outportIndex++);
            outPort.newCursor().setTextValue("...");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public NodeView<AbstractCommandNodeModel> createNodeView(final int viewIndex,
            final AbstractCommandNodeModel nodeModel) {
        return new CmdErrorOutputNodeView(nodeModel);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean hasDialog() {
        return true;
    }

}
