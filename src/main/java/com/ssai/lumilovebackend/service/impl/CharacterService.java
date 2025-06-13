package com.ssai.lumilovebackend.service.impl;

import com.ssai.lumilovebackend.entity.Character;
import com.ssai.lumilovebackend.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;

    @Transactional(readOnly = true)
    public String buildCharacterPrompt(Long characterId) {
        if (characterId == null) {
            log.warn("Character ID is null. Using a default fallback prompt.");
            return "You are a helpful AI assistant. Please start the conversation.";
        }
        
        try {
            Optional<Character> characterOptional = characterRepository.findById(characterId);

            if (characterOptional.isEmpty()) {
                log.warn("Character with ID {} not found. Using a default fallback prompt.", characterId);
                return "You are a helpful AI assistant. Please introduce yourself and start the conversation.";
            }

            Character character = characterOptional.get();

            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("You are ").append(character.getName()).append(". ");
            promptBuilder.append("Here is a brief description of you: ").append(character.getDescription()).append(". ");
            
            promptBuilder.append("Your detailed character configuration and rules are as follows: \n")
                         .append(character.getPromptConfig());

            String finalPrompt = promptBuilder.toString();
            log.info("Successfully built prompt for character ID {}.", characterId);
            return finalPrompt;
            
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching character with ID {}. Using fallback. Error: {}", 
                      characterId, e.getMessage(), e);
            return "You are a helpful AI assistant. An error occurred, but please start a friendly conversation.";
        }
    }
}