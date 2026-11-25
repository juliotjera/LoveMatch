package com.esmia.lovematch.controller.rest;

import com.esmia.lovematch.dto.response.NotificationResponse;
import com.esmia.lovematch.security.SessionCourante;
import com.esmia.lovematch.service.NotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notifications;
    private final SessionCourante session;

    public NotificationController(NotificationService notifications, SessionCourante session) {
        this.notifications = notifications;
        this.session = session;
    }

    @GetMapping
    public List<NotificationResponse> recentes() {
        return notifications.recentes(session.id());
    }

    @GetMapping("/non-lues")
    public Map<String, Long> nonLues() {
        return Map.of("nombre", notifications.nonLues(session.id()));
    }

    @PostMapping("/lues")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toutLire() {
        notifications.toutLire(session.id());
    }
}
