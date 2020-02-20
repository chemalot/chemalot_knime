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

import com.genentech.knime.Settings;

/**
 * This class encapsulates the information about one remote command line program.
 * 
 * It contains all information stored in the configuration xml file for one command line node.
 * 
 * It is unmutable.
 * 
 * @author albertgo
 *
 */
public abstract class CMDProgramDefinition implements
        Comparable<CMDProgramDefinition> { 
    static final String HELPFilePath = Settings.CONFIGPath + "/cmdLine/helpText";

    /** used as Node ID */
    private final String name;
    
    /** executed via ssh */
    private final String command;
    
    /** Displayed in tree and on node */
    private final String label;
    
    private final String subFolder;
    
    private final String inOptions;
    private final String outOptions;
    private final String defaultOpts;
    private final CMDPortType[] inPorts;
    private final CMDPortType[] outPorts;

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getSubFolder() {
        return subFolder;
    }

    public String getCommand() {
        return command;
    }

    public String getInOptions() {
        return inOptions;
    }

    public String getOutOptions() {
        return outOptions;
    }

    public String getIOOptions() {
        return inOptions + " " + outOptions;
    }

    public String getDefaultOpts() {
        return defaultOpts;
    }

    public String getDefaultMysubOpts() {
        return Settings.getMysubOptions();
    }

    public CMDPortType[] getInPorts() {
        return inPorts;
    }

    public CMDPortType[] getOutPorts() {
        return outPorts;
    }

    /**
     * A node can be a CommandType#CONSUMER if it only has one input port,
     *   a CommandType#PROCESSOR if it has one input and one output port,
     *   a CommandType#GENERATOR if it has only one output port
     *   or CommandType#OTHER.
     */
    public CommandType getType() {
        if (countPorts(inPorts, CMDPortType.SDF) == 1) {
            if (countPorts(outPorts, CMDPortType.SDF) == 0)
                return CommandType.CONSUMER;
            else if (countPorts(outPorts, CMDPortType.SDF) == 1)
                return CommandType.PROCESSOR;

        } else if (countPorts(inPorts, CMDPortType.SDF) == 0) {
            if (countPorts(outPorts, CMDPortType.SDF) == 1)
                return CommandType.GENERATOR;
        }

        return CommandType.OTHER;
    }

    /**
     * Count number of ports of given type.
     */
    private static int countPorts(final CMDPortType[] ports,
            final CMDPortType type) {
        int c = 0;
        for (CMDPortType p : ports)
            if (p == type)
                c++;

        return c;
    }

    public abstract String getHelpTxt();


    public CMDProgramDefinition(final String name, final String label, final String command,
            final String subFolder, final CMDPortType[] inPorts, final CMDPortType[] outPort,
            final String inOpt, final String outOpt, final String defOpts) {
        this.name    = name;
        this.command = command == null ? name : command;
        this.label   = label   == null ? name : label;
        this.subFolder=subFolder==null ? ""   : subFolder;
        
        this.inPorts = inPorts;
        this.outPorts = outPort;
        this.defaultOpts = defOpts;
        this.inOptions = inOpt;
        this.outOptions = outOpt;

    }

    public static void validate(final List<String> msgs, final String name,
            final String label, final String command, final String folder, 
            final CMDPortType[] inPorts,
            final CMDPortType[] outPort, final String inOpt,
            final String outOpt, final String defOpts) {
        if (name == null || name.length() == 0)
            msgs.add("Name may not be empty.");

    }

    @Override
    public int compareTo(final CMDProgramDefinition o) {
        return name.toUpperCase().compareTo(o.name.toUpperCase());
    }

    /** 
     * ComandLine to execute given the useroption supplied.
     */
    public String getCommandLine(String userOptions) {
        return command + " " + getIOOptions() + " " + userOptions;
    }
    
    /**
     * may be overwritten in special cases when the command generation requires
     * special processing.
     * see {@see ConcatenateProgramObject}
     */       
    public CommandObject createComamndObject(final String userOptions, final String mysubOptions) {
        return new CommandObject(userOptions, mysubOptions, this);
    }
    
    /**
     * may be overwritten in special cases when the command generation requires
     * special processing.
     * see {@see ConcatenateProgramObject}
     */       
    public CommandObject createCommandObject(final CommandObject parentCommand,
            final String userOptions, String mysubOptions) {
        return new CommandObject(parentCommand, userOptions, mysubOptions, this);
    }
    
    public CommandObject createCommandObject(final List<CommandObject> parentCommands,
            final String userOptions, String mysubOptions ) {
        return new CommandObject(parentCommands, userOptions, mysubOptions, this);
    }
}
