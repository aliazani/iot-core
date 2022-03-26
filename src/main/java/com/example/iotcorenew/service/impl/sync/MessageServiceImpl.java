package com.example.iotcorenew.service.impl.sync;

import com.example.iotcorenew.domain.Message;
import com.example.iotcorenew.repository.MessageRepository;
import com.example.iotcorenew.service.dto.MessageDTO;
import com.example.iotcorenew.service.mapper.MessageMapper;
import com.example.iotcorenew.service.sync.MessageServiceSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Message}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MessageServiceImpl implements MessageServiceSync {
    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    @Override
    public MessageDTO save(MessageDTO messageDTO) {
        log.debug("Request to save Message : {}", messageDTO);

        Message message = messageMapper.toEntity(messageDTO);
        message = messageRepository.save(message);

        return messageMapper.toDto(message);
    }

    @Override
    public Optional<MessageDTO> partialUpdate(MessageDTO messageDTO) {
        log.debug("Request to partially update Message : {}", messageDTO);

        return messageRepository
                .findById(messageDTO.getId())
                .map(existingMessage -> {
                    messageMapper.partialUpdate(existingMessage, messageDTO);

                    return existingMessage;
                })
                .map(messageRepository::save)
                .map(messageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Messages");

        return messageRepository.findAll(pageable).map(messageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageDTO> findOne(Long id) {
        log.debug("Request to get Message : {}", id);

        return messageRepository.findById(id).map(messageMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Message : {}", id);

        messageRepository.deleteById(id);
    }
}
