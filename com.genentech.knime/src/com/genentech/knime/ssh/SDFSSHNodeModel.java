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
package com.genentech.knime.ssh;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.knime.chem.base.node.io.sdfwriter2.DefaultSDFWriter;
import org.knime.chem.base.node.io.sdfwriter2.SDFWriterSettings;
//import org.knime.chem.base.node.io.sdf.DefaultSDFWriter;
//import org.knime.chem.base.node.io.sdf.SDFWriterSettings;
import org.knime.chem.base.node.io.sdf.DefaultSDFReader;
import org.knime.chem.base.node.io.sdf.SDFReaderSettings;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeModel;
import org.knime.core.node.util.filter.NameFilterConfiguration.EnforceOption;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
/**
 * This {@link NodeModel} assumes the input port
 * has structure and property data; Writes it as an sdf file into
 * an SSH program executed on a remote host; Finally it reads input from
 * the remote program and tries to interpret it as SDF file to convert it
 * back into a knime table.
 * 
 * The parsing of the SDF files is delegated to implementations in the KNIME
 * classes in org.knime.chem.base.node.io.sdf and org.knime.chem.base.node.io.sdfwriter2.
 * 
 * @author albertgo @ Genentech
 */

public class SDFSSHNodeModel extends AbstractSSHNodeModel {
	
   /**
    * Overwrite if TabSSHToolSettings ins not appropriate.
    */
   @Override
   public SDFSSHToolSettings createSettings() {
      return new SDFSSHToolSettings();
   }

   @Override
   public String getInFileExtention() {
      return "sdf";
   }

   @Override
   public String getOutFileExtention() {
      return "sdf";
   }


   @Override
   public BufferedDataTable parseOutput(ExecutionContext exec, File tmpOutFile) 
         throws IOException, CanceledExecutionException {
      exec.setMessage("Analyzing result sdf file...");
      
      SDFReaderSettings rSettings = new SDFReaderSettings();
      rSettings.extractSDF(true);
      rSettings.extractAllProperties(true);
      ArrayList<URL> iFiles = new ArrayList<URL>(1);
      iFiles.add(tmpOutFile.toURI().toURL());
      rSettings.urls(iFiles);
      DefaultSDFReader sdfReader = new DefaultSDFReader(rSettings );
      BufferedDataTable result;
      try {
         result = sdfReader.execute(exec)[0]; // ignore error sdf records
         
      } catch (Exception e) {
         throw new IOException("Error reading sdf: " +e.getMessage(), e);
      }
      exec.checkCanceled();
      
      return result;
   }

   @Override
   public void writeTMPInFile(ExecutionContext exec, BufferedDataTable inData, 
                              File tmpInFile) throws IOException {
      DefaultSDFWriter sdfWriter = null;
      try {
         SDFSSHToolSettings confSettings = (SDFSSHToolSettings)getConfigSettings();
         String structCol = confSettings.getStructColumn();
         SDFWriterSettings wSet = new SDFWriterSettings();
         wSet.addEmptyStructuresForMissing(true);
         wSet.fileName(tmpInFile.toString());
         wSet.overwriteOK(true);
         wSet.structureColumn(structCol);
         exec.setMessage("Writing input table to (local) temp sdf file...");
      
         DataColumnSpecFilterConfiguration colFilter 
             = new DataColumnSpecFilterConfiguration("testF");
         colFilter.loadDefaults(new String[0], new String[] { structCol }, 
                                EnforceOption.EnforceExclusion);
         wSet.setFilterConfiguration(colFilter);
         
         sdfWriter = new DefaultSDFWriter( wSet );
         DataTableSpec inSpec = inData.getDataTableSpec();
         final int colCount = inSpec.getNumColumns();
         CloseableRowIterator it = inData.iterator();
         
         sdfWriter.execute(inSpec, it, colCount, exec.createSubProgress(0));
         
         it.close();
      } catch (Exception e) {
         throw new IOException("In SDFWriter: " + e.getMessage(),e);
      }
   }
}
