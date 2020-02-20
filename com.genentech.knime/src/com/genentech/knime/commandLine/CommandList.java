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
import java.util.HashMap;
import java.util.List;

import org.jdom.Element;
import org.knime.core.node.NodeLogger;

import com.genentech.knime.Settings;


/**
 * Contains the list of all known {@link CMDProgramDefinition}.
 * 
 * Currently this can only be instantiated with an xml file descibing the nodes.
 * 
 * @author albertgo
 *
 */
public class CommandList {
    /**  */
    private final List<CMDProgramDefinition> programs;
    private final HashMap<String, CMDProgramDefinition> programMap;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CommandList.class);

    public final static CMDProgramDefinition SDF_CONCATENATER;
    public final static CMDProgramDefinition SDF_KNIME_BRIDGE;
    public static final CMDProgramDefinition SDF_REMOTE_WRITER;
    public final static CommandList DEFAULT;

    static
    {   // order dependent!!!
        SDF_CONCATENATER  = createSDFConcatenator();
        SDF_KNIME_BRIDGE  = createKnimeBridge();
        SDF_REMOTE_WRITER = createRemoteWriter();
        
        DEFAULT = new CommandList(Settings.CMD_CONFIG_FILE_PATH);
    }
    
    @SuppressWarnings("unchecked")
    CommandList(final String cmdConfigFilePath) {
        programs = new ArrayList<CMDProgramDefinition>();
        try {
            Element root = Settings.getConfigRoot(cmdConfigFilePath);

            // add progDefinitions from xml file
            for (Element e : (List<Element>) root.getChildren("command")) {
                CMDProgramDefinition cmdDef = XMLCMDProgramDefinition.createXMLProgram(e);
                programs.add(cmdDef);
            }
            
            programs.add(createRemoteReader());
            Collections.sort(programs);
            
            programMap = new HashMap<String, CMDProgramDefinition>(programs.size()+10);
            for( CMDProgramDefinition pd : programs )
                programMap.put(pd.getName(), pd);
            
            // add to map for de-serialization but not to programs which
            // is used to create dynamic nodes
            programMap.put(SDF_KNIME_BRIDGE.getName(),  SDF_KNIME_BRIDGE);
            programMap.put(SDF_CONCATENATER.getName(),  SDF_CONCATENATER);
            programMap.put(SDF_REMOTE_WRITER.getName(), SDF_REMOTE_WRITER);


        } catch (Exception e) {
            LOGGER.error("XML filename: " + cmdConfigFilePath);
            throw new Error(e);
        }
    }

    /**
     * Get {@link CMDProgramDefinition} for RemoteReader Node.
     */
    private static CMDProgramDefinition createRemoteReader() {
        String name = "Remote SDF Reader";
        String command = "cat";
        String help = "Read sdf on ssh server.\n"
                     +"Specify as many files as you need separated by space.\n"
                     +"File paths are relative to the working directory of your pipe.\n";
        String defOpts = "file1.sdf project/myPrj/vortex.sdf file3.sdf";
        String subfolder = "GNEReader";
        
        CMDProgramDefinition rdr = InternalCMDProgramDefinition.createCMDProgramDefinition(
                name, name, command, subfolder,
                CMDPortType.NONE, CMDPortType.SINGLE_SDF, 
                "", "", 
                defOpts, help);
        return rdr;
    }
    
    
    /**
     * Get {@link CMDProgramDefinition} for KnimeTosDF Node.
     */
    private static CMDProgramDefinition createKnimeBridge() {
        String name = "Pipe from Knime";
        String command = "cat";
        String help = "Read sdf file from KNIME pipe containing molecules.\n"
                     +"This node must be able to write to a network directory that is\n"
                     +"accessbile from the remote host.\n";
        String defOpts = "";
        String subfolder = "GNEConverter";
        
        CMDProgramDefinition knimePipe = InternalCMDProgramDefinition.createCMDProgramDefinition(
                name, name, command, subfolder,
                CMDPortType.NONE, CMDPortType.SINGLE_SDF, 
                "", "", 
                defOpts, help);
        return knimePipe;
    }
    
    
    /**
     * Get {@link CMDProgramDefinition} for RemoteWriter Node.
     */
    private static CMDProgramDefinition createRemoteWriter() {
        String name = "Remote SDF Writer";
        String command = "cat >";
        String help = "Write sdf file to location on remote host.";
        String defOpts = "fileName.sdf";
        String subFolder = "GNEWriter";
        
        
        CMDProgramDefinition rWriter = new InternalCMDProgramDefinition(
                name, name, command, subFolder,
                CMDPortType.SINGLE_SDF, null, 
                "", "", 
                defOpts, help ) {
            
            @Override
            public CommandObject createCommandObject(final CommandObject parentCommand,
                    final String userOptions, final String mysubOptions) {
                return new RemoteWriterCmdObject(parentCommand, userOptions, mysubOptions, this);
            }
        };
        return rWriter;
    }
    
    
    /**
     * Get {@link CMDProgramDefinition} for SDFConcatenator Node.
     */
    private static CMDProgramDefinition createSDFConcatenator() {
        return SDFConcatenateCMDProgramDefinition.createCMDProgramDefinition();
    }

    /**
     * Get list of all {@link CMDProgramDefinition}'s for all Knime Nodes.
     */
    public List<CMDProgramDefinition> getDefinitions() {
        return Collections.unmodifiableList(programs);
    }

    /**
     * Get list of all {@link CMDProgramDefinition}'s for Knime Nodes of given type.
     */
    public List<CMDProgramDefinition> getDefinitions(final CommandType type) {
        ArrayList<CMDProgramDefinition> res = new ArrayList<CMDProgramDefinition>(
                programs.size());
        for (CMDProgramDefinition def : programs)
            if (def.getType() == type)
                res.add(def);

        return res;
    }

    /**
     * Get list of all {@link CMDProgramDefinition}'s for Knime Node with given Name.
     */
    public CMDProgramDefinition getDefinition(String name) {
        return programMap.get(name);
    }

    /**
     * Just for testing
     */
    public static void main(final String... args) {
        List<CMDProgramDefinition> prgs = CommandList.DEFAULT.getDefinitions();
        for (CMDProgramDefinition p : prgs) {
            System.err.printf("%s: %s\n", p.getName(), p.getType());
        }
        
        CMDProgramDefinition p = DEFAULT.getDefinition("Remote SDF Writer");
        System.err.printf("%s: %s\n", p.getName(), p.getType());
        
    }
}
