package Delima.com.example.OAuth2demo.config.UserService;



import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContactService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_BASE_URL = "https://www.googleapis.com/contact/v3"; // Change this to the actual API URL

    // Create Contact
    public void createContact(String givenName, String familyName, String email, String phone) {
        String url = API_BASE_URL + "/contacts";

        Map<String, Object> request = new HashMap<>();
        request.put("givenName", givenName);
        request.put("familyName", familyName);
        request.put("email", email);
        request.put("phone", phone);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        restTemplate.postForEntity(url, entity, String.class);
    }

    // Update Contact
    public void updateContact(String resourceName, String name, String email, String phone) {
        String url = API_BASE_URL + "/contacts/" + resourceName;

        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("email", email);
        request.put("phone", phone);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    // Delete Contact
    public void deleteContact(String resourceName) {
        String url = API_BASE_URL + "/contacts/" + resourceName;

        restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    }
}
