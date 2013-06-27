package com.java_podio.code_gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.podio.app.Application;
import com.podio.oauth.OAuthUsernameCredentials;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;

public class CodeGenMain {

	public CodeGenMain() {
	}

	/**
	 * @param args
	 *            PODIOUSERNAME PODIOPASSWORD APPID_1 .. APPID_N
	 * @throws JClassAlreadyExistsException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws JClassAlreadyExistsException, IOException {
		if (args.length < 3) {
			System.out.println("Usage: java -jar code-gen.jar PODIOUSERNAME PODIOPASSWORD APPID_1 .. APPID_N");
			return;
		}

		String username = args[0];
		String password = args[1];
		OAuthUsernameCredentials usercredentials = new OAuthUsernameCredentials(username, password);
		
		List<Integer> appIds = new ArrayList<Integer>();
		for(int i=2; i<args.length; i++) {
			appIds.add(Integer.parseInt(args[i]));
		}

		//fetch app info:
		//TODO fetching can be parallelized for speed optimization:
		List<Application> appInfos = new ArrayList<Application>();
		for(Integer appId : appIds) {
			FetchAppInfo appInfo = new FetchAppInfo(appId, usercredentials);
			appInfos.add(appInfo.fetch());
		}
		
		//generate code:
		CodeGenerator codeGen = new CodeGenerator("podio.generated");
		JCodeModel jCodeModel = codeGen.generateCode(appInfos);
		jCodeModel.build(new File("."));
	}

}
