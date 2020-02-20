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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A CommandObject represents the command line arguments of a node and all
 * its preceding nodes.
 * 
 * @author albertgo
 *
 */
public class CommandObject {

    private final String m_userOptions;
    private final String m_mysubOptions;

    private final CMDProgramDefinition m_progDefinition;

    private final List<CommandObject> m_parentCommands;

    CommandObject(final String userOptions, final String mysubOptions,
            final CMDProgramDefinition progDefintion) {
        if( progDefintion == null ) 
            throw new IllegalArgumentException("Command Object needs program definition");
        m_parentCommands = new ArrayList<CommandObject>();
        m_userOptions = userOptions.replaceAll("[\n\r]" , " ");
        m_mysubOptions = mysubOptions.replaceAll("[\n\r]" , " ");
        m_progDefinition = progDefintion;
    }

    CommandObject(final CommandObject parentCommand,
            final String userOptions, final String mysubOptions,
            final CMDProgramDefinition progDefinition) {
        this(userOptions, mysubOptions, progDefinition);
        m_parentCommands.add(parentCommand);
    }

    /** For {@see SDFConcatenateNodeModel */
    CommandObject(final List<CommandObject> parentCommands,
            final String userOptions, final String mysubOptions, final CMDProgramDefinition progDefintion) {

        if( progDefintion == null ) 
            throw new IllegalArgumentException("Command Object needs program definition");
        
        m_parentCommands = new ArrayList<CommandObject>(parentCommands);
        m_userOptions = userOptions.replaceAll("[\n\r]" , " ");
        m_mysubOptions = mysubOptions.replaceAll("[\n\r]" , " ");
        m_progDefinition = progDefintion;
    }

    public String getUserOptions() {
        return m_userOptions;
    }

    public String getMysubOptions() {
        return m_mysubOptions;
    }

    /** get the mysub options of the first node in a sequence of sdf nodes **/
    public String getRootMysubOptions() {
    	CommandObject cmd = this;
    	while( cmd.m_parentCommands.size() > 0)
    		cmd = cmd.m_parentCommands.get(0);
    	
    	return cmd.m_mysubOptions;	
    }
    
    
    public CMDProgramDefinition getProgramDefintion() {
        return m_progDefinition;
    }

    public List<CommandObject> getParentCmdObjectList() {
        return Collections.unmodifiableList(m_parentCommands);
    }
    
    public String getMyCommandLine() {
        return this.m_progDefinition.getCommandLine( this.m_userOptions );
    }

    /**
     * Return a unix pipe for the c-shell to execute the command and all its
     * parents.
     */
    public String getCSHPipe(boolean newLineAllowed ) {
        return getCSHPipe(newLineAllowed, "");
    }

    protected String getCSHPipe(boolean newLineAllowed, String indent ) {
        
        String cmdSeparator = "\\\n";
        String nextIndent = "   ";
        if( ! newLineAllowed ) {
            cmdSeparator = "";
        }
        
        String cmdLine = m_progDefinition.getCommandLine(m_userOptions);

        if (m_parentCommands.size() == 0)
            return indent + cmdLine;

        if (m_parentCommands.size() == 1)
            return m_parentCommands.get(0).getCSHPipe(newLineAllowed, indent) 
                + " " + cmdSeparator + indent + nextIndent + "| " + cmdLine;

        assert false : "Unrecognized Program definition with multilple parents: " 
                      + m_progDefinition.getName();
        // only known node with multiple parents is concatenate which overrides this method
        
        return null;
    }

    @Override
    public String toString() {
        return "userOptions=" + m_userOptions + ";progDef=" + m_progDefinition
                + ":" + m_parentCommands;
    }
}
