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
 * {@link CommandObject} for SDFConcatenate Node.
 * 
 * @author albertgo
 *
 */
public class SDFConcatenateCmdObject extends CommandObject {

    SDFConcatenateCmdObject(final String userOptions,
            final CMDProgramDefinition progDefintion) {
        super(userOptions, progDefintion);
    }

    
    public SDFConcatenateCmdObject(final CommandObject parentCommand,
            final String userOptions, final CMDProgramDefinition progDefinition) {
        super(parentCommand, userOptions, progDefinition);
    }
    
    
    public SDFConcatenateCmdObject(List<CommandObject> parentCommands,
            String userOptions, CMDProgramDefinition progDefintion) {
        super(parentCommands, userOptions, progDefintion);
    }

    protected String getCSHPipe(boolean newLineAllowed, String indent ) {
        
        String cmdSeparator = "\\\n";
        String catSeparator = ";\\\n";
        String nextIndent = "   ";
        if( ! newLineAllowed ) {
            cmdSeparator = "";
            catSeparator = " ; ";
        }

        List<CommandObject> parentCommands = getParentCmdObjectList();
        
        StringBuilder sb = new StringBuilder(2000);
        sb.append(indent).append("( ").append(cmdSeparator).append(" ");

        for (CommandObject co : parentCommands)
            sb.append(co.getCSHPipe(newLineAllowed, indent + nextIndent))
              .append(catSeparator).append(indent).append(" ");

        sb.setLength(sb.length() - 1 - indent.length() - catSeparator.length());
        sb.append(cmdSeparator).append(indent).append(")");

        return sb.toString();
    }
}
