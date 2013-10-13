package com.java_podio.code_gen.static_interface;

import com.podio.BaseAPI;

public class TestPodioInterface extends GenericPodioImpl {

    public TestPodioInterface() {
	// TODO Auto-generated constructor stub
    }

    @Override
    protected <T extends BaseAPI> T getAPI(Integer appId, Class<T> type) {
	return null;
    }

}
