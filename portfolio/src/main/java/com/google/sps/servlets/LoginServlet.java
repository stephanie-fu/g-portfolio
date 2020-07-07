package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles user logins. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    PrintWriter out = response.getWriter();
    out.println("<h1>Login Status</h1>");

    // Only logged-in users can see the form
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/login");
      out.println(String.format("<p>Hello %s!</p>", userService.getCurrentUser().getEmail()));
      out.println("<p>You are logged in.</p>");
      out.println(String.format("<p>Logout <a href=\"%s\">here</a>.</p>", logoutUrl));
    } else {
      String loginUrl = userService.createLoginURL("/login");
      out.println("<p>Hello stranger!</p>");
      out.println("<p>You are not logged in.</p>");
      out.println(String.format("<p>Login <a href=\"%s\">here</a>.</p>", loginUrl));
    }
  }
}
