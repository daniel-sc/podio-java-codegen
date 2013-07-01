package com.podio.codegen;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.java_podio.code_gen.CodeGenMain;
import com.sun.codemodel.JClassAlreadyExistsException;


@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class PodioCodeGenMojo extends AbstractMojo {
	
	@Parameter(required=true)
	private String user;
	
	@Parameter(required=true)
	private String password;
	
	@Parameter
	private Integer spaceId;
	
	@Parameter
	private List<Integer> appIds;
	
	@Parameter(defaultValue="podio.generated")
	private String basePackage;
	
	@Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
	private File outputDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		getLog().debug("user="+user);
		getLog().debug("password="+password);
		getLog().debug("spaceId="+spaceId);
		getLog().debug("appIds="+appIds);
		getLog().debug("basePackage="+basePackage);
		getLog().debug("outputDirectory="+outputDirectory.toString());
		
		try {
			if(spaceId!=null && spaceId>0) {
				CodeGenMain.generateSpace(user, password, spaceId, outputDirectory, basePackage);
			} else if (appIds!=null && appIds.size()>0 ) {
				CodeGenMain.generateApps(user, password, appIds, outputDirectory, basePackage);
			} else {
				throw new MojoFailureException("Must provide either spaceId or appIds!");
			}
		} catch (JClassAlreadyExistsException e) {
			throw new MojoExecutionException("Error: duplicate classes!", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Error: could not write classes!", e);
		}
	}
}
