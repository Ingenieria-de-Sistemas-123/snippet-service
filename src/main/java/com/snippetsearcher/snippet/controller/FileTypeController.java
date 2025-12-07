package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.response.FileTypeResponse;
import com.snippetsearcher.snippet.service.FileTypeService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file-types")
public class FileTypeController {

  private final FileTypeService fileTypeService;

  public FileTypeController(FileTypeService fileTypeService) {
    this.fileTypeService = fileTypeService;
  }

  @GetMapping
  public List<FileTypeResponse> getFileTypes() {
    return fileTypeService.getFileTypes();
  }
}
