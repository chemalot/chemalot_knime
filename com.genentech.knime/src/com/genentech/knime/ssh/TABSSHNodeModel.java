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
import java.io.FileWriter;
import java.io.IOException;

import org.knime.base.node.io.csvwriter.CSVWriter;
import org.knime.base.node.io.csvwriter.FileWriterSettings;
import org.knime.base.node.io.csvwriter.FileWriterSettings.quoteMode;
import org.knime.base.node.io.filereader.FileAnalyzer;
import org.knime.base.node.io.filereader.FileReaderNodeSettings;
import org.knime.base.node.io.filereader.FileTable;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeModel;
/**
 * {@link NodeModel} the writes a knime table to a remote command
 * executed via SSH and interprets its stdout as tab separated file
 * to create the knime table on the output port.
 * 
 * The tab file creation and parsing is delegated to methods in
 * org.knime.base.node.io
 * 
 * @author albertgo
 *
 */
public class TABSSHNodeModel extends AbstractSSHNodeModel {

   /**
    * Overwrite if TabSSHToolSettings ins not appropriate.
    */
   @Override
   public TABSSHToolSettings createSettings() {
      return new TABSSHToolSettings();
   }

   @Override
   public String getInFileExtention() {
      return "tab";
   }

   @Override
   public String getOutFileExtention() {
      return "tab";
   }

   @Override
   public BufferedDataTable parseOutput(ExecutionContext exec, File tmpOutFile) 
         throws IOException, CanceledExecutionException {
      exec.setMessage("Analyzing result tab file...");
      FileReaderNodeSettings frns = new FileReaderNodeSettings();
      frns.setDataFileLocationAndUpdateTableName(tmpOutFile.toURI().toURL());
      frns = FileAnalyzer.analyze(frns, exec.createSubProgress(0));
  
      DataTableSpec tSpec = new DataTableSpec("External SSH Tool output",
            frns.createDataTableSpec(), new DataTableSpec("empty"));
  
      FileTable ft = new FileTable(tSpec, frns, exec.createSubExecutionContext(0));
      exec.checkCanceled();
      BufferedDataTable result = 
            exec.createBufferedDataTables(new DataTable[] { ft }, exec)[0];

      exec.checkCanceled();
      
      return result;
   }

   @Override
   public void writeTMPInFile(ExecutionContext exec, BufferedDataTable inData, 
                              File tmpInFile) 
         throws IOException, CanceledExecutionException {
      CSVWriter csvWriter = null;
      try {
         exec.setMessage("Writing input table to "
               + "(local) temp CSV file...");
         FileWriterSettings fws = createFileWriterSettings();
         fws.setColSeparator("\t");
         fws.setReplaceSeparatorInStrings(true);
         fws.setSeparatorReplacement("\\t");
         fws.setQuoteMode(quoteMode.REPLACE);
         FileWriter inTableWriter = new FileWriter(tmpInFile);
         csvWriter = new CSVWriter(inTableWriter, fws);
         csvWriter.write(inData, exec.createSubProgress(0));
      } finally {
         if (csvWriter != null) {
            csvWriter.close();
         }
      }
   }

   private FileWriterSettings createFileWriterSettings() {
      FileWriterSettings fws = new FileWriterSettings();
      fws.setColSeparator(",");
      fws.setMissValuePattern("");
      fws.setQuoteBegin("\"");
      fws.setQuoteEnd("\"");
      fws.setQuoteMode(quoteMode.IF_NEEDED);
      fws.setWriteColumnHeader(true);
      fws.setWriteRowID(true);
      return fws;
   }


}
