package edu.emory.cci.bindaas.dicom_parser;

import java.io.File;

import com.mongodb.BasicDBObject;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomInputStream;

public class SimpleParser {

	public static String parseHeaders(File dicomFile) throws Exception
	{
		AttributeList attrList = new AttributeList();
		attrList.read(new DicomInputStream(dicomFile));
		BasicDBObject tuple = new BasicDBObject();
		for(Object attrT : attrList.keySet())
		{
			AttributeTag attrTag = (AttributeTag) attrT;
			Attribute attr = attrList.get(attrTag);
			String attrName = AttributeList.getDictionary().getNameFromTag(attrTag); 
			tuple.put( attrName == null ? attrTag.toString() : attrName, attr.getSingleStringValueOrNull());
			
		}
		return tuple.toString();
		
	}
}
