package br.dev.ulk.ulkwhatsapp.infraestructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppService {

    @Value("${whatsappBusinessAccountID}")
    private String whatsappBusinessAccountID;

    @Value("${meta.api.version}")
    private String version;

    @Value("${meta.api.accessToken}")
    private String accessToken;

    @Value("${meta.api.authorizedTestNumber}")
    private String authorizedTestNumber;

    private final String apiUrl =
        "https://graph.facebook.com/" + version + "/" + whatsappBusinessAccountID + "/messages";

    Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private String processUserMessage(String messageText) {
        if (messageText.equalsIgnoreCase("Opa")) {
            return "Opa! ulk no dev, manda a letra!";
        } else if (messageText.contains("Test")) {
            return "Por favor, me diga seu nome.";
        } else {
            return "Desculpe, não entendi sua mensagem.";
        }
    }

    public void sendMessage(String toPhoneNumber, String message) {
        RestTemplate restTemplate = new RestTemplate();

        String finalMessage = processUserMessage(message);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\n" +
            "  \"messaging_product\": \"whatsapp\",\n" +
            "  \"to\": \"" + authorizedTestNumber + "\",\n" +
            "  \"text\": {\n" +
            "    \"body\": \"" + finalMessage + "\"\n" +
            "  }\n" +
            "}";

        logger.info("Mensagem: {}", requestBody);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity,
                String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Mensagem enviada com sucesso!");
                logger.info("Resposta: {}", response.getBody());
            } else {
                logger.info("Erro ao enviar mensagem: {}", response.getStatusCode());
                logger.info("Resposta de erro: {}", response.getBody());
            }
        } catch (Exception e) {
            logger.error("Erro durante a comunicação com a API do WhatsApp: {}", e.getMessage());
        }
    }
}