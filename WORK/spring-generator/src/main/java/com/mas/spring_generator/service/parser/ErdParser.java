package com.mas.spring_generator.service.parser;

import com.mas.spring_generator.DTO.EntityRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ErdParser {

    List<EntityRequest> parse(MultipartFile file);

}
