package edu.emory.cci.bindaas.dicom_parser.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import au.com.bytecode.opencsv.CSVReader;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.dicom_parser.SimpleParser;

/**
 * A galaxy tool that reads a CSV file containing physical links to the dicom images and outputs Dicom Headers in JSON format
 * @author nadir
 *
 */
public class GalaxyTool {

	private String inputFile;
	private String outputFile;
	
	public void startProcess() throws Exception
	{
		CSVReader csvReader = new CSVReader(new FileReader(inputFile));
		String[] row = null;
		FileWriter fw = new FileWriter(outputFile);
		fw.append("[");
		int rowIndex = 0;
		while( (row = csvReader.readNext())!=null)
		{
			try {
			if(row.length > 0 )
			{
				String dicomFilePath = row[0];
				JsonObject json = SimpleParser.parseHeaders(new File(dicomFilePath));
				if(rowIndex > 0)
				{
					fw.append(",").append(json.toString());
				}
				else
				{
					fw.append(json.toString());
				}
				
				fw.flush();
			}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		
		fw.append("]");
		fw.close();
		csvReader.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		Options  options = new Options();
		Option inputOption = new Option("i", true, "input csv");
		Option outputOption = new Option("o", true, "output file");
		options.addOption(inputOption).addOption(outputOption);
		CommandLineParser parser = new BasicParser();
		CommandLine line = parser.parse( options, args );
		String inputFile = line.getOptionValue("i");
		String outputFile = line.getOptionValue("o");
		
		if(inputFile == null) throw new Exception("Input file not specified");
		if(outputFile == null) throw new Exception("Output file not specified");
		
		GalaxyTool galaxyTool = new GalaxyTool();
		galaxyTool.inputFile = inputFile;
		galaxyTool.outputFile = outputFile;
		galaxyTool.startProcess();
	}
}
