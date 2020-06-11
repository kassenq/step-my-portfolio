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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.sps.data.Keys;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  public static final Gson GSON = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Keys.COMMENT_KIND).addSort(Keys.TIMESTAMP, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    UserService userService = UserServiceFactory.getUserService();

    // Loop over entities.
    List<Comment> comments = new ArrayList<>();
    int max = Integer.parseInt(request.getParameter(Keys.MAX));
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty(Keys.NAME);
      String text = (String) entity.getProperty(Keys.TEXT);
      long timestamp = (long) entity.getProperty(Keys.TIMESTAMP);
      String email = (String) entity.getProperty(Keys.EMAIL);
      String imageUrl = (String) entity.getProperty(Keys.IMAGE);
      comments.add(new Comment(name, text, timestamp, email, imageUrl));
      if (comments.size() == max) {
        break;
      }
    }
    response.setContentType("application/json;");
    // if (!userService.isUserLoggedIn()) {
    //   String urlToRedirectToAfterUserLogsIn = "/comments.html";
    //   String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
    //   String loginMessage = "<p>You must be logged in to post comments. Log in <a href=\""
    //       + loginUrl + "\">here</a>.</p>";
    // }
    response.getWriter().println(GSON.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, Keys.NAME_INPUT, Keys.EMPTY_STRING);
    String text = getParameter(request, Keys.TEXT_INPUT, Keys.EMPTY_STRING);
    long timestamp = System.currentTimeMillis();
    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();
    String imageUrl = getUploadedFileUrl(request, Keys.IMAGE);

    // Create new Entity with kind Comment and set properties with keys and values.
    Entity commentEntity = new Entity(Keys.COMMENT_KIND);
    commentEntity.setProperty(Keys.NAME, name);
    commentEntity.setProperty(Keys.TEXT, text);
    commentEntity.setProperty(Keys.TIMESTAMP, timestamp);
    commentEntity.setProperty(Keys.EMAIL, email);
    commentEntity.setProperty(Keys.IMAGE, imageUrl);

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

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(Keys.IMAGE);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }
}
