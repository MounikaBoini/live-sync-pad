package com.websocket.example.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class FileUpdateController {
    private final String FILE_PATH = "src/main/resources/files/learn.txt";

    @MessageMapping("/load")
    @SendTo("/topic/loaded")
    public String loadFile() throws IOException {
        Path path= Path.of(FILE_PATH);
        if(Files.exists(path)){
            String fileContent=Files.readString(path);
            return fileContent;
        }
        return "File does not exists";
    }
    @MessageMapping("/edit")
    @SendTo("/topic/updates")
    public String previewContent(String newContent){
        return newContent;
    }

    @MessageMapping("/save")
    @SendTo("/topic/saved")
    public void saveFile(String fileContent) throws IOException {
        Path path=Path.of(FILE_PATH);
        Files.writeString(path,fileContent);
        System.out.println("File successfully saved!");
    }
}
