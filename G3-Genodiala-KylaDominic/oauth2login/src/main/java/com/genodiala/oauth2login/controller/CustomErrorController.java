package com.genodiala.oauth2login.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object type = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "An unexpected error has occurred";
        String errorType = "Unknown Error";
        String requestPath = request.getRequestURI();

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            httpStatus = HttpStatus.valueOf(statusCode);

            switch (statusCode) {
                case 404:
                    errorMessage = "The requested page was not found";
                    errorType = "Not Found";
                    break;
                case 403:
                    errorMessage = "You don't have permission to access this resource";
                    errorType = "Forbidden";
                    break;
                case 500:
                    errorMessage = "Server encountered an internal error";
                    errorType = "Internal Server Error";
                    break;
            }
        }

        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        }

        if (type != null) {
            errorType = type.toString();
        }

        if (path != null) {
            requestPath = path.toString();
        }

        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy");
        String currentTime = now.format(formatter);

        model.addAttribute("status", httpStatus.value());
        model.addAttribute("error", errorType);
        model.addAttribute("message", errorMessage);
        model.addAttribute("timestamp", currentTime);
        model.addAttribute("path", requestPath);

        return "error";
    }
}