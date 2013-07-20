package com.java_podio.code_gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.podio.app.Application;
import com.podio.oauth.OAuthUsernameCredentials;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;

public class CodeGenMain {

    public CodeGenMain() {
    }

    /**
     * @param args
     *            PODIOUSERNAME PODIOPASSWORD APPID_1 .. APPID_N
     * @throws JClassAlreadyExistsException
     * @throws IOException
     */
    public static void main(String[] args) throws JClassAlreadyExistsException,
	    IOException {
	if (args.length < 3) {
	    System.out
		    .println("Usage: java -jar code-gen.jar PODIOUSERNAME PODIOPASSWORD APPID_1 .. APPID_N");
	    System.out
		    .println("       java -jar code-gen.jar PODIOUSERNAME PODIOPASSWORD -space SPACEID");
	    return;
	}

	String username = args[0];
	String password = args[1];

	if ("-space".equalsIgnoreCase(args[2])) {
	    generateSpace(username, password, Integer.parseInt(args[3]),
		    new File("."), "podio.generated", "UTF-8");
	} else {
	    List<Integer> appIds = new ArrayList<Integer>();
	    for (int i = 2; i < args.length; i++) {
		appIds.add(Integer.parseInt(args[i]));
	    }
	    generateApps(username, password, appIds, new File("."),
		    "podio.generated", "UTF-8");
	}
    }

    /**
     * Generates code for all apps in a space.
     * 
     * @param user
     * @param password
     * @param spaceId
     * @param outputFolder
     * @param basePackage
     * @param encoding
     *            encoding of generated soureces - e.g. "UTF-8"
     * @throws JClassAlreadyExistsException
     * @throws IOException
     */
    public static void generateSpace(String user, String password,
	    Integer spaceId, File outputFolder, String basePackage,
	    String encoding) throws JClassAlreadyExistsException, IOException {
	OAuthUsernameCredentials usercredentials = new OAuthUsernameCredentials(
		user, password);

	FetchAppInfo appInfo = new FetchAppInfo(usercredentials);
	List<Application> appInfos = new ArrayList<Application>();
	appInfos.addAll(appInfo.fetchAppsForSpace(spaceId));

	generateSources(outputFolder, basePackage, appInfos, encoding);
    }

    /**
     * Generates code for all apps defined by {@code appIds}.
     * 
     * @param user
     * @param password
     * @param appIds
     * @param outputFolder
     * @param basePackage
     * @param encoding
     *            encoding of generated soureces - e.g. "UTF-8"
     * @throws JClassAlreadyExistsException
     * @throws IOException
     */
    public static void generateApps(String user, String password,
	    List<Integer> appIds, File outputFolder, String basePackage,
	    String encoding) throws JClassAlreadyExistsException, IOException {
	OAuthUsernameCredentials usercredentials = new OAuthUsernameCredentials(
		user, password);

	FetchAppInfo appInfo = new FetchAppInfo(usercredentials);
	List<Application> appInfos = new ArrayList<Application>();

	// fetch app info:
	// TODO fetching can be parallelized for speed optimization:
	for (Integer appId : appIds) {
	    appInfos.add(appInfo.fetchApp(appId));
	}

	generateSources(outputFolder, basePackage, appInfos, encoding);
    }

    /**
     * @param outputFolder
     * @param basePackage
     * @param appInfos
     * @param encoding
     *            encoding of output files - e.g. "UTF-8"
     * @throws JClassAlreadyExistsException
     * @throws IOException
     */
    static void generateSources(File outputFolder, String basePackage,
	    List<Application> appInfos, String encoding)
	    throws JClassAlreadyExistsException, IOException {
	// generate code:
	CodeGenerator codeGen = new CodeGenerator(basePackage);
	JCodeModel jCodeModel = codeGen.generateCode(appInfos);
	FileCodeWriter writer = new FileCodeWriter(outputFolder, encoding);
	jCodeModel.build(writer);
    }

}
