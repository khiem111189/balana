/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.balana.samples.image.filtering;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

/**
 * Web page image filtering sample
 */
public class HealthCareAccessControl {

	private static Balana balana;

	public static void main(String[] args) throws IOException {

		initBalana();

		List<String> requestList = new ArrayList<>();
		String folder = "E:\\Workspace\\Thesis\\openaz-work\\openaz-xacml-test\\src\\test\\resources\\testsets\\healthcare\\xml-requests";
		File file = new File(folder);

		for (File child : file.listFiles()) {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(child));
			for (Iterator<String> itor = br.lines().iterator(); itor.hasNext();) {
				String line = itor.next();
				sb.append(line).append("\n");
			}
			requestList.add(sb.toString());
			br.close();
		}
		
		for (String request : requestList) {
			PDP pdp = getPDPNewInstance();

//			System.out.println("\n======================== XACML Request ====================");
//			System.out.println(request);
//			System.out.println("===========================================================");

			String response = pdp.evaluate(request);

			System.out.println("\n======================== XACML Response ===================");
			System.out.println(response);
			System.out.println("===========================================================");
		}

	}

	private static void initBalana() {

		// using file based policy repository. so set the policy location as system
		// property
		String policyLocation = "E:\\Workspace\\Thesis\\Research\\alfa-project\\alfa-project\\src-gen\\health_care.HealthCarePsPatientRecord.xml";
		System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
		// create default instance of Balana
		balana = Balana.getInstance();
	}

	/**
	 * Returns a new PDP instance with new XACML policies
	 *
	 * @return a PDP instance
	 */
	private static PDP getPDPNewInstance() {

		PDPConfig pdpConfig = balana.getPdpConfig();

		// registering new attribute finder. so default PDPConfig is needed to change
		AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
		List<AttributeFinderModule> finderModules = attributeFinder.getModules();
		finderModules.add(new HeathCareAttributeFinderModule());
		attributeFinder.setModules(finderModules);

		return new PDP(new PDPConfig(attributeFinder, pdpConfig.getPolicyFinder(), null, true));
	}

	/**
	 * Creates DOM representation of the XACML request
	 *
	 * @param response
	 *          XACML request as a String object
	 * @return XACML request as a DOM element
	 */
	public static Element getXacmlResponse(String response) {

		ByteArrayInputStream inputStream;
		DocumentBuilderFactory dbf;
		Document doc;

		inputStream = new ByteArrayInputStream(response.getBytes());
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		try {
			doc = dbf.newDocumentBuilder().parse(inputStream);
		} catch (Exception e) {
			System.err.println("DOM of request element can not be created from String");
			return null;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				System.err.println("Error in closing input stream of XACML response");
			}
		}
		return doc.getDocumentElement();
	}

}
