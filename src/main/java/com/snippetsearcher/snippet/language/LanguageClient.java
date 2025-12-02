package com.snippetsearcher.snippet.language;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LanguageClient {
    private final RestClient http;

    public LanguageClient(RestClient restClient /* bean ya configurado a base-url del language */) {
        this.http = restClient;
    }

    public LanguageDtos.ValidateResponse validate(LanguageDtos.ValidateRequest req) {
        return http.post().uri("/validate").contentType(APPLICATION_JSON).body(req)
                .retrieve().body(LanguageDtos.ValidateResponse.class);
    }

    public LanguageDtos.FormatResponse format(LanguageDtos.FormatRequest req) {
        return http.post().uri("/format").contentType(APPLICATION_JSON).body(req)
                .retrieve().body(LanguageDtos.FormatResponse.class);
    }

    public LanguageDtos.AnalyzeResponse analyze(LanguageDtos.AnalyzeRequest req) {
        return http.post().uri("/analyze").contentType(APPLICATION_JSON).body(req)
                .retrieve().body(LanguageDtos.AnalyzeResponse.class);
    }

    public LanguageDtos.ExecuteResponse execute(LanguageDtos.ExecuteRequest req) {
        return http.post().uri("/execute").contentType(APPLICATION_JSON).body(req)
                .retrieve().body(LanguageDtos.ExecuteResponse.class);
    }
}
