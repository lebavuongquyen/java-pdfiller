package vn.quyen.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.quyen.service.PdfFillService;

import java.util.Base64;

@RestController
@RequestMapping("/api/pdf")
public class PdfFillController {

    @PostMapping(value = "/fill", consumes = "multipart/form-data")
    public ResponseEntity<?> fillPdf(
            @RequestPart(required = false) MultipartFile source,
            @RequestPart(required = false) String sourceBase64,
            @RequestPart String data,
            @RequestParam(required = false) boolean flatten,
            @RequestParam(required = false) String filename) {
        try {
            String input;
            if (source != null) {
                input = Base64.getEncoder().encodeToString(source.getBytes());
            } else if (sourceBase64 != null) {
                input = sourceBase64;
            } else {
                return ResponseEntity.badRequest().body("Missing source");
            }

            String result = PdfFillService.fill(input, data, flatten);

            if (filename != null) {
                byte[] bytes = Base64.getDecoder().decode(result);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .body(bytes);
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}