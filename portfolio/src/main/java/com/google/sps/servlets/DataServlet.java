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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
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
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final String dataKind = "Comment";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<String> comments = new ArrayList<>();
    Query query = new Query(dataKind).addSort("timestamp", SortDirection.ASCENDING);;
    PreparedQuery results = datastore.prepare(query);
    
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("name");
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");
      comments.add(getCommentStatement(name, comment, timestamp));
    }

    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = getParameter(request, "name", "anonymous");
    String comment = getParameter(request, "comment", "");
    long timestamp = System.currentTimeMillis();

    storeComments(name, comment, timestamp);
    response.sendRedirect("/index.html");
  }

  /**
   * Converts a DataServlet instance into a JSON string using the Gson library.
   */
  private String convertToJson(List<String> data) {
    return new Gson().toJson(data);
  }

  /**
   * @return the desired comment format to display
   */
  private String getCommentStatement(String name, String comment, long timestamp) {
    return "at " + timestamp + ", "  + name + " said: " + comment;
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
  private void storeComments(String name, String comment, long timestamp) {
    Entity taskEntity = new Entity(dataKind);
    taskEntity.setProperty("name", name);
    taskEntity.setProperty("comment", comment);
    taskEntity.setProperty("timestamp", timestamp);
    datastore.put(taskEntity);
  }
}
