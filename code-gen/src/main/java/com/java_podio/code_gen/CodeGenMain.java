package com.java_podio.code_gen;

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
		String appid = args[2];

	}

}
