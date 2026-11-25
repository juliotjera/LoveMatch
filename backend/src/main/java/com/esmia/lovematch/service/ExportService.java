package com.esmia.lovematch.service;

import com.esmia.lovematch.dto.response.ExportResponse;

public interface ExportService {

    // tout ce que LoveMatch garde sur le membre
    ExportResponse exporter(Long moi);
}
