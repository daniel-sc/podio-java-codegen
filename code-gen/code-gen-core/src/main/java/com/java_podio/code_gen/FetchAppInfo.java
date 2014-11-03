package com.java_podio.code_gen;

import java.util.ArrayList;
import java.util.List;

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

	private final OAuthUserCredentials credentials;

	public FetchAppInfo(OAuthUserCredentials credentials) {
		this.credentials = credentials;
	}
	
	public List<Application> fetchAppsForSpace(Integer spaceId) {
		List<Application> result = new ArrayList<Application>();
		AppAPI spaceAPI = getAPI(spaceId, AppAPI.class, credentials);
		List<Application> apps = spaceAPI.getAppsOnSpace(spaceId);
		for(Application appMini : apps) {
			result.add(fetchApp(appMini.getId()));
		}
		return result;
	}

	/**
	 * This is a possibly long running REST call!
	 * 
	 * @return
	 */
	public Application fetchApp(Integer appId) {
		AppAPI appAPI = getAPI(appId, AppAPI.class, credentials);
		Application app = appAPI.getApp(appId);
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
	public static synchronized <T extends BaseAPI> T getAPI(Integer appId, Class<T> type, OAuthUserCredentials usercredentials) {
		return getBaseAPI(usercredentials).getAPI(type);
	}

}
