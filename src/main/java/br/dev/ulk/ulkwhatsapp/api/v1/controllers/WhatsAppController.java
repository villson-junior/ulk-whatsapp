package br.dev.ulk.ulkwhatsapp.api.v1.controllers;

import br.dev.ulk.ulkwhatsapp.infraestructure.external.WhatsAppService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    @Value("${meta.api.accessToken}")
    private String accessToken;

    @Value("${meta.api.authorizedTestNumber}")
    private String authorizedTestNumber;

    @Autowired
    private WhatsAppService whatsAppService;

    Logger logger = LoggerFactory.getLogger(WhatsAppController.class);

    @GetMapping("/webhook")
    public ResponseEntity<String> validateWebhook(
        @RequestParam(value = "hub.verify_token") String verifyToken,
        @RequestParam(value = "hub.challenge") String challenge) {
        if (accessToken.equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido");
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleIncomingMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Payload recebido: {}", payload);

        Optional.ofNullable(payload.get("entry"))
            .map(entryObj -> (List<Map<String, Object>>) entryObj)
            .ifPresent(entries -> entries.stream()
                .map(entry -> (List<Map<String, Object>>) entry.get("changes"))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(change -> (Map<String, Object>) change.get("value"))
                .filter(Objects::nonNull)
                .map(value -> (List<Map<String, Object>>) value.get("messages"))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(message -> {
                    String fromNumber = (String) message.get("from");
                    Optional.ofNullable(message.get("text"))
                        .map(textObj -> (Map<String, Object>) textObj)
                        .map(text -> (String) text.get("body"))
                        .ifPresent(messageBody -> {

                            logger.info("Número do remetente: {}", fromNumber);
                            logger.info("Mensagem recebida: {}", messageBody);

                            whatsAppService.sendMessage(fromNumber,
                                messageBody);
                        });
                }));

        return ResponseEntity.ok("Mensagem processada com sucesso!");
    }

    @GetMapping("/v1-test")
    public ResponseEntity<String> sendMessage() {
        String messageBody = "Simple Test";
        whatsAppService.sendMessage(authorizedTestNumber, messageBody);
        return ResponseEntity.ok("Mensagem processada com sucesso!");
    }
}