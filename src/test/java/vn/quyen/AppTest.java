package vn.quyen;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vn.quyen.http.PdfFillController;
import vn.quyen.service.PdfFillService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PdfFillController.class)
public class AppTest {

    @Autowired
    private MockMvc mockMvc;

    private String base64FromFile(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String buildJsonData(String avatarBase64) {
        return """
                {
                    "name": "Nguyen Van Quyen",
                    "dob": "1990-01-01",
                    "date": "30/05/2025",
                    "email": "quyen@example.com",
                    "gender": "Male",
                    "combo": "a",
                    "list": ["a", "b", "c"],
                    "checkbox": true,
                    "avatar": "%s"
                }
                """.formatted(avatarBase64);
    }

    @Test
    void testHttpFillWithRawFile() throws Exception {
        byte[] pdfBytes = Files.readAllBytes(Paths.get("template.pdf"));
        String avatarBase64 = base64FromFile("lion.png");
        String dataJson = buildJsonData(avatarBase64);

        MockMultipartFile file = new MockMultipartFile(
                "source", "template.pdf", MediaType.APPLICATION_PDF_VALUE, pdfBytes);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data.json", MediaType.APPLICATION_JSON_VALUE, dataJson.getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart("/api/pdf/fill")
                .file(file)
                .file(dataPart)
                .param("flatten", "false")
                .param("filename", "filled_raw.pdf"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] responseBytes = result.getResponse().getContentAsByteArray();
        Path outputPath = Paths.get("test-output/filled_raw.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, responseBytes);

        try (PDDocument doc = PDDocument.load(responseBytes)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            assertNotNull(form, "❌ Không tìm thấy form");

            PDField nameField = form.getField("name");
            PDField emailField = form.getField("email");

            assertNotNull(nameField, "❌ Không tìm thấy trường 'name'");
            assertNotNull(emailField, "❌ Không tìm thấy trường 'email'");

            assertEquals("Nguyen Van Quyen", nameField.getValueAsString());
            assertEquals("quyen@example.com", emailField.getValueAsString());
        }
    }

    @Test
    void testHttpFillWithBase64() throws Exception {
        String pdfBase64 = base64FromFile("template.pdf");
        String avatarBase64 = base64FromFile("lion.png");
        String dataJson = buildJsonData(avatarBase64);

        MockMultipartFile sourceBase64Part = new MockMultipartFile(
                "sourceBase64", "source.txt", MediaType.TEXT_PLAIN_VALUE, pdfBase64.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data.json", MediaType.APPLICATION_JSON_VALUE, dataJson.getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart("/api/pdf/fill")
                .file(sourceBase64Part)
                .file(dataPart)
                .param("flatten", "false"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] decoded = Base64.getDecoder().decode(result.getResponse().getContentAsString());
        Path outputPath = Paths.get("test-output/filled_base64.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, decoded);

        try (PDDocument doc = PDDocument.load(decoded)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            assertNotNull(form, "❌ Không tìm thấy form");

            PDField nameField = form.getField("name");
            PDField emailField = form.getField("email");

            assertNotNull(nameField, "❌ Không tìm thấy trường 'name'");
            assertNotNull(emailField, "❌ Không tìm thấy trường 'email'");

            assertEquals("Nguyen Van Quyen", nameField.getValueAsString());
            assertEquals("quyen@example.com", emailField.getValueAsString());
        }
    }

    @Test
    void testDirectCallToPdfFillService() throws Exception {
        byte[] pdfBytes = Files.readAllBytes(Paths.get("template.pdf"));
        String avatarBase64 = base64FromFile("lion.png");
        String dataJson = buildJsonData(avatarBase64);
        String inputBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        // Gọi trực tiếp vào service
        String resultBase64 = PdfFillService.fill(inputBase64, dataJson, false);

        // Giải mã và lưu file
        byte[] filledBytes = Base64.getDecoder().decode(resultBase64);
        Path outputPath = Paths.get("test-output/filled_direct.pdf");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, filledBytes);

        // Đọc lại và kiểm tra nội dung
        try (PDDocument doc = PDDocument.load(filledBytes)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            assertNotNull(form, "❌ Không tìm thấy form");

            PDField nameField = form.getField("name");
            PDField emailField = form.getField("email");
            PDField dobField = form.getField("dob");

            assertNotNull(nameField, "❌ Không tìm thấy trường 'name'");
            assertNotNull(emailField, "❌ Không tìm thấy trường 'email'");
            assertNotNull(dobField, "❌ Không tìm thấy trường 'dob'");

            assertEquals("Nguyen Van Quyen", nameField.getValueAsString());
            assertEquals("quyen@example.com", emailField.getValueAsString());
            // assertEquals("1990-01-01", dobField.getValueAsString());
        }
    }
}