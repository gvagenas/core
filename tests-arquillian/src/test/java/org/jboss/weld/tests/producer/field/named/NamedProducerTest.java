/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.tests.producer.field.named;

import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * <p>Check what happens when session.invalidate() is called.</p>
 * 
 * @author Pete Muir
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
@Run(RunModeType.AS_CLIENT)
public class NamedProducerTest
{
   @Deployment
   public static WebArchive createDeployment() 
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
               .addClasses(User.class, NewUserAction.class, Employee.class, SaveAction.class)
               .addWebResource(NamedProducerTest.class.getPackage(), "web.xml", "web.xml")
               .addWebResource(NamedProducerTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
               .addResource(NamedProducerTest.class.getPackage(), "view.xhtml", "view.xhtml")
               .addResource(NamedProducerTest.class.getPackage(), "home.xhtml", "home.xhtml")
               .addWebResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   /*
    * description = "forum post"
    */
   @Test
   public void testNamedProducerWorks() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(false);
      
      HtmlPage page = client.getPage(getPath("/view.jsf"));
      // Check the page rendered ok
      Assert.assertNotNull(getFirstMatchingElement(page, HtmlSubmitInput.class, "saveButton"));
   }
   
   /*
    * description = "WELD-404"
    */
   @Test
   public void testNamedProducerFieldLoosesValues() throws Exception
   {
      WebClient client = new WebClient();
      
      HtmlPage page = client.getPage(getPath("/home.jsf"));
      // Check the page rendered ok
      HtmlSubmitInput saveButton = getFirstMatchingElement(page, HtmlSubmitInput.class, "saveButton");
      HtmlTextInput employeeFieldName = getFirstMatchingElement(page, HtmlTextInput.class, "employeeFieldName");
      HtmlTextInput employeeMethodName = getFirstMatchingElement(page, HtmlTextInput.class, "employeeMethodName");
      
      Assert.assertNotNull(employeeFieldName);
      Assert.assertNotNull(employeeMethodName);
      Assert.assertNotNull(saveButton);
      
      employeeFieldName.setValueAttribute("Pete");
      employeeMethodName.setValueAttribute("Gavin");
      saveButton.click();
   }
   
   protected String getPath(String page)
   {
      // TODO: this should be moved out and be handled by Arquillian
      return "http://localhost:8080/test/" + page;
   }

   protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass)
   {
     Set<T> result = new HashSet<T>();
     
     for (HtmlElement element : rootElement.getAllHtmlChildElements())
     {
        result.addAll(getElements(element, elementClass));
     }
     
     if (elementClass.isInstance(rootElement))
     {
        result.add(elementClass.cast(rootElement));
     }
     return result;
     
   }
 
   protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id)
   {
     Set<T> inputs = getElements(page.getBody(), elementClass);
     for (T input : inputs)
     {
         if (input.getId().contains(id))
         {
            return input;
         }
     }
     return null;
   }
}