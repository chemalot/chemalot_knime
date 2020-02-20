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
import java.util.List;

/**
 * {@link CMDProgramDefinition} for SDF Concatenate Ndoe.
 * 
 * @author albertgo
 *
 */
public class SDFConcatenateCMDProgramDefinition extends InternalCMDProgramDefinition {

    public static CMDProgramDefinition createCMDProgramDefinition() {
        String name = "SDF Concatenate";
        String command = "cat";
        CMDPortType[] inPorts = new CMDPortType[] {CMDPortType.SDF,CMDPortType.SDF,CMDPortType.SDF,CMDPortType.SDF}; 
        CMDPortType[] outPort = CMDPortType.SINGLE_SDF;
        String inOpt = "";
        String outOpt = "";
        String defOpts = "";
        String subFolder = "GNEDataManipulation";
        String help = "Concatenates multiple pipes into a single pipe.\n"
            +"No options are allowed.\n"
            +"The records are ordered from the top pipe to the bottom pipe.\n";
        
        List<String> msgs = new ArrayList<String>();

        CMDProgramDefinition.validate(msgs, name, name, command, "", inPorts, outPort,
                                      inOpt, outOpt, defOpts);

        if (msgs.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Error in definition for %s:\n", name));
            for (String s : msgs)
                sb.append('\t').append(s).append('\n');

            throw new IllegalArgumentException(sb.toString());
        }

        return new SDFConcatenateCMDProgramDefinition(name, command, subFolder,
                inPorts, outPort, inOpt, outOpt, defOpts, help);
    }

    private SDFConcatenateCMDProgramDefinition(final String name,
            final String command, final String subFolder, final CMDPortType[] inPorts,
            final CMDPortType[] outPort, final String inOpt,
            final String outOpt, final String defOpts, final String helpTxt) {
        super(name, name, command, subFolder, inPorts, outPort, inOpt, outOpt, defOpts, helpTxt);
    }

    @Override
    public CommandObject createComamndObject(final String userOptions, final String mysubOptions) {
        return new SDFConcatenateCmdObject(userOptions, mysubOptions, this);
    }
    
    @Override
    public CommandObject createCommandObject(final CommandObject parentCommand,
            final String userOptions, final String mysubOptions) {
        return new SDFConcatenateCmdObject(parentCommand, userOptions, mysubOptions, this);
    }
    
    @Override
    public CommandObject createCommandObject(final List<CommandObject> parentCommands,
            final String userOptions, final String mysubOptions ) {
        return new SDFConcatenateCmdObject(parentCommands, userOptions, mysubOptions, this);
    }
    
}
