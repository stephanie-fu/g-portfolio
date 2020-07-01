// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles user comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private List<String> comments = new ArrayList<>();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, "name", "anonymous");
    String comment = getParameter(request, "comment", "");

    // Respond with a refresh and do local and persistent storage updates.
    comments.add(name + " says: " + comment);
    storeComments(name, comment);
    
    response.sendRedirect("/index.html");
  }

  /**
   * Converts a DataServlet instance into a JSON string using the Gson library.
   */
  private String convertToJson(List<String> data) {
    return new Gson().toJson(data);
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Stores user comments in a form of persistent storage.
   */
  private void storeComments(String name, String comment) {
    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("name", name);
    taskEntity.setProperty("comment", comment);
    datastore.put(taskEntity);
  }
}
