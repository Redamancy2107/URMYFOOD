package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostSearchIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void searchHandlesFlexibleQueries() {
        java.time.OffsetDateTime anchor = java.time.OffsetDateTime.now();
        assertThat(postRepository.searchByKeyword("bun", null, 0, 5, anchor)).isNotNull();
        assertThat(postRepository.searchByKeyword("bn", null, 0, 5, anchor)).isNotNull();
        assertThat(postRepository.searchByKeyword("toi muon an bun", null, 0, 5, anchor)).isNotNull();
        assertThat(postRepository.countByKeyword("bun", anchor)).isGreaterThanOrEqualTo(0L);
    }
}
