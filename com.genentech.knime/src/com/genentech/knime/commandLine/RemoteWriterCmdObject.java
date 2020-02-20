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

import java.util.List;

/**
 * {@link CommandObject} for RemoteWriter Node.
 * 
 * @author albertgo
 *
 */
public class RemoteWriterCmdObject extends CommandObject {

    RemoteWriterCmdObject(CommandObject parentCommand, String userOptions, String mysubOptions,
            CMDProgramDefinition progDefinition) {
        super(parentCommand, userOptions, mysubOptions, progDefinition);
    }

    protected String getCSHPipe(boolean newLineAllowed, String indent ) {
        String cmdSeparator = "\\\n";
        String nextIndent = "   ";
        if( ! newLineAllowed ) {
            cmdSeparator = "";
        }
        
        List<CommandObject> parentCommands = getParentCmdObjectList();

        // only user options for RemoteWrite as command itself is "cat" 
        // and only used for single execution
        String cmdLine = "> " + getUserOptions();  

        if (parentCommands.size() == 1)
            return parentCommands.get(0).getCSHPipe(newLineAllowed, indent) 
                + " " + cmdSeparator + indent + nextIndent + cmdLine;

        assert false : "commandWriter should have single input port: " 
                      + getProgramDefintion().getName();
        // only known node with multiple parents is concatenate which overrides this method
        
        return null;
    }
}
