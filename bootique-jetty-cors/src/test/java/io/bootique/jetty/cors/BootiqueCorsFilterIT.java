/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.jetty.cors;

import io.bootique.test.junit.BQTestFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

public class BootiqueCorsFilterIT {

    private static final String RESTRICTED_HEADERS_PROP = "sun.net.http.allowRestrictedHeaders";
    private static String ORIGINAL_RESTRICTED_HEADERS;

    @Rule
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @BeforeClass
    public static void configJavaForCors() {
        ORIGINAL_RESTRICTED_HEADERS = System.getProperty(RESTRICTED_HEADERS_PROP);

        // This is to allow "Origin" header to be passed through. See:
        // https://stackoverflow.com/questions/13255051/setting-origin-and-access-control-request-method-headers-with-jersey-client
        System.setProperty(RESTRICTED_HEADERS_PROP, "true");
    }

    @AfterClass
    public static void undoConfigJavaForCors() {

        String restore = ORIGINAL_RESTRICTED_HEADERS != null ? ORIGINAL_RESTRICTED_HEADERS : "false";
        System.setProperty(RESTRICTED_HEADERS_PROP, restore);
    }

    @Test
    public void testResponseHeaders_DefaultConfig() {
        testFactory
                .app("-s", "-c", "classpath:io/bootique/jetty/cors/NoConfigCorsFilter.yml")
                .module(JettyCorsModule.class)
                .run();

        // by default matches all paths and all origins

        WebTarget target = ClientBuilder.newClient().target("http://localhost:15001/xp");
        Response r = target.request().header("Origin", "xo").options();
        assertEquals("xo", r.getHeaderString("Access-Control-Allow-Origin"));
    }

    @Test
    public void testResponseHeaders_ByOrigin() {
        testFactory
                .app("-s", "-c", "classpath:io/bootique/jetty/cors/CorsFilter.yml")
                .module(JettyCorsModule.class)
                .run();

        WebTarget target = ClientBuilder.newClient().target("http://localhost:15001/api");

        Response r1 = target.request().header("Origin", "test").options();
        assertEquals("test", r1.getHeaderString("Access-Control-Allow-Origin"));

        Response r2 = target.request().header("Origin", "test2").options();
        assertNull(r2.getHeaderString("Access-Control-Allow-Origin"));

        Response r3 = target.request().options();
        assertNull(r3.getHeaderString("Access-Control-Allow-Origin"));
    }

    @Test
    public void testResponseHeaders_ByPath() {
        testFactory
                .app("-s", "-c", "classpath:io/bootique/jetty/cors/CorsFilter.yml")
                .module(JettyCorsModule.class)
                .run();

        WebTarget coveredPath = ClientBuilder.newClient().target("http://localhost:15001/api");

        Response r1 = coveredPath.request().header("Origin", "test").options();
        assertEquals("test", r1.getHeaderString("Access-Control-Allow-Origin"));


        WebTarget notCoveredPath = ClientBuilder.newClient().target("http://localhost:15001/notapi");
        Response r2 = notCoveredPath.request().header("Origin", "test").options();
        assertNull(r2.getHeaderString("Access-Control-Allow-Origin"));
    }
}
