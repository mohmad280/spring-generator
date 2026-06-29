package com.mas.spring_generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.DTO.ProjectRequestWithERD;
import com.mas.spring_generator.service.ProjectGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(
            value = "/generate-from-erd",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> generateFromErd(
            @RequestPart("request") String requestJson,
            @RequestPart("erdFile") MultipartFile erdFile
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ProjectRequestWithERD request =
                mapper.readValue(requestJson, ProjectRequestWithERD.class);

        byte[] zip = projectGeneratorService.generateWithERD(request, erdFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + request.getProjectName() + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
