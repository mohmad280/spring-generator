package com.mas.spring_generator.service;

import com.mas.spring_generator.DTO.ProjectRequest;

public interface ProjectGeneratorService {

    byte[] generate(ProjectRequest request);

}
