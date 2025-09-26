package vn.quyen.http;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/documents")
    public String documents() {
        return "forward:/documents.html";
    }
}
