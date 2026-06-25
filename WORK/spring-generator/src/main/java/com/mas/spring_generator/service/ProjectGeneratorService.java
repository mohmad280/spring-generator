package com.mas.spring_generator.service;

import com.mas.spring_generator.DTO.ProjectRequest;
import com.mas.spring_generator.DTO.ProjectRequestWithERD;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectGeneratorService {

    byte[] generate(ProjectRequest request);
    byte[] generateWithERD(ProjectRequestWithERD request, MultipartFile erdFile);


}
