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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

//import org.knime.chem.base.node.io.sdf.DefaultSDFWriter;
//import org.knime.chem.base.node.io.sdf.SDFWriterSettings;
import org.knime.chem.base.node.io.sdfwriter2.DefaultSDFWriter;
import org.knime.chem.base.node.io.sdfwriter2.SDFWriterSettings;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.filter.NameFilterConfiguration.EnforceOption;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.workflow.FlowVariable;

import com.genentech.knime.dynamicNode.generator.GeneratorCmdSdfNodeModel;

/**
 * Node that converts Knime input to SDF Command Line port.
 *
 */
public class KnimeSDFCMDBridgeNodeModel extends AbstractCommandNodeModel {

    static final String CMD_KNIME_BRIDGE_SET = "KnimeBrdgSet";
    private SSHConfiguration m_sshConfiguration;
    private KnimeSDFCMDBridgeSettings m_Settings;
    private File tmpExchangeFile;

    /**
     * Constructor for the node model.
     */
    protected KnimeSDFCMDBridgeNodeModel() {
        super( new PortType[] { BufferedDataTable.TYPE },
               new PortType[] { SDFCmdPortObject.TYPE  } );
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_sshConfiguration == null) {
            throw new InvalidSettingsException("No ssh configuration available");
        }
        
        if (m_Settings == null) {
            throw new InvalidSettingsException("No input configuration available");
        }


        File localExchangeDir = null;
        try {
            localExchangeDir = new File(replaceVars(m_Settings.getLocalExchangeDir()));
            if( ! localExchangeDir.isDirectory() )
            {   throw new InvalidSettingsException("local exchange dir invalid: " + localExchangeDir.getAbsolutePath() + " - try using flow variable");
            }
            

            tmpExchangeFile = File.createTempFile("SDFCmd_", ".sdf", localExchangeDir);
            String remoteFile = replaceVars(m_Settings.getRemoteExchangeDir() + "/" + tmpExchangeFile.getName());
            String mysubOpts = m_Settings.getMysubOptions();
            CommandObject command = CommandList.SDF_KNIME_BRIDGE.createComamndObject(remoteFile, mysubOpts);
            
            return new PortObjectSpec[]{new SDFCmdPortObjectSpec(command, m_sshConfiguration)};
            
        } catch (IOException e) {
            LOGGER.error(String.format("exchangeDir=%s remoteDir=%s remoteFile=%s", 
                    localExchangeDir == null ?  null : localExchangeDir.toString(), 
                    m_Settings.getRemoteExchangeDir(), 
                    tmpExchangeFile == null ? null : tmpExchangeFile.getName()));
            throw new Error(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
          final ExecutionContext exec) throws Exception {
        BufferedDataTable inTable = (BufferedDataTable) inData[0];

        String remoteFile = m_Settings.getRemoteExchangeDir() + "/" + tmpExchangeFile.getName();
        String mysubOpts = m_Settings.getMysubOptions();
        CommandObject command = CommandList.SDF_KNIME_BRIDGE.createComamndObject(remoteFile, mysubOpts);
        writeTableToSDFFile(exec, inTable, tmpExchangeFile);

        SDFCmdPortObjectSpec outSpec = new SDFCmdPortObjectSpec( command, m_sshConfiguration);

        if( m_sshConfiguration.isExecuteSSH() ) {
            SSHExecutionResult sshRes = null;
            try {
                sshRes = runSSHExecute(outSpec, exec, null);
            }finally
            {   // this was done only for debugging, downstream nodes will read from tmpExchangeFile
                try { 
                 // stderr is still used in AbstractCommandNodeModel
                sshRes.getStdOut().delete();
                }catch(Exception e)
                {   LOGGER.error(e);
                }
            }
        }
        
        return new PortObject[]{new SDFCmdPortObject(outSpec, tmpExchangeFile)};
    }

 
    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {
        NodeSettingsRO nodeSettings = settings.getNodeSettings(
                            GeneratorCmdSdfNodeModel.SSH_CONFIGURATION_NAME);
        SSHConfiguration.loadFromModel(nodeSettings);
        
        nodeSettings = settings.getNodeSettings(CMD_KNIME_BRIDGE_SET);
        KnimeSDFCMDBridgeSettings.validateSettings(nodeSettings);
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {
        NodeSettingsRO nodeSettings = settings
                .getNodeSettings(GeneratorCmdSdfNodeModel.SSH_CONFIGURATION_NAME);
        SSHConfiguration sshSet = SSHConfiguration.loadFromModel(nodeSettings);
        m_sshConfiguration = sshSet;

        nodeSettings = settings.getNodeSettings(CMD_KNIME_BRIDGE_SET);
        KnimeSDFCMDBridgeSettings kSet = KnimeSDFCMDBridgeSettings.loadFromModel(nodeSettings);
        m_Settings = kSet;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_sshConfiguration != null) {
            NodeSettingsWO sshSettings = settings.addNodeSettings(
                                GeneratorCmdSdfNodeModel.SSH_CONFIGURATION_NAME);
            m_sshConfiguration.save(sshSettings);
        }
        
        if (m_Settings != null) {
            NodeSettingsWO kSet = settings.addNodeSettings(CMD_KNIME_BRIDGE_SET);
            m_Settings.save(kSet);
        }
    }

    public void writeTableToSDFFile(ExecutionContext exec,
            BufferedDataTable inData, File sdfFile) throws IOException {
        DefaultSDFWriter sdfWriter = null;
        try {
            String structCol = m_Settings.getStructColumn();
            SDFWriterSettings wSet = new SDFWriterSettings();
            
            wSet.addEmptyStructuresForMissing(true);
            wSet.fileName(sdfFile.toString());
            wSet.overwriteOK(true);
            wSet.structureColumn(structCol);
            exec.setMessage("Writing input table to (local) temp sdf file: " + sdfFile);
            
            DataColumnSpecFilterConfiguration colFilter 
                = new DataColumnSpecFilterConfiguration("testF");
            colFilter.loadDefaults(new String[0], new String[] { structCol }, 
                                   EnforceOption.EnforceExclusion);
            wSet.setFilterConfiguration(colFilter);
            
            sdfWriter = new DefaultSDFWriter(wSet);
            DataTableSpec inSpec = inData.getDataTableSpec();
            final int colCount = inSpec.getNumColumns();
            CloseableRowIterator it = inData.iterator();

            sdfWriter.execute(inSpec, it, colCount, exec.createSubProgress(0));

            it.close();
        } catch (Exception e) {
            throw new IOException("In SDFWriter: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void finalize() {
        if( tmpExchangeFile != null ) tmpExchangeFile.delete();
    }
    
    private String replaceVars(String txt) {
        Collection<FlowVariable> vars = getAvailableInputFlowVariables().values();
        for(FlowVariable var : vars) {
            String vName = var.getName();
            if( ! vName.matches("\\w+") ) continue;
            
            // ignore flow variable containing unexpected characters
            String value = var.getValueAsString().trim();
            if( value.matches("[^A-Za-z_0-1/\\:]") ) continue;
            
            txt = txt.replace("${"+vName+'}', value);
            txt = txt.replace("$"+vName, value);
        }
        return txt;
    }
    
}
