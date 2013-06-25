package com.java_podio.code_gen;

import com.podio.app.Application;
import com.podio.oauth.OAuthUsernameCredentials;

public class CodeGenMain {

	public CodeGenMain() {
	}

	/**
	 * @param args
	 *            PODIOUSERNAME PODIOPASSWORD APPID
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: java -jar code-gen.jar PODIOUSERNAME PODIOPASSWORD APPID");
			return;
		}

		String username = args[0];
		String password = args[1];
		Integer appid = Integer.parseInt(args[2]);

		OAuthUsernameCredentials usercredentials = new OAuthUsernameCredentials(username, password);
		
		FetchAppInfo appInfo = new FetchAppInfo(appid, usercredentials);
		Application app = appInfo.fetch();
		
		CodeGenerator codeGen = new CodeGenerator();
		codeGen.generateCode(app);
	}

}
