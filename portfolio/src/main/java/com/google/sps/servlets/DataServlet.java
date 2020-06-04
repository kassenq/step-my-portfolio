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
import com.google.sps.data.Comment;
import java.io.IOException;
import java.lang.Integer;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  static final Gson GSON = new Gson();
  static final String NAME = "name";
  static final String TEXT = "text";
  static final String TIMESTAMP = "timestamp";
  static final String NAME_INPUT = "name-input";
  static final String TEXT_INPUT = "text-input";
  static final String EMPTY_STRING = "";
  static final String COMMENT_KIND = "Comment";
  static final String MAX = "max";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(COMMENT_KIND).addSort(TIMESTAMP, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Loop over entities.
    List<Comment> comments = new ArrayList<>();
    int max = Integer.parseInt(request.getParameter(MAX));
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty(NAME);
      String text = (String) entity.getProperty(TEXT);
      long timestamp = (long) entity.getProperty(TIMESTAMP);
      comments.add(new Comment(name, text, timestamp));
      if (comments.size() == max) {
        break;
      }
    }

    response.setContentType("application/json;");
    response.getWriter().println(GSON.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, NAME_INPUT, EMPTY_STRING);
    String text = getParameter(request, TEXT_INPUT, EMPTY_STRING);
    long timestamp = System.currentTimeMillis();

    // Create new Entity with kind Comment and set properties with keys and values.
    Entity commentEntity = new Entity(COMMENT_KIND);
    commentEntity.setProperty(NAME, name);
    commentEntity.setProperty(TEXT, text);
    commentEntity.setProperty(TIMESTAMP, timestamp);

    // Create instance of DatastoreService class and store entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Respond with the result.
    response.sendRedirect("/comments.html");
  }
  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client.
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
