package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.repository.KeywordRepository;
import org.springframework.stereotype.Service;

@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public KeywordService(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    /**
     * Finds a keyword by name or creates a new one if it doesn't exist.
     */
    public Keyword findOrCreateByName(String name) {
        Keyword keyword = keywordRepository.findByKeyword(name);
        if (keyword == null) {
            keyword = new Keyword();
            keyword.setKeyword(name);
            keyword = keywordRepository.save(keyword);
        }
        return keyword;
    }
}
