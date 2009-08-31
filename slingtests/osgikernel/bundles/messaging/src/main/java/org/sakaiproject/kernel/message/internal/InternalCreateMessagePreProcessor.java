/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.kernel.message.internal;

import org.apache.sling.api.SlingHttpServletRequest;
import org.sakaiproject.kernel.api.message.CreateMessagePreProcessor;
import org.sakaiproject.kernel.api.message.MessageConstants;
import org.sakaiproject.kernel.api.message.MessagingException;

import javax.servlet.http.HttpServletResponse;


/**
 * Checks if the message the user wants to create has all the right properties on it.
 * 
 * @scr.component immediate="true" label="InternalCreateMessagePreProcessor"
 *                description="Checks request for Internal messages"
 * @scr.property name="service.vendor" value="The Sakai Foundation"
 * @scr.property name="sakai.message.createpreprocessor" value="internal"
 * @scr.service interface="org.sakaiproject.kernel.api.message.CreateMessagePreProcessor"
 */
public class InternalCreateMessagePreProcessor implements CreateMessagePreProcessor {

  // Constructor so we can use this anywhere else but not trough src.
  // We do this because it is the default preprocessor.
  public InternalCreateMessagePreProcessor() {
  }

  public void checkRequest(SlingHttpServletRequest request)
      throws MessagingException {
    if (request.getRequestParameter(MessageConstants.PROP_SAKAI_TO) == null) {
      throw new MessagingException(HttpServletResponse.SC_BAD_REQUEST, "The "
          + MessageConstants.PROP_SAKAI_TO + " parameter has to be specified.");
    }
  }

  public String getType() {
    return MessageConstants.TYPE_INTERNAL;
  }

}
