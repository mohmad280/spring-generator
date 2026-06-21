package com.mas.spring_generator.service.generator;


import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipHelper {


    public void addFile(ZipOutputStream zip, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zip.putNextEntry(entry);
        zip.write(content.getBytes());
        zip.closeEntry();
    }
}
