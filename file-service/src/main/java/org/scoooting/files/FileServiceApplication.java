package org.scoooting.files;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.scoooting.files.adapters.exceptions.MinioException;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.services.FileFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

//@SpringBootApplication
public class FileServiceApplication {

    public static void main(String[] args) {
        try (InputStream inputStream = new FileInputStream("../init/minio/files/Mission report.pdf");
             PdfReader pdfReader = new PdfReader(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfStamper pdfStamper = new PdfStamper(pdfReader, baos)) {

            PdfContentByte canvas = pdfStamper.getOverContent(1);
            URL url = FileServiceApplication.class.getClassLoader().getResource("fonts/DejaVuSans.ttf");
            String fontpath = Paths.get(url.toURI()).toString();
            BaseFont font = BaseFont.createFont(fontpath, BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);

            canvas.beginText();
            canvas.setFontAndSize(font, 14);

            canvas.showTextAligned(Element.ALIGN_LEFT, "3", 193, 650, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "kdne", 238, 625, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "Type", 191, 600, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "Prio", 188, 574, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "Identity", 323, 549, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "space", 300, 524, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "12:30", 255, 498, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, "12:30", 230, 472, 0);

            canvas.endText();

            pdfStamper.close();
            pdfReader.close();

            byte[] bytes = baos.toByteArray();
            String reportName = "haha.pdf";

            try {
                Files.write(Path.of(reportName), bytes);
            } catch (Exception e) {
                throw new MinioException(e.getMessage());
            }

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        SpringApplication.run(FileServiceApplication.class, args);
    }


//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI().servers(List.of(new Server().url("/api/file")))
//                .components(new Components()
//                        .addSecuritySchemes("Bearer-jwt",
//                                new SecurityScheme()
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")))
//                .addSecurityItem(new SecurityRequirement().addList("Bearer-jwt"));
//    }
}
