/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.flexible.pubsub;

import com.google.gson.*;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Push with PubSub", value = "/pubsub/push")
public class PubSubPush extends HttpServlet {

  private final MessageRepository messageRepository = MessageRepository.getInstance();
  private final Gson gson = new Gson();
  private final JsonParser jsonParser = new JsonParser();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    String pubsubVerificationToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
    // Do not process message if request token does not match pubsubVerificationToken
    if (req.getParameter("token").compareTo(pubsubVerificationToken) != 0) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    Message message = getMessage(req);
    try {
      messageRepository.save(message);
      // 200, 201, 204, 102 status codes are interpreted as success by the Pub/Sub system
      resp.setStatus(200);
    } catch (Exception e) {
      resp.setStatus(500);
    }
  }

  private Message getMessage(HttpServletRequest request) throws IOException {
    String requestBody = request.getReader().lines().collect(Collectors.joining("\n"));
    JsonElement jsonRoot = jsonParser.parse(requestBody);
    String message = jsonRoot.getAsJsonObject().get("message").toString();
    return gson.fromJson(message, Message.class);
  }
}