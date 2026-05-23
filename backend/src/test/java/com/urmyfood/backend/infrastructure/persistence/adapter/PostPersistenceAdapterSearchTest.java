package com.urmyfood.backend.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostPersistenceAdapterSearchTest {

    @Test
    void searchTermsNormalizeVietnameseText() {
        PostPersistenceAdapter.SearchTerms terms = PostPersistenceAdapter.searchTerms("Bún bò Huế");

        assertThat(terms.query()).isEqualTo("bun bo hue");
        assertThat(terms.tokens()).containsExactly("bun", "bo", "hue");
    }

    @Test
    void searchTermsRemoveIntentWordsFromLongSentence() {
        PostPersistenceAdapter.SearchTerms terms = PostPersistenceAdapter.searchTerms("toi muon an bun");

        assertThat(terms.query()).isEqualTo("bun");
        assertThat(terms.tokens()).containsExactly("bun");
    }

    @Test
    void searchTermsBuildShortTokenRegexForFuzzyMatch() {
        PostPersistenceAdapter.SearchTerms terms = PostPersistenceAdapter.searchTerms("bn");

        assertThat(terms.query()).isEqualTo("bn");
        assertThat(terms.tokenRegexes()).containsExactly("b.*n.*");
    }

    @Test
    void searchTermsReturnBlankForEmptyInput() {
        PostPersistenceAdapter.SearchTerms terms = PostPersistenceAdapter.searchTerms("   ");

        assertThat(terms.query()).isBlank();
        assertThat(terms.tokens()).isEmpty();
    }
}
