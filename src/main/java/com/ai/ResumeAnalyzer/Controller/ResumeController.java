package com.ai.ResumeAnalyzer.Controller;

import org.apache.tika.Tika;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("api/resume")
@CrossOrigin("*")
public class ResumeController {
    private final ChatClient chatClient;

    private final Tika tika = new Tika();

    public ResumeController(OpenAiChatModel openAiChatModel){
        this.chatClient= ChatClient.create(openAiChatModel);
    }


    @PostMapping("/analyzer")
    public Map<String , Object> analyser(@RequestParam("file") MultipartFile file) throws Exception{
        //extracting my file
        String  content = tika.parseToString(file.getInputStream());

        String prompt = """
                Analyze the resume text:
                %s
                1. Extract Key Skills
                2. Rate overall Resume Quality(1-10)
                3. Suggest atleast 3 Improvements
                Reply in Structures JSON Format
                """.formatted(content);
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return Map.of("analysis",aiResponse);

    }


}
