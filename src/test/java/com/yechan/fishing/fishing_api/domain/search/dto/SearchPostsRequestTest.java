package com.yechan.fishing.fishing_api.domain.search.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SearchPostsRequestTest {

    @Test
    void safeSize_returnsDefaultWhenSizeIsNull() {
        SearchPostsRequest request = new SearchPostsRequest("bass", "bass_walking", null, null);

        assertEquals(20, request.safeSize());
        assertEquals("bass", request.q());
        assertEquals("bass_walking", request.boardKey());
        assertNull(request.cursor());
    }

    @Test
    void safeSize_returnsProvidedSize() {
        SearchPostsRequest request = new SearchPostsRequest(null, null, "cursor", 50);

        assertEquals(50, request.safeSize());
    }
}
