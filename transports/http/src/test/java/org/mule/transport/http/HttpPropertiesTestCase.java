/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.HashMap;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

public class HttpPropertiesTestCase extends FunctionalTestCase
{
    @Rule
    public DynamicPort dynamicPort = new DynamicPort("port1");

    @Override
    protected String getConfigFile()
    {
        return "http-properties-conf.xml";
    }

    @Test
    public void testPropertiesGetMethod() throws Exception
    {
        HttpGet httpGet = new HttpGet("http://localhost:" + dynamicPort.getNumber() + "/resources/client?id=1");
        CloseableHttpResponse response = HttpClients.createMinimal().execute(httpGet);
        assertEquals("Retrieving client with id = 1", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testPropertiesPostMethod() throws Exception
    {
        MuleClient client = muleContext.getClient();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("Content-Type","application/x-www-form-urlencoded");

        MuleMessage response = client.send("http://localhost:" + dynamicPort.getNumber() + "/resources/client", "name=John&lastname=Galt", properties);

        assertNotNull(response);
        assertEquals("client", response.getInboundProperty("http.relative.path"));
        assertEquals("http://localhost:" + dynamicPort.getNumber() + "/resources", response.getInboundProperty("http.context.uri"));
        assertEquals("Storing client with name = John and lastname = Galt", response.getPayloadAsString());
    }

    @Test
    public void testRedirectionWithRelativeProperty() throws Exception
    {
        MuleClient client = muleContext.getClient();
        MuleMessage response = client.send("http://localhost:" + dynamicPort.getNumber() + "/redirect/products?retrieve=all&order=desc", TEST_MESSAGE, null);
        assertEquals("Successfully redirected: products?retrieve=all&order=desc", response.getPayloadAsString());
    }
}
