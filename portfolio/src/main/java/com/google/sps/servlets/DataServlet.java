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
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/** Servlet that handles user comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final Translate translate = TranslateOptions.getDefaultInstance().getService();
  private static final UserService userService = UserServiceFactory.getUserService();
  private static final String ENTITY_KIND = "Comment";
  private static final String ENTITY_NAME_HEADER = "name";
  private static final String ENTITY_EMAIL_HEADER = "email";
  private static final String ENTITY_COMMENT_HEADER = "comment";
  private static final String ENTITY_SENTIMENT_HEADER = "sentiment";
  private static final String ENTITY_TIMESTAMP_HEADER = "timestamp";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<String> comments = new ArrayList<>();
    Query query = new Query(ENTITY_KIND).addSort(ENTITY_TIMESTAMP_HEADER, SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);
    String sourceLanguageCode = request.getParameter("sourceLanguageCode");
    String targetLanguageCode = request.getParameter("targetLanguageCode");
    for (Entity entity : results.asIterable()) {
      comments.add(buildComment(entity, sourceLanguageCode, targetLanguageCode));
    }
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(convertToJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, ENTITY_NAME_HEADER, /* DefaultValue= */ "anonymous");
    String email = userService.getCurrentUser().getEmail();
    String comment = getParameter(request, ENTITY_COMMENT_HEADER, /* DefaultValue= */ "");
    long timestamp = System.currentTimeMillis();
    Optional<Double> preSentiment = getSentiment(comment);
    Double sentiment = preSentiment.isPresent() ? preSentiment.get() : Double.NaN;

    // Respond with a refresh and do local and persistent storage updates.
    storeComments(name, email, comment, sentiment, timestamp);
    response.sendRedirect("/index.html");
  }
  
  private static String buildComment(Entity entity, String sourceLanguageCode, String targetLanguageCode) {
    String name = (String) entity.getProperty(ENTITY_NAME_HEADER);
    String email = (String) entity.getProperty(ENTITY_EMAIL_HEADER);
    String comment = (String) entity.getProperty(ENTITY_COMMENT_HEADER);
    Double sentiment = (Double) entity.getProperty(ENTITY_SENTIMENT_HEADER);
    if (!sourceLanguageCode.equals(targetLanguageCode)) {
      comment = translateComment(comment, targetLanguageCode);
    }
    long timestamp = (long) entity.getProperty(ENTITY_TIMESTAMP_HEADER);
    String commentDisplay = String.format("at %d, %s (%s) said: %s", timestamp, name, email, comment);
    return !sentiment.isNaN() ? commentDisplay + String.format(" (with sentiment: %f)", (double) sentiment) : 
                                   commentDisplay;
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

  private static Optional<Double> getSentiment(String message) {
    try {
      Document doc =
          Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      double score = sentiment.getScore();
      languageService.close();
      return Optional.of(score);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return Optional.empty();
    }
  }

  private static void storeComments(String name, String email, String comment, Double sentiment, long timestamp) {
    Entity taskEntity = new Entity(ENTITY_KIND);
    taskEntity.setProperty(ENTITY_NAME_HEADER, name);
    taskEntity.setProperty(ENTITY_EMAIL_HEADER, email);
    taskEntity.setProperty(ENTITY_COMMENT_HEADER, comment);
    taskEntity.setProperty(ENTITY_SENTIMENT_HEADER, sentiment);
    taskEntity.setProperty(ENTITY_TIMESTAMP_HEADER, timestamp);
    datastore.put(taskEntity);
  }

  private static String translateComment(String originalText, String targetLanguageCode) {
    try {
      Translation translation =
        translate.translate(originalText, Translate.TranslateOption.targetLanguage(targetLanguageCode));
      return translation.getTranslatedText();
    } catch (TranslateException e) {
      System.err.println(e.getMessage());
      return "Error: Please choose a valid language.";
    }
  }
}
