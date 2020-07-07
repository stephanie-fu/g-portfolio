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
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final String ENTITY_KIND = "Comment";
  private static final String ENTITY_NAME_HEADER = "name";
  private static final String ENTITY_EMAIL_HEADER = "email";
  private static final String ENTITY_COMMENT_HEADER = "comment";
  private static final String ENTITY_TIMESTAMP_HEADER = "timestamp";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<String> comments = new ArrayList<>();
    Query query = new Query(ENTITY_KIND).addSort(ENTITY_TIMESTAMP_HEADER, SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      comments.add(buildComment(entity));
    }

    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, ENTITY_NAME_HEADER, /* DefaultValue= */ "anonymous");
    String email = '';
    String comment = getParameter(request, ENTITY_COMMENT_HEADER, /* DefaultValue= */ "");
    long timestamp = System.currentTimeMillis();

    // Respond with a refresh and do local and persistent storage updates.
    storeComments(name, email, comment, timestamp);
    response.sendRedirect("/index.html");
  }

  private static String buildComment(Entity entity) {
    String name = (String) entity.getProperty(ENTITY_NAME_HEADER);
    String email = (String) entity.getProperty(ENTITY_EMAIL_HEADER);
    String comment = (String) entity.getProperty(ENTITY_COMMENT_HEADER);
    long timestamp = (long) entity.getProperty(ENTITY_TIMESTAMP_HEADER);
    return String.format("at %d, %s (%s) said: %s", timestamp, name, email, comment);
  }

  private static String convertToJson(List<String> data) {
    return new Gson().toJson(data);
  }
  
  private static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  private static void storeComments(String name, String email, String comment, long timestamp) {
    Entity taskEntity = new Entity(ENTITY_KIND);
    taskEntity.setProperty(ENTITY_NAME_HEADER, name);
    taskEntity.setProperty(ENTITY_EMAIL_HEADER, email);
    taskEntity.setProperty(ENTITY_COMMENT_HEADER, comment);
    taskEntity.setProperty(ENTITY_TIMESTAMP_HEADER, timestamp);
    datastore.put(taskEntity);
  }
}
