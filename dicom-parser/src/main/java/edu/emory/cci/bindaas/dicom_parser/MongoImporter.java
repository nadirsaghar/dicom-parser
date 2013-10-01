package edu.emory.cci.bindaas.dicom_parser;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomInputStream;

public class MongoImporter {

	private String rootImageDirectory;
	private String mongoHost;
	private int mongoPort;
	private String mongoDBName;
	private String mongoCollection;
	private String filepathFieldName = "filepath";
	

	public String getFilepathFieldName() {
		return filepathFieldName;
	}



	public void setFilepathFieldName(String filepathFieldName) {
		this.filepathFieldName = filepathFieldName;
	}

	private Log log = LogFactory.getLog(getClass());
	private DBCollection dbCollection;
	private int count = 0;

	
	public void init() throws Exception
	{
		Mongo mongo = new Mongo(mongoHost,mongoPort);
		DB db = mongo.getDB(mongoDBName);
		dbCollection = db.getCollection(mongoCollection);
		dbCollection.drop();
	}
	
	
	
	public void startIndexing()
	{
		log.info("Starting to index DICOM files");
		buildIndex(new File(rootImageDirectory));
		
	}
	
	
	public void buildIndex(File directory)
	{
		log.info("Building index for directory [" + directory + "]");
		File[] subdirs = directory.listFiles( new FileFilter() {
			
			public boolean accept(File pathname) {
				if(pathname.isDirectory() && pathname.isHidden()==false)
					return true;
				else
					return false;
			}
		});
		
		for(File subdir : subdirs)
		{
			buildIndex(subdir);
		}
		
		File[] dcmFiles = directory.listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				if(pathname.isFile() && pathname.canRead() && pathname.getName().endsWith("dcm"))
					return true;
				else
					return false;
			}
		}) ;
		
		for(File dcmFile : dcmFiles)
		{
			try {
				parseAndStore(dcmFile);
			} catch (Exception e) {
				log.error("Could not parse and store DICOM file",e);
			}
		}
	}
	
	
	public void parseAndStore(File dicomFile) throws Exception
	{
		log.info("Parsing DICOM file [" + dicomFile + "]");
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
		tuple.put(filepathFieldName, dicomFile.getAbsoluteFile().toString());
		dbCollection.insert(tuple);
		log.info("Total file indexed [" + ++count  +"]");
	}
	public String getRootImageDirectory() {
		return rootImageDirectory;
	}

	public void setRootImageDirectory(String rootImageDirectory) {
		this.rootImageDirectory = rootImageDirectory;
	}

	public String getMongoHost() {
		return mongoHost;
	}

	public void setMongoHost(String mongoHost) {
		this.mongoHost = mongoHost;
	}

	public int getMongoPort() {
		return mongoPort;
	}

	public void setMongoPort(int mongoPort) {
		this.mongoPort = mongoPort;
	}

	public String getMongoDBName() {
		return mongoDBName;
	}

	public void setMongoDBName(String mongoDBName) {
		this.mongoDBName = mongoDBName;
	}

	public String getMongoCollection() {
		return mongoCollection;
	}

	public void setMongoCollection(String mongoCollection) {
		this.mongoCollection = mongoCollection;
	}

	public DBCollection getDbCollection() {
		return dbCollection;
	}

	public void setDbCollection(DBCollection dbCollection) {
		this.dbCollection = dbCollection;
	}
	
	public static void main(String[] args) throws Exception {
		MongoImporter builder = new MongoImporter();
		
		if(System.getProperty("mongoHost") == null)
		{
			builder.setMongoHost("localhost");
		}
		else
		{
			builder.setMongoHost(System.getProperty("mongoHost"));
		}
		
		
		if(System.getProperty("mongoCollection") == null)
		{
			builder.setMongoCollection("mongopacsCollection");
		}
		else
		{
			builder.setMongoCollection(System.getProperty("mongoCollection"));
		}
		
		
		if(System.getProperty("mongoDb") == null)
		{
			builder.setMongoDBName("mongopacsDb");
		}
		else
		{
			builder.setMongoDBName(System.getProperty("mongoDb"));
		}
		
		
		
		
		if(System.getProperty("baseDir") == null)
		{
			builder.setRootImageDirectory(".");
		}
		else
		{
			builder.setRootImageDirectory(System.getProperty("baseDir"));
		}
		
		
		
		
		
		
		builder.setMongoPort(27017);
		builder.init();
		builder.startIndexing();
	}
}
