package com.esmia.lovematch.dto.response;

import java.util.List;

public record PageResponse<T>(List<T> elements, int page, int taille, long total) {}
