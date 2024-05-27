package com.example.FrontEnd;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MyWebService {

    @Value("${backEndURL}")
    String backEndURL;

    @RequestMapping(path = "/hello", method = RequestMethod.GET)
    public String hello(){
        try{
            RestTemplate restTemplate = new RestTemplate();
            String s = restTemplate.getForObject(backEndURL, String.class);
            return "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css'>" +
                    "<title>Users</title>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h2 class='mt-5'>Response from the SQL</h2>" +
                    "<table class='table table-bordered mt-3'>" +
                    "<thead>" +
                    "<tr>" +
                    "<th>ID</th>" +
                    "<th>Name</th>" +
                    "</tr>" +
                    "</thead>" +
                    "<tbody>" +
                    s +
                    "</tbody>" +
                    "</table>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
        }catch (Exception e){
            return e.getLocalizedMessage();
        }
    }
}
