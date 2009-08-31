/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.kernel.proxy;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create a HTTP server on the next available port between 8888 and 8988 that will respond
 * with the configured request to any request. Should be run single threaded.
 */
public class DummyServer extends AbstractHandler {

  /**
   * The next body response to send.
   */
  private String responseBody = "Hello";
  /**
   * The server object.
   */
  private Server server;
  /**
   * The port on which the server is listening.
   */
  private int port;
  /**
   * The next content type to respond with.
   */
  private String contentType = "text/plain";
  /**
   * The next status to send.
   */
  private int status = 200;
  /**
   * The last captured request.
   */
  private CapturedRequest request;

  /**
   * Create the dummy server on the next available port 8888 to 8988.
   */
  public DummyServer() {
    port = 8888;
    for (int i = 0; i < 100; i++) {
      try {
        server = new Server(port);
        server.setHandler(this);
        server.start();
        break;
      } catch (Exception ex) {
        port++;
        if (server != null) {
          try {
            server.stop();
            server.destroy();
          } catch (Exception ex2) {
          }
        }
        server = null;
      }
    }
    if (server == null) {
      throw new RuntimeException(
          "Unable to find a free port the range 8888 - 8988, aborting http server startup ");
    }
  }

  /**
   * Close the server down, releasing resources.
   */
  public void close() {
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
      }
      server.destroy();
      server = null;
    }
  }

  /**
   * @return the current URL that the server is listening on.
   */
  public String getUrl() {
    return "http://localhost:" + port + "/test";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.mortbay.jetty.Handler#handle(java.lang.String,
   *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
   *      int)
   */
  public void handle(String target, HttpServletRequest request,
      HttpServletResponse response, int dispatch) throws IOException, ServletException {

    this.request = new CapturedRequest(request);

    response.setContentType(contentType);
    response.setStatus(status);
    response.getWriter().print(responseBody);
    ((Request) request).setHandled(true);
  }

  /**
   * @param contentType
   *          the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * @param responsebody
   *          the responsebody to set
   */
  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  /**
   * @return the request
   */
  public CapturedRequest getRequest() {
    return request;
  }

}
