package jp.co.toyo.uploader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.toyo.uploader.storage.StorageFileNotFoundException;
import jp.co.toyo.uploader.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files",
                storageService.loadAll()
                        .map(path -> MvcUriComponentsBuilder
                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                                .build().toUri().toString())
                        .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<String> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        String safeOutput = "";
        try {
            BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result;
                result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            // safeOutput = ESAPI.encoder().encodeForHTML(buf.toString("UTF-8"));
            safeOutput = buf.toString("UTF-8").replaceAll("&", "&amp").replaceAll("<", "&lt").replaceAll(">", "&gt");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, 
        "text/html; charset=UTF-8").body(safeOutput);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
