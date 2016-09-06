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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.knime.core.node.NodeSetFactory;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.config.ConfigRO;

import com.genentech.knime.commandLine.CMDProgramDefinition;
import com.genentech.knime.commandLine.CommandList;
import com.genentech.knime.commandLine.CommandType;

/**
 * Abstract {@link NodeSetFactory} for command line nodes.
 * 
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 */
public abstract class AbstractCmdSdfNodeSetFactory implements NodeSetFactory {

    private final CommandType m_cmdType;

    /**
     *  */
    public AbstractCmdSdfNodeSetFactory(final CommandType cmdType) {
        m_cmdType = cmdType;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getNodeFactoryIds() {
        List<String> result = new ArrayList<String>();
        for (CMDProgramDefinition cmd : CommandList.DEFAULT.getDefinitions(m_cmdType)) {
            result.add(cmd.getName());
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String getAfterID(final String id) {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public ConfigRO getAdditionalSettings(final String id) {
        NodeSettings s = new NodeSettings("head");
        s.addString("programName", id);
        return s;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String getCategoryPath(final String id) {
    	final CMDProgramDefinition def = CommandList.DEFAULT.getDefinition(id);
    	String folder = def.getSubFolder();
    	if( folder.length() > 0 ) 
    	    return getCategoryPathRoot(id) + "/" + def.getSubFolder();
    	
    	return getCategoryPathRoot(id);
    }
    
    protected abstract String getCategoryPathRoot(final String id);

}
