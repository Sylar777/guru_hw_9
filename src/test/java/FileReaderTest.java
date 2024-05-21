import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import model.JsonModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileReaderTest {
    private final ClassLoader cl = FileReaderTest.class.getClassLoader();

    @Test
    void zipFileParsingTest() {
        try (ZipInputStream zis = new ZipInputStream(
                Objects.requireNonNull(cl.getResourceAsStream("archive.zip"))
        )) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().startsWith("__MACOSX")) {
                    continue;
                }

                System.out.println("Reading file: " + entry.getName());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] entryData = baos.toByteArray();

                if (entry.getName().endsWith(".csv")) {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                        readContentFromCsv(bais);
                    }
                } else if (entry.getName().endsWith(".xlsx")) {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                        readContentFromXls(bais);
                    }
                } else if (entry.getName().endsWith(".pdf")) {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                        readContentFromPdf(bais);
                    }
                } else if (entry.getName().endsWith(".json")) {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                        readContentFromJsonByJackson(bais);
                    }
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                        readContentFromJsonByJacksonWithModel(bais);
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private static void readContentFromCsv(InputStream inputStream) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));

        List<String[]> allRows = reader.readAll();
        Assertions.assertEquals(allRows.get(2)[6],"8754324321");
    }

    private static void readContentFromXls(InputStream inputStream) throws IOException {
        BufferedInputStream reader = new BufferedInputStream(inputStream);
        XLS xls = new XLS(reader);
        var firstData = xls.excel.getSheetAt(0).getRow(0).getCell(0).getNumericCellValue();
        Assertions.assertEquals(1465000.0, firstData);
    }

    private static void readContentFromPdf(InputStream inputStream) throws IOException {
        PDF pdf = new PDF(inputStream);
        Assertions.assertEquals(pdf.numberOfPages, 2);
    }

    private static void readContentFromJsonByJackson(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(inputStream);
        Assertions.assertEquals("Andromeda", jsonNode.get("name").asText());
        Assertions.assertEquals(100000, jsonNode.get("innerData").get("age").asInt());
    }

    private static void readContentFromJsonByJacksonWithModel(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonModel jsonModel = objectMapper.readValue(inputStream, JsonModel.class);
        Assertions.assertEquals("Andromeda", jsonModel.getName());
        Assertions.assertEquals("Galaxy", jsonModel.getInnerData().getType());
    }
}
