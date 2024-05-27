package com.example.myservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MyServiceRest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public String getUsers() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM users");
        StringBuilder result = new StringBuilder();

        result.append("<html>");
        result.append("<head>");
        result.append("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css\">");
        result.append("</head>");
        result.append("<body>");
        result.append("<div class=\"container\">");
        result.append("<h1 class=\"mt-5\">Response from the SQL</h1>");
        result.append("<table class=\"table table-striped mt-3\">");
        result.append("<thead><tr><th>ID</th><th>Name</th></tr></thead>");
        result.append("<tbody>");

        for (Map<String, Object> user : users) {
            result.append("<tr>");
            result.append("<td>").append(user.get("ID")).append("</td>");
            result.append("<td>").append(user.get("Name")).append("</td>");
            result.append("</tr>");
        }

        result.append("</tbody>");
        result.append("</table>");
        result.append("</div>");
        result.append("</body>");
        result.append("</html>");

        return result.toString();
    }
}
