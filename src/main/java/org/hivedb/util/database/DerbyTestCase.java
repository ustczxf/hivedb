package org.hivedb.util.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import javax.sql.DataSource;

import org.hivedb.meta.persistence.HiveBasicDataSource;
import org.hivedb.util.database.DerbyUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class DerbyTestCase {
	//Override these valus to customize your test
	protected boolean cleanupDbAfterEachTest = false;
	protected String databaseName =  "derbyTestDb";
	protected String loadScript = null;
	protected String userName = "theuser";
	protected String password = "thepassword";
	protected boolean cleanupOnLoad = true;
	protected boolean cleanupOnExit = true;
	
	@SuppressWarnings("unused")
	@BeforeClass
	protected void initializeDerby(){
		if( cleanupOnLoad || cleanupDbAfterEachTest) 
			deleteDerbyDb();
		try {
			DerbyUtils.createDatabase(getDatabaseName(), userName, password);
			if( loadScript != null && !"".equals(loadScript))
				loadFromSqlScript();
		} catch (IOException e) {
			throw new DerbyTestCaseException(
					"Error initializing the Derby database: IOException while reading the sql script.",e);
		} catch (Exception e) {
			throw new DerbyTestCaseException("Error initializing the Derby database: " + e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unused")
	@AfterClass
	protected void cleanupDerby() {
		if( cleanupOnExit ){
			deleteDerbyDb();
		}
	}
	
	@BeforeMethod
	protected void beforeMethod() {
		if( cleanupDbAfterEachTest ){
			initializeDerby();
		}
	}
	@AfterMethod
	protected void afterMethod() {
		if( cleanupDbAfterEachTest ){
			deleteDerbyDb();
		}
	}

	
	protected String getConnectString()  {
		return DerbyUtils.connectString(getDatabaseName());
	}
	
	protected Connection getConnection() throws InstantiationException, SQLException {
		return DerbyUtils.getConnection(getDatabaseName(), userName, password);
	}
	
	protected DataSource getDataSource() {
		return new HiveBasicDataSource(getConnectString());
	}
	
	private void loadFromSqlScript() throws IOException, SQLException, InstantiationException {
		String sql = readFileAsString( relativePathToFullPath(loadScript));
		DerbyUtils.executeScript(sql, DerbyUtils.getConnection(getDatabaseName(), userName, password));
	}
	
	private void deleteDerbyDb() {
		String path;
		try {
			path = new File(".").getCanonicalPath() + File.separator + getDatabaseName();
			File db = new File(path);
			if( db.exists())
				DerbyUtils.deleteDatabase(new File(".").getCanonicalPath(), getDatabaseName());
		} catch (IOException e) {
			throw new DerbyTestCaseException("Error deleting database", e);
		}
	}
	
	private static String relativePathToFullPath(String relativePath){
		String baseDir = null;
		try {
			baseDir = new File(".").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baseDir + File.separator + relativePath;
	}
	
	private static String readFileAsString(String file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		StringBuilder sb = new StringBuilder();
		while((line = reader.readLine()) != null)
			sb.append(line + "\n");
		return sb.toString();
	}
	
	@SuppressWarnings("serial")
	class DerbyTestCaseException extends RuntimeException {
		public Throwable innerException;
		
		public DerbyTestCaseException(String message, Throwable t){
			//Don't ever set an empty message.  Apologies for the confusing syntax
			//but super() has to go first.
			super(message == null || "".equals(message) ? t.getMessage() : message);
			innerException = t;
		}
	}
	
	public String getDatabaseName() {
		return databaseName;
	}
	
	protected String[] dataNodes = new String[] { "data1", "data2", "data3" }; 
		
		public Collection<String> getDataUris() {
			return Arrays.asList(dataNodes);
		}
	}
