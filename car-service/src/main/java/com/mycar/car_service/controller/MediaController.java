package com.mycar.car_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MediaController {
    private static final Logger log = LoggerFactory.getLogger(MediaController.class);

    @GetMapping("/media")
    public String showMediaPage() {
        log.info("Запрос HTML-страницы медиа-галереи /media");
        return "media";
    }

    @GetMapping("/media/files/{type}/{fileName:.+}")
    public ResponseEntity<Resource> streamMediaFile(@PathVariable String type, @PathVariable String fileName,
            @RequestParam(defaultValue = "false") boolean download) {
        log.info("Запрос медиафайла: type = '{}', fileName = '{}', download = {}", type, fileName, download);
        Resource resource = new ClassPathResource("static/" + type + "/" + fileName);

        if (!resource.exists()) {
            log.warn("Медиафайл не найден: static/{}/{}", type, fileName);
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        HttpHeaders headers = new HttpHeaders();
        ContentDisposition disposition = download
                ? ContentDisposition.attachment().filename(fileName).build()
                : ContentDisposition.inline().filename(fileName).build();
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .headers(headers)
                .body(resource);
    }
}