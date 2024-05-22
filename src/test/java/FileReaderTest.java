import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import model.JsonModel;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FileReaderTest {
    private final ClassLoader cl = FileReaderTest.class.getClassLoader();

    @Test
    void readCSVFile() {
        zipFileParsingAndReadTheFile(".csv");
    }

    @Test
    void readXLSFile() {
        zipFileParsingAndReadTheFile(".xls");
    }

    @Test
    void readPDFFile() {
        zipFileParsingAndReadTheFile(".pdf");
    }

    @Test
    void readJSONFile() {
        zipFileParsingAndReadTheFile(".json");
    }

    public void zipFileParsingAndReadTheFile(String fileExtension) {
        try (ZipInputStream zis = new ZipInputStream(
                Objects.requireNonNull(cl.getResourceAsStream("archive.zip"))
        )) {
            ZipEntry entry;

            System.out.println("Reading file: " + fileExtension);

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().startsWith("__MACOSX")) {
                    continue;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] entryData = baos.toByteArray();

                switch (fileExtension) {
                    case ".csv" -> {
                        if (entry.getName().endsWith(".csv")) {
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                                readContentFromCsv(bais);
                            }
                        }
                    }
                    case ".xls" -> {
                        if (entry.getName().endsWith(".xls")) {
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                                readContentFromXls(bais);
                            }
                        }
                    }
                    case ".pdf" -> {
                        if (entry.getName().endsWith(".pdf")) {
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                                readContentFromPdf(bais);
                            }
                        }
                    }
                    case ".json" -> {
                        if (entry.getName().endsWith(".json")) {
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                                readContentFromJsonByJackson(bais);
                            }
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                                readContentFromJsonByJacksonWithModel(bais);
                            }
                        }
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
        assertThat(allRows.get(2)[6]).isEqualTo("8754324321");
    }

    private static void readContentFromXls(InputStream inputStream) throws IOException {
        BufferedInputStream reader = new BufferedInputStream(inputStream);
        XLS xls = new XLS(reader);
        var firstData = xls.excel.getSheetAt(0).getRow(0).getCell(0).getNumericCellValue();
        assertThat(firstData).isEqualTo(1465000.0);
    }

    private static void readContentFromPdf(InputStream inputStream) throws IOException {
        PDF pdf = new PDF(inputStream);
        assertThat(pdf.numberOfPages).isEqualTo(2);
    }

    private static void readContentFromJsonByJackson(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(inputStream);
        assertThat(jsonNode.get("name").asText()).isEqualTo("Andromeda");
        assertThat(jsonNode.get("innerData").get("age").asInt()).isEqualTo(100000);
    }

    private static void readContentFromJsonByJacksonWithModel(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonModel jsonModel = objectMapper.readValue(inputStream, JsonModel.class);
        assertThat(jsonModel.getName()).isEqualTo("Andromeda");
        assertThat(jsonModel.getInnerData().getType()).isEqualTo("Galaxy");
    }
}
