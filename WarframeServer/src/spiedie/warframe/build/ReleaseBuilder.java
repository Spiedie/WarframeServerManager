package spiedie.warframe.build;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import spiedie.utilities.encryption.KeyGen;
import spiedie.utilities.files.FileUtils;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class ReleaseBuilder {
	/*
	 * 0.1.0.0 initial version
	 */
	public static final String VERSION = "0.1.0.0";
	
	public static void build(String root) throws IOException, NoSuchAlgorithmException {
		if(!new File("../program/WFAllocManagerLocal/WFAllocManager.jar").exists()) Log.err(ReleaseBuilder.class, "Create JAR using builder/WFAllocManager.jardesc first");
		if(!new File("../program/WFAllocSetup/WFAllocSetup.jar").exists()) Log.err(ReleaseBuilder.class, "Create JAR using builder/WFAllocSetup.jardesc first");
		if(!new File("../program/WFHttpApi/WFHttpApi.jar").exists()) Log.err(ReleaseBuilder.class, "Create JAR using builder/WarframeApi.jardesc first");
		if(!new File("../program/WFServerConfig/WFServerConfig.jar").exists()) Log.err(ReleaseBuilder.class, "Create JAR using builder/WFServerConfigExport.jardesc first");
		
		// jars
		Stream.copyFile("../program/WFAllocManagerLocal/WFAllocManager.jar", root+"/WFAllocManagerLocal/WFAllocManager.jar", false);
		Stream.copyFile("../program/WFAllocSetup/WFAllocSetup.jar", root+"/WFAllocSetup/WFAllocSetup.jar", false);
		Stream.copyFile("../program/WFHttpApi/WFHttpApi.jar", root+"/WFHttpApi/WFHttpApi.jar", false);
		Stream.copyFile("../program/WFServerConfig/WFServerConfig.jar", root+"/WFServerConfig/WFServerConfig.jar", false);
		
		// scripts
		Stream.writeToFile("localhost", new File(root, "WFAllocManagerLocal/info/server/ip.dat"));
		Stream.writeToFile("@echo off\ntitle WFAllocManagerLocal\npushd %~dp0\njava -Xmx192M -jar WFAllocManager.jar\npause\n", new File(root, "WFAllocManagerLocal/WFAllocManager.cmd"));
		Stream.writeToFile("java -Xmx192M -jar WFAllocManager.jar\n", new File(root, "WFAllocManagerLocal/alloc.sh"));
		FileUtils.copyFolderProgressed("scripts", root+"/WFAllocManagerLocal/scripts", true, null);
		Stream.copyFile("scripts/WFAllocInitLocal.txt", root+"/WFAllocManagerLocal/scripts/WFAllocInit.txt", false);
		
		Stream.writeToFile("title WFServerConfig\npushd %~dp0\nstart javaw -jar WFServerConfig.jar\nexit\n", new File(root, "WFServerConfig/WFServerConfig.cmd"));
		Stream.copyFile("config/DS.cfg.dynamictemplate", root+"/WFServerConfig/config/DS.cfg.dynamictemplate", false);
		
		Stream.writeToFile("java -Xmx32M -jar WFHttpApi.jar -port 80\n", new File(root, "WFHttpApi/wfhttpapi.sh"));
		
		// server version of allocator
		FileUtils.copyFolderProgressed(root+"/WFAllocManagerLocal", root+"/WFAllocManager", true, null);
		Stream.copyFile("scripts/WFAllocInitServer.txt", root+"/WFAllocManager/scripts/WFAllocInit.txt", false);
		new File(root+"/WFAllocManager/scripts/WFAllocInitLocal.txt").delete();
		new File(root+"/WFAllocManager/scripts/WFAllocInitServer.txt").delete();
		FileUtils.deleteIn(root+"/WFAllocManager/info/server", true);
		
		// delete files copied from local variant not needed for server variant
		new File(root+"/WFAllocManagerLocal/scripts/WFAllocInitLocal.txt").delete();
		new File(root+"/WFAllocManagerLocal/scripts/WFAllocInitServer.txt").delete();
		new File(root+"/WFAllocManager/info/WFLocalToRemoteAllocatorHandlerPublic").delete();
		
		// keys
		KeyGen.generate(root+"/WFAllocManagerLocal/info", "WFServer", "WFAllocationManager", null);
		// keep WFRemoteFromLocalAllocatorHandler private if you use it as a publicly available allocator
		KeyGen.generate(root, "WFAllocManagerLocal/info/WFLocalToRemoteAllocatorHandler", "WFAllocManager/info/WFRemoteFromLocalAllocatorHandler", null);
	}
	
	public static void main(String[] args) throws Exception {
		build("E:\\release");
	}
}
