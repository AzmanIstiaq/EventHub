package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.repository.KeywordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KeywordServiceTest {

    private KeywordRepository keywordRepository;
    private KeywordService keywordService;

    @BeforeEach
    void setup() {
        keywordRepository = mock(KeywordRepository.class);
        keywordService = new KeywordService(keywordRepository);
    }

    @Test
    @DisplayName("findOrCreateByName(): returns existing keyword")
    void returnsExisting() {
        Keyword existing = new Keyword(); existing.setId(1L); existing.setKeyword("hackathon");
        when(keywordRepository.findByKeyword("hackathon")).thenReturn(existing);

        Keyword result = keywordService.findOrCreateByName("hackathon");
        assertThat(result).isSameAs(existing);
        verify(keywordRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateByName(): creates when missing")
    void createsWhenMissing() {
        when(keywordRepository.findByKeyword("ai")).thenReturn(null);
        when(keywordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Keyword created = keywordService.findOrCreateByName("ai");
        assertThat(created.getKeyword()).isEqualTo("ai");
        verify(keywordRepository).save(any(Keyword.class));
    }
}
