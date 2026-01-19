package org.orbitalLogistic.file.adapters.reports;

import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.PdfReportGenerator;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.adapters.kafka.dto.ReportDataDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class PdfReportGeneratorImpl implements PdfReportGenerator {

    private final StorageOperations storageOperations;

    @Override
    public byte[] generate(ReportDataDTO report) {
        try (InputStream inputStream = storageOperations.download(FileCategory.USER, "reports/Mission report.pdf")
                .inputStream();
             PdfReader pdfReader = new PdfReader(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfStamper pdfStamper = new PdfStamper(pdfReader, baos)) {

            PdfContentByte canvas = pdfStamper.getOverContent(1);
            BaseFont font = BaseFont.createFont(getFontPath("DejaVuSans"), BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);

            canvas.beginText();
            canvas.setFontAndSize(font, 14);

            canvas.showTextAligned(Element.ALIGN_LEFT, report.missionCode(), 193, 650, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.missionName(), 238, 625, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.missionType(), 191, 600, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.priority(), 188, 574, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(report.commandingOfficerId()), 323, 549, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(report.spacecraftId()), 300, 524, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.scheduledDeparture().toString(), 255, 498, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.scheduledArrival().toString(), 230, 472, 0);

            canvas.endText();

            pdfStamper.close();
            pdfReader.close();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get absolute path of fonts which locate in resources directory.
     * @param font - font name
     * @return -
     */
    private String getFontPath(String font) {
        URL url = getClass().getClassLoader().getResource("fonts/" + font + ".ttf");
        try {
            return Paths.get(url.toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTimeString(Long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());

        String date = String.format("%02d.%02d.%d", dateTime.getDayOfMonth(), dateTime.getMonthValue(), dateTime.getYear());
        String time = String.format("%02d:%02d:%02d", dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

        return date + " " + time;
    }

    private String getDurationString(Integer duration) {
        return String.format("%dч %dмин", duration / 60, duration % 60);
    }
}
