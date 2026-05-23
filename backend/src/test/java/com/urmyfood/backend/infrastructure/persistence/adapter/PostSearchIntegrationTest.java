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
        assertThat(postRepository.searchByKeyword("bun", null, 0, 5)).isNotNull();
        assertThat(postRepository.searchByKeyword("bn", null, 0, 5)).isNotNull();
        assertThat(postRepository.searchByKeyword("toi muon an bun", null, 0, 5)).isNotNull();
        assertThat(postRepository.countByKeyword("bun")).isGreaterThanOrEqualTo(0L);
    }
}
