package com.java_podio.code_gen;

import com.podio.APIFactory;
import com.podio.BaseAPI;
import com.podio.ResourceFactory;
import com.podio.app.AppAPI;
import com.podio.app.Application;
import com.podio.oauth.OAuthClientCredentials;
import com.podio.oauth.OAuthUserCredentials;

/**
 * Fetches all relevant Infos for a given app.
 */
public class FetchAppInfo {

	private Integer appId;

	private AppAPI appAPI;

	public FetchAppInfo(Integer appId, OAuthUserCredentials credentials) {
		this.appId = appId;
		appAPI = getAPI(appId, AppAPI.class, credentials);
	}

	/**
	 * This is a possibly long running REST call!
	 * 
	 * @return
	 */
	public Application fetch() {
		Application app = appAPI.getApp(appId);
		
//		//DEBUG:
//		for(ApplicationField field : app.getFields()) {
//			ApplicationField f = appAPI.getField(appId, field.getId());
//			CodeGenerator.printAppField(f);
//		}
		return app;
	}

	private static APIFactory getBaseAPI(OAuthUserCredentials credentials) {
		ResourceFactory resourceFactory = new ResourceFactory(new OAuthClientCredentials("java-podio-code-gen", "XivCmwrnRlTgbFhqIlGe2k6NrZJFzUIbkIcdVFy867aqvQ2iQx2v2x7zuSvhePJD"), credentials);
		return new APIFactory(resourceFactory);
	}

	/**
	 * @param appId
	 * @param type
	 *            api type
	 * @param usercredentials
	 * @return
	 */
	public static <T extends BaseAPI> T getAPI(Integer appId, Class<T> type, OAuthUserCredentials usercredentials) {
		return getBaseAPI(usercredentials).getAPI(type);
	}

}
