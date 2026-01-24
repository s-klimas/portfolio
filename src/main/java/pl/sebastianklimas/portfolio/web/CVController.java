package pl.sebastianklimas.portfolio.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;

@Controller
public class CVController {

    @GetMapping("/cv")
    public ResponseEntity<Resource> previewPdf() {
        Resource resource = new ClassPathResource("files/CVKlimasSebastian.pdf");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }

    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException ex) {
        return "error/404";
    }
}
