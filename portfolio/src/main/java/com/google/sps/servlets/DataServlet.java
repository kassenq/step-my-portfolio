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
import com.google.sps.data.Task;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private ArrayList<String> comments;
  private static final Gson gson = new Gson();

  @Override
  public void init() {
    comments = new ArrayList<>();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Task");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Loop over entities.
    List<Task> tasks = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("text");
      long timestamp = (long) entity.getProperty("timestamp");
      tasks.add(new Task(name, text, timestamp));
    }

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(tasks));
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, "name-input", "");
    String text = getParameter(request, "text-input", "");
    long timestamp = System.currentTimeMillis();

    // Create new Entity with kind Task and set properties with keys and values.
    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("name", name);
    taskEntity.setProperty("text", text);
    taskEntity.setProperty("timestamp", timestamp);

    // Create instance of DatastoreService class and store entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

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
  /**
  * Converts a ServerStats instance into a JSON string using the Gson library. Note: We first added
  * the Gson library dependency to pom.xml.
  */
  private String convertToJsonUsingGson(ArrayList<String> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
