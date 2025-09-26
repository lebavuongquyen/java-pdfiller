package vn.quyen.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import vn.quyen.service.PdfFillService;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class PdfFillerCli {

    @Parameter(names = "--template", required = true, description = "Path to input PDF template")
    private String templatePath;

    @Parameter(names = "--data", description = "Path to JSON data file")
    private String dataPath;

    @Parameter(names = "--json", description = "Raw JSON string to fill form")
    private String jsonString;

    @Parameter(names = "--output", description = "Path to output PDF file")
    private String outputPath;

    @Parameter(names = "--flatten", description = "Flatten the PDF form")
    private boolean flatten = false;

    public static void main(String[] args) {
        PdfFillerCli cli = new PdfFillerCli();
        JCommander.newBuilder()
                .addObject(cli)
                .build()
                .parse(args);

        cli.run();
    }

    public void run() {
        try {
            File template = new File(templatePath);
            String pdfBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(template.toPath()));

            String jsonData;
            if (jsonString != null) {
                jsonData = jsonString;
            } else if (dataPath != null) {
                jsonData = Files.readString(new File(dataPath).toPath());
            } else {
                jsonData = "{}"; // mặc định nếu không có --json hoặc --data
            }

            String resultBase64 = PdfFillService.fill(pdfBase64, jsonData, flatten);

            if (outputPath != null) {
                File output = new File(outputPath);
                Files.write(output.toPath(), Base64.getDecoder().decode(resultBase64));
                System.out.println("✅ Output saved to " + output.getPath());
            } else {
                System.out.println(resultBase64);
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}