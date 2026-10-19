package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.out.document;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.EnrollmentUserImagesDocumentPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.EnrollmentUserImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentUserImagesDocumentAdapter implements EnrollmentUserImagesDocumentPort {

  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
  private static final int N_COLUMNS = 5;

  private static final int PHOTO_SIZE = 80;

  @Override
  @SneakyThrows
  public byte[] generateEnrollmentImagesDocument(
      List<EnrollmentUserImage> users, EnrollmentImagesDocumentCommand command) {

    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        XWPFDocument document = new XWPFDocument()) {

      fillHeaderAndFoot(document, command.getHeaderText());

      XWPFTable tab =
          document.createTable(
              (int) Math.ceil(users.size() / (double) N_COLUMNS),
              Math.min(users.size(), N_COLUMNS));

      tab.setCellMargins(50, 0, 0, 0);

      for (int i = 0; i < users.size(); i++) {
        EnrollmentUserImage user = users.get(i);
        XWPFTableRow row = tab.getRow(i / N_COLUMNS);
        row.setCantSplitRow(false);

        XWPFTableCell cell = row.getCell(i % N_COLUMNS);

        XWPFParagraph paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();

        byte[] pictureData = user.getUserImage().getImage();

        run.addPicture(
            new ByteArrayInputStream(pictureData),
            getPictureType(pictureData, user.getUserImage().getContentType()),
            user.getFullName(),
            Units.pixelToEMU(PHOTO_SIZE),
            Units.pixelToEMU(PHOTO_SIZE));

        run.addBreak();
        run.setText(user.getFullName());
      }

      document.write(out);
      return out.toByteArray();
    }
  }

  public static void fillHeaderAndFoot(XWPFDocument document, String headerText) {
    XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
    if (headerFooterPolicy == null) headerFooterPolicy = document.createHeaderFooterPolicy();

    // create header start
    XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

    XWPFParagraph paragraph = header.createParagraph();

    XWPFRun run = paragraph.createRun();
    run.setText(headerText);
    paragraph = header.createParagraph();
    paragraph.setAlignment(ParagraphAlignment.RIGHT);

    run = paragraph.createRun();
    run.setText(LocalDateTime.now().format(DATE_TIME_FORMATTER));

    XWPFFooter footer = document.createFooter(HeaderFooterType.DEFAULT);

    paragraph = footer.getParagraphArray(0);
    if (paragraph == null) paragraph = footer.createParagraph();
    paragraph.setAlignment(ParagraphAlignment.CENTER);
    run = paragraph.createRun();

    paragraph.getCTP().addNewFldSimple().setInstr("PAGE \\* ARABIC MERGEFORMAT");
    run = paragraph.createRun();
    run.setText("/");
    paragraph.getCTP().addNewFldSimple().setInstr("NUMPAGES \\* ARABIC MERGEFORMAT");
  }

  public static PictureType getPictureType(byte[] byteArray, String contentType) {

    try {

      FileMagic fm = FileMagic.valueOf(byteArray);
      return PictureType.valueOf(fm);

    } catch (Exception e) {
      log.error("Error occurred while determining picture type", e);
    }

    for (PictureType pictureType : PictureType.values()) {
      if (pictureType.getContentType().equals(contentType)) {
        return pictureType;
      }
    }
    return PictureType.PNG;
  }
}
