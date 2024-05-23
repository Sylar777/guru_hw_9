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
    private static final ClassLoader cl = FileReaderTest.class.getClassLoader();

    @Test
    void readCSVFile() throws IOException, CsvException {
        var ByteArrayInputStream = zipFileParsing(".csv");
        assert ByteArrayInputStream != null;

        CSVReader reader = new CSVReader(new InputStreamReader(ByteArrayInputStream));
        List<String[]> allRows = reader.readAll();
        assertThat(allRows.get(2)[6]).isEqualTo("8754324321");
    }

    @Test
    void readXLSFile() throws IOException {
        var ByteArrayInputStream = zipFileParsing(".xlsx");
        assert ByteArrayInputStream != null;

        BufferedInputStream reader = new BufferedInputStream(ByteArrayInputStream);
        XLS xls = new XLS(reader);
        var firstData = xls.excel.getSheetAt(0).getRow(0).getCell(0).getNumericCellValue();
        assertThat(firstData).isEqualTo(1465000.0);
    }

    @Test
    void readPDFFile() throws IOException {
        var ByteArrayInputStream = zipFileParsing(".pdf");
        assert ByteArrayInputStream != null;

        PDF pdf = new PDF(ByteArrayInputStream);
        assertThat(pdf.numberOfPages).isEqualTo(2);
    }

    @Test
    void readJSONFileByJackson() throws IOException {
        var ByteArrayInputStream = zipFileParsing(".json");
        assert ByteArrayInputStream != null;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(ByteArrayInputStream);
        assertThat(jsonNode.get("name").asText()).isEqualTo("Andromeda");
        assertThat(jsonNode.get("innerData").get("age").asInt()).isEqualTo(100000);
    }

    @Test
    void readJSONFileByJacksonWithModel() throws IOException {
        var ByteArrayInputStream = zipFileParsing(".json");
        assert ByteArrayInputStream != null;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonModel jsonModel = objectMapper.readValue(ByteArrayInputStream, JsonModel.class);
        assertThat(jsonModel.getName()).isEqualTo("Andromeda");
        assertThat(jsonModel.getInnerData().getType()).isEqualTo("Galaxy");
    }

    public static ByteArrayInputStream zipFileParsing(String fileExtension) {
        try (ZipInputStream zis = new ZipInputStream(
                Objects.requireNonNull(cl.getResourceAsStream("archive.zip"))
        )) {
            ZipEntry entry;

            System.out.println("Reading file: " + fileExtension);

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(fileExtension)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > -1) {
                        baos.write(buffer, 0, len);
                    }
                    byte[] entryData = baos.toByteArray();
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(entryData)) {
                        return bais;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
