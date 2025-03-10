package com.revilleza.google.contacts.controller;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class LoginController {
    private static String authorizationRequestBaseUri
            = "oauth2/authorization";
    Map<String, String> oauth2AuthenticationUrls = Maps.newHashMap();
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/login")
    public String loginPage(Model model) {
        System.out.println("==== LoginController: I was here");
        Iterable<ClientRegistration> clientRegistrations = null;
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
        if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }

        clientRegistrations.forEach(clientRegistration ->
                oauth2AuthenticationUrls.put(clientRegistration.getClientName(),
                        authorizationRequestBaseUri + "/" + clientRegistration.getRegistrationId()));
        System.out.println("LoginController: " + oauth2AuthenticationUrls);
        model.addAttribute("urls", oauth2AuthenticationUrls);
        return "components/login";
    }
}
