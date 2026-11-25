package com.esmia.lovematch.dto.response;

public record StatistiquesResponse(long membres, long suspendus, long matchsOuverts, long messages24h,
                                   long signalementsOuverts) {}
