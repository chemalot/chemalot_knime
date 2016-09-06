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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * {@link PortObjectSpec} for SDF command Line Nodes.
 * 
 * @author albertgo
 *
 */
public class SDFCmdPortObjectSpec implements PortObjectSpec {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(SDFCmdPortObjectSpec.class);
    
    private final CommandObject m_commandObject;
    
    private final SSHConfiguration m_SSHConfiguration;
    
    public SDFCmdPortObjectSpec(final CommandObject command, final SSHConfiguration sshConfig) {
        m_commandObject = command;
        m_SSHConfiguration = sshConfig;
    }
    
    public SDFCmdPortObjectSpec(final SDFCmdPortObjectSpec parentSpec, final CommandObject command) {
        CMDProgramDefinition pdef = command.getProgramDefintion();
        m_commandObject = pdef.createCommandObject(parentSpec.m_commandObject, 
                                                     command.getUserOptions() );
        m_SSHConfiguration = parentSpec.getSSHConfiguration();
    }
    
    public SDFCmdPortObjectSpec(final SDFCmdPortObjectSpec[] parentSpecs, final CommandObject command) {
        if (parentSpecs.length < 1) {
            throw new IllegalArgumentException("SDF command input spec must be >= 1: " 
                    + parentSpecs.length);
        }
        ArrayList<CommandObject> parentCommands = new ArrayList<CommandObject>();
        for (int i = 0; i < parentSpecs.length; i++) {
            parentCommands.add(parentSpecs[i].m_commandObject);
        }
        CMDProgramDefinition pdef = command.getProgramDefintion();
        m_commandObject = pdef.createCommandObject(parentCommands,
                                                   command.getUserOptions()); 
        // only takes the SSH configuration from the first port
        m_SSHConfiguration = parentSpecs[0].getSSHConfiguration();
    }
        
    public CommandObject getCommandObject() {
        return m_commandObject;
    }
    public SSHConfiguration getSSHConfiguration() {
        return m_SSHConfiguration;
    }
    
    @Override
    public JComponent[] getViews() {
        // each returned value is on tab in the model/data out-port view
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.setName("SDF Command");
        panel.setBorder(BorderFactory.createTitledBorder(" SDF Command Line "));
        JTextArea pane = new JTextArea();
        pane.setEditable(false);
        pane.setText(m_commandObject.getCSHPipe(true));
        final JScrollPane jsp = new JScrollPane(pane);
        jsp.setPreferredSize(new Dimension(800, 400));
        panel.add(jsp);
        return new JComponent[]{ panel };
    }
    
    public static class Serializer
                 extends PortObjectSpecSerializer<SDFCmdPortObjectSpec> {

        @Override
        public SDFCmdPortObjectSpec loadPortObjectSpec(
                final PortObjectSpecZipInputStream in) throws IOException {
            return load(in);
        }

        @Override
        public void savePortObjectSpec(
                final SDFCmdPortObjectSpec portObjectSpec, 
                final PortObjectSpecZipOutputStream out) 
                throws IOException {
            save(out, portObjectSpec);
        }
    };
    
    /**
     * Serializer used to save <code>SDFCmdPortObjectSpec</code>.
     * @return a new SDFCmd spec serializer
     */
    public static PortObjectSpecSerializer<SDFCmdPortObjectSpec> 
            getPortObjectSpecSerializer() {
        return new Serializer();
    }
    
    private static final String KEY_SDF_COMMAND_SETTINGS = "SDF_COMMAND_SETTINGS";
    private static final String KEY_SSH_CONFIGURATION = "SSH_CONFIGURATION";
    
    private static SDFCmdPortObjectSpec load(
            final PortObjectSpecZipInputStream is) throws IOException {
        // load SDF command settings
        ZipEntry ze = is.getNextEntry();
        if (!ze.getName().equals(KEY_SDF_COMMAND_SETTINGS)) {
            throw new IOException("Key \"" + ze.getName() + "\" does not "
                    + " match expected zip entry name \"" 
                    + KEY_SDF_COMMAND_SETTINGS + "\".");
        }
        NodeSettingsRO sdfSettings = NodeSettings.loadFromXML(
                new NonClosableInputStream.Zip(is));
        // load SSH configuration
        ze = is.getNextEntry();
        if (!ze.getName().equals(KEY_SSH_CONFIGURATION)) {
            throw new IOException("Key \"" + ze.getName() + "\" does not "
                    + " match expected zip entry name \"" 
                    + KEY_SSH_CONFIGURATION + "\".");
        }
        NodeSettingsRO sshSettings = NodeSettings.loadFromXML(
                new NonClosableInputStream.Zip(is));
        try {
            CommandObject command = load(sdfSettings);
            SSHConfiguration sshConfig = SSHConfiguration.loadFromModel(sshSettings);
            return new SDFCmdPortObjectSpec(command, sshConfig);
        } catch (InvalidSettingsException ise) {
            throw new IOException(ise.getMessage(), ise);
        }
    }
    
    private static CommandObject load(final NodeSettingsRO settings) throws InvalidSettingsException {
        List<CommandObject> commands = new ArrayList<CommandObject>();
        String[] names = settings.getStringArray("names");
        for (String name : names) {
            NodeSettingsRO subSettings = settings.getNodeSettings(name);
            CommandObject co = load(subSettings);
            commands.add(co);
        }
        String userOptions = settings.getString("userOptions");
        String progDefinitionName = settings.getString("progDefinitionName");
        CMDProgramDefinition progDefinition;
        try {
            progDefinition = CommandList.DEFAULT.getDefinition(progDefinitionName);
        } catch( NoClassDefFoundError e)
        {   LOGGER.error(String.format("Error trying to load %s\n", progDefinitionName));
            throw e;
        }
        return progDefinition.createCommandObject(commands, userOptions);
    }
    
    private static void save(final PortObjectSpecZipOutputStream os, 
            final SDFCmdPortObjectSpec portObjectSpec) throws IOException {
        // save SDF command settings
        ZipEntry ze = new ZipEntry(KEY_SDF_COMMAND_SETTINGS);
        os.putNextEntry(ze);
        CommandObject command = portObjectSpec.getCommandObject(); 
        NodeSettingsWO settings = new NodeSettings(KEY_SDF_COMMAND_SETTINGS);
        save(settings, command);
        ((NodeSettings) settings).saveToXML(new NonClosableOutputStream.Zip(os));
        ze.clone();
        // save SSH configuration
        ze = new ZipEntry(KEY_SSH_CONFIGURATION);
        os.putNextEntry(ze);
        SSHConfiguration sshConfig = portObjectSpec.getSSHConfiguration(); 
        settings = new NodeSettings(KEY_SSH_CONFIGURATION);
        sshConfig.save(settings);
        ((NodeSettings) settings).saveToXML(new NonClosableOutputStream.Zip(os));
        ze.clone();
        os.close();
    }
    
    private static void save(final NodeSettingsWO settings, final CommandObject command) {
        settings.addString("userOptions", command.getUserOptions());
        settings.addString("progDefinitionName", command.getProgramDefintion().getName());
        List<CommandObject> list = command.getParentCmdObjectList();
        ArrayList<String> names = new ArrayList<String>(list.size());
        int parentNum = 0;
        for (CommandObject co : list) {
            String name = co.getProgramDefintion().getName() + '\t' + parentNum++;
            names.add(name);
            NodeSettingsWO subSettings = settings.addNodeSettings(name);// if two parents have the same name e.g. sdfTagTool.csh only one key is saved because name is used as key into settings object
            save(subSettings, co);
        }
        settings.addStringArray("names", names.toArray(new String[names.size()]));
    }

}
