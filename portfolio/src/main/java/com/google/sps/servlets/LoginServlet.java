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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.UserStatus;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  public static final Gson GSON = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

      String loginMessage = "Hello! Log out <a href=\"" + logoutUrl + "\">here</a>.";
      UserStatus userStatus = new UserStatus(userService.isUserLoggedIn(), loginMessage);
      response.getWriter().println(GSON.toJson(userStatus));
    } else {
      String urlToRedirectToAfterUserLogsIn = "/comments.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      String loginMessage =
          "You must be logged in to post comments. Log in <a href=\"" + loginUrl + "\">here</a>.";
      UserStatus userStatus = new UserStatus(userService.isUserLoggedIn(), loginMessage);
      response.getWriter().println(GSON.toJson(userStatus));
    }
  }
}
