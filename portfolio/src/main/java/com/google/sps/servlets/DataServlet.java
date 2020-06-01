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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private List<String> facts;

  @Override
  public void init() {
    facts = new ArrayList<>();
    facts.add(
        "My favorite color is green. "
            + "I also like light blue.");
    facts.add("My parents made up my name based on my Chinese one.");
    facts.add("My birthday is on Halloween.");
    facts.add("I have the best younger brother!");
    facts.add("My favorite food is pasta.");
    facts.add("I'm a cat person. I like dogs, but I like cats more.");
    facts.add(
        "I didn't have any experience with "
            + "computer science in high school.");
    facts.add("I play the acoustic guitar.");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String fact = facts.get((int) (Math.random() * facts.size()));
    response.setContentType("text/html;");
    response.getWriter().println(fact);
  }
}
