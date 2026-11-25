package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.PageResponse;
import com.esmia.lovematch.dto.response.SuggestionResponse;

public interface SuggestionService {

    PageResponse<SuggestionResponse> suggestions(Long moi, int page, int taille, Integer distanceMax, boolean interetsCommuns);
}
