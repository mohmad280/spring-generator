package com.mas.spring_generator.controller;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.service.ProjectGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectGeneratorController {

    private final ProjectGeneratorService projectGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody ProjectRequest request) {
        byte[] zip = projectGeneratorService.generate(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + request.getProjectName() + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
