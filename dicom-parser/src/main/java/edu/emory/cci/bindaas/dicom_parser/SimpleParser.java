package edu.emory.cci.bindaas.dicom_parser;

import java.io.File;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomInputStream;

public class SimpleParser {

	public static JsonObject parseHeaders(File dicomFile) throws Exception
	{
		AttributeList attrList = new AttributeList();
		attrList.read(new DicomInputStream(dicomFile));
		JsonObject json = new JsonObject();
		for(Object attrT : attrList.keySet())
		{
			AttributeTag attrTag = (AttributeTag) attrT;
			Attribute attr = attrList.get(attrTag);
			String attrName = AttributeList.getDictionary().getNameFromTag(attrTag); 
			if(attr.getSingleStringValueOrNull()!=null)
				json.add( attrName == null ? attrTag.toString() : attrName, new JsonPrimitive(attr.getSingleStringValueOrNull() ));
			
		}
		System.out.println(json);
		return json;	
	}
}
