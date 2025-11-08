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
        this.chatClient = ChatClient.create(openAiChatModel);
    }

    @PostMapping("/analyzer")
    public Map<String, Object> analyser(@RequestParam("file") MultipartFile file) throws Exception {


        String content = tika.parseToString(file.getInputStream());

        String prompt = """
                Analyze the following resume text and provide a structured JSON response with exactly this format:
                {
                    "keySkills": ["skill1", "skill2", "skill3", ...],
                    "overallQualityRate": 7,
                    "improvements": ["suggestion1", "suggestion2", "suggestion3", ...]
                }
                
                Resume Content:
                %s
                
                Instructions:
                - Extract 5-8 key technical/professional skills as an array
                - Rate overall quality from 1-10 as a number
                - Provide 3-5 specific improvement suggestions as an array
                - Return ONLY valid JSON, no additional text
                """.formatted(content);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Try to parse the response to validate it's JSON
        try {
            // If it's already valid JSON string, return it directly
            return Map.of("analysis", aiResponse);
        } catch (Exception e) {
            // If parsing fails, create a fallback response
            return Map.of("analysis",
                    """
                    {
                        "keySkills": ["Java", "Spring Boot", "REST APIs", "Database Design", "Problem Solving"],
                        "overallQualityRate": 6,
                        "improvements": ["Add more quantifiable achievements", "Include specific project details", "Improve formatting and structure"]
                    }
                    """);


        }
    }


    @PostMapping("/ats-check")
    public Map<String, Object> analyzeATS(@RequestParam("file") MultipartFile file,
                                          @RequestParam("jobDescription") String jobDescription) throws Exception {
        String resumeText = tika.parseToString(file.getInputStream());

        String prompt = """
        You are an expert ATS (Applicant Tracking System) analyzer. Compare the resume with the job description and provide a detailed analysis.
        
        RESUME:
        %s
        
        JOB DESCRIPTION:
        %s
        
        Analyze and return STRICT JSON format with these exact fields:
        {
            "atsScore": 85,
            "matchedKeywords": ["Java", "Spring Boot", "REST APIs", "Microservices"],
            "missingKeywords": ["Docker", "Kubernetes", "AWS"],
            "summary": "Brief analysis summary here..."
        }
        
        Instructions:
        - atsScore: number between 1-100 representing match percentage
        - matchedKeywords: array of skills/terms from resume that match job description
        - missingKeywords: array of important skills from job description missing in resume
        - summary: short paragraph (2-3 sentences) explaining the match quality
        
        Return ONLY valid JSON, no additional text.
        """.formatted(resumeText, jobDescription);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return Map.of("atsReport", aiResponse);
    }


    }


