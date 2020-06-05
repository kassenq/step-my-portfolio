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
import com.google.sps.data.Keys;
import java.io.IOException;
import java.lang.Integer;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Keys.COMMENT_KIND).addSort(Keys.TIMESTAMP, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Loop over entities.
    List<Comment> comments = new ArrayList<>();
    int max = Integer.parseInt(request.getParameter(Keys.MAX));
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty(Keys.NAME);
      String text = (String) entity.getProperty(Keys.TEXT);
      long timestamp = (long) entity.getProperty(Keys.TIMESTAMP);
      comments.add(new Comment(name, text, timestamp));
      if (comments.size() == max) {
        break;
      }
    }

    response.setContentType("application/json;");
    response.getWriter().println(Keys.GSON.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, Keys.NAME_INPUT, Keys.EMPTY_STRING);
    String text = getParameter(request, Keys.TEXT_INPUT, Keys.EMPTY_STRING);
    long timestamp = System.currentTimeMillis();

    // Create new Entity with kind Comment and set properties with keys and values.
    Entity commentEntity = new Entity(Keys.COMMENT_KIND);
    commentEntity.setProperty(Keys.NAME, name);
    commentEntity.setProperty(Keys.TEXT, text);
    commentEntity.setProperty(Keys.TIMESTAMP, timestamp);

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
