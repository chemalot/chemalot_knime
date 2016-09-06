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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import javax.swing.JTextArea;

//import org.knime.base.data.util.DataTableSpecExtractor;
import org.knime.chem.base.node.io.sdf.DefaultSDFReader;
import org.knime.chem.base.node.io.sdf.SDFReaderSettings;
import org.knime.core.data.DataTable;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.util.DataTableSpecExtractor;
import org.knime.core.data.util.DataTableSpecExtractor.PossibleValueOutputFormat;
import org.knime.core.data.util.DataTableSpecExtractor.PropertyHandlerOutputFormat;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.DefaultNodeProgressMonitor;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.Node;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.workflow.BufferedDataTableView;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.virtual.parchunk.VirtualParallelizedChunkPortObjectInNodeFactory;
import org.knime.core.util.FileUtil;

/** 
 * {@link PortObject} that can link SDF command line nodes.
 * 
 */
public class SDFCmdPortObject implements PortObject {
    
    /**
     * SDFCmd port type formed <code>PortObjectSpec.class</code> and
     * <code>PortObject.class</code> from this class.
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(SDFCmdPortObject.class);
//  public static final PortType TYPE = new PortType(SDFCmdPortObject.class);

    /**
     * Optional SDFCmd port type formed <code>PortObjectSpec.class</code> and
     * <code>PortObject.class</code> from this class.
     */
    public static final PortType TYPE_OPTIONAL = PortTypeRegistry.getInstance().getPortType(SDFCmdPortObject.class, true);
    //public static final PortType TYPE_OPTIONAL = new PortType(SDFCmdPortObject.class, true);
    
    
    private final SDFCmdPortObjectSpec m_spec;
    
    private final File m_sdfTempFile;
    
    private BufferedDataTable m_table;
    
    /**
     * 
     * @param spec
     * @param sdfTempFile is handed over for full control (including deletion) to this class
     */
    public SDFCmdPortObject(final SDFCmdPortObjectSpec spec, final File sdfTempFile) {
        m_spec = spec;
        m_sdfTempFile = sdfTempFile; 
    }
    
    private static String KEY_SDF_FILE = "KEY_SDF_FILE";

    
    
    
    public static final class Serializer extends PortObjectSerializer<SDFCmdPortObject> {
        /** {@inheritDoc} */
        @Override
        public void savePortObject(final SDFCmdPortObject portObject,
                final PortObjectZipOutputStream out,
                final ExecutionMonitor exec)
                throws IOException, CanceledExecutionException {
            ZipEntry ze = new ZipEntry(KEY_SDF_FILE);
            out.putNextEntry(ze);
            save(portObject, out);
            out.close();
        }

        /** {@inheritDoc} */
        @Override
        public SDFCmdPortObject loadPortObject(
                final PortObjectZipInputStream in,
                final PortObjectSpec spec,
                final ExecutionMonitor exec)
                throws IOException, CanceledExecutionException {
            ZipEntry ze = in.getNextEntry();
            if (!ze.getName().equals(KEY_SDF_FILE)) {
                throw new IOException("Key \"" + ze.getName() + "\" does not "
                        + " match expected zip entry name \"" 
                        + KEY_SDF_FILE + "\".");
            }
            return load(in, (SDFCmdPortObjectSpec) spec);
        }
    }

    /**
     * Serializer used to save <code>SDFCmdPortObject</code>.
     * @return a new SDF command port object serializer
     */
    public static PortObjectSerializer<SDFCmdPortObject>
            getPortObjectSerializer() {
        return new Serializer();
    }
    
    private static void save(final SDFCmdPortObject portObject, 
            final PortObjectZipOutputStream out) throws IOException {
        if (portObject.m_sdfTempFile == null) {
            return;
        }
        FileUtil.copy(new FileInputStream(portObject.m_sdfTempFile), out);
    }
    
    private static SDFCmdPortObject load(final PortObjectZipInputStream in, 
            final SDFCmdPortObjectSpec spec) throws IOException {
        final File tmpFile;
        if (spec.getSSHConfiguration().isExecuteSSH()) {
            tmpFile = File.createTempFile("SDFCmd_", ".sdf");
            FileUtil.copy(in, new FileOutputStream(tmpFile));
        } else {
            tmpFile = null;
        }
        return new SDFCmdPortObject(spec, tmpFile);
    }

    @Override
    public String getSummary() {
        return "SDF command line";
    }

    @Override
    public SDFCmdPortObjectSpec getSpec() {
        return m_spec;
    }
    
    public File getSDFile() {
        return m_sdfTempFile;
    }

    @Override
    public JComponent[] getViews() {
        if (!m_spec.getSSHConfiguration().isExecuteSSH()) {
            return new JComponent[0];
        }
        try {
            BufferedDataTable table = getTable();
            DataTableSpecExtractor e = new DataTableSpecExtractor();
            e.setPossibleValueOutputFormat(PossibleValueOutputFormat.Collection);
            e.setPropertyHandlerOutputFormat(PropertyHandlerOutputFormat.Hide);        
//            e.setExtractPossibleValuesAsCollection(true);
//            e.setExtractPropertyHandlers(false);
            DataTable extract = e.extract(table.getSpec());
            BufferedDataTableView bdtView = new BufferedDataTableView(table);
            BufferedDataTableView dtsView = new BufferedDataTableView(extract);
            return new JComponent[]{bdtView, dtsView};
        } catch (IOException ioe) {
            JTextArea area = new JTextArea();
            area.setText(ioe.getMessage() + "\n" + ioe.getCause());
            return new JComponent[]{area};
        }
    }
    
    public BufferedDataTable getTable() throws IOException {
        if (m_table != null) {
            return m_table;
        }
        m_table = getTable(m_sdfTempFile);
        return m_table;
    }
    
    public static BufferedDataTable getTable(final File sdfFile) 
            throws IOException {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        NodeFactory<NodeModel> dummyFactory =
            (NodeFactory) new VirtualParallelizedChunkPortObjectInNodeFactory(new PortType[0]);
        Node node = new Node(dummyFactory);
        ExecutionContext exec = new ExecutionContext(
                new DefaultNodeProgressMonitor(), node,
                SingleNodeContainer.MemoryPolicy.CacheOnDisc,
                new HashMap<Integer, ContainerTable>());
        /*TODO: 
         *This part is sdf-specific. We could write out the data differently,
         *if we know the port type.
         *For writing out tab-delimited files, check TABSSHNodeModel.parseOutput()*/
        exec.setMessage("Analyzing result SDF file...");
        SDFReaderSettings rSettings = new SDFReaderSettings();
        rSettings.extractSDF(false);
        rSettings.extractMol(true);
        rSettings.extractAllProperties(true);
        ArrayList<URL> iFiles = new ArrayList<URL>(1);
        iFiles.add(sdfFile.toURI().toURL());
        rSettings.urls(iFiles);
        DefaultSDFReader sdfReader = new DefaultSDFReader(rSettings);
        try {
           return sdfReader.execute(exec)[0]; // ignore error sdf records
        } catch (Exception e) {
           throw new IOException("Error reading SDF: " + e.getMessage(), e);
        }
    }

    @Override
    public void finalize()
    {   // clean up tmp files
        if( m_sdfTempFile != null ) 
            m_sdfTempFile.delete();
    }
}
