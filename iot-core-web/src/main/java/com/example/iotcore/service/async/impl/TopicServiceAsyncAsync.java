package com.example.iotcore.service.async.impl;

import com.example.iotcore.domain.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Topic}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class TopicServiceAsyncAsync {

//    private final TopicRepository topicRepository;
//
//    private final TopicMapper topicMapper;
//
//    @Async
//    @Override
//    public CompletableFuture<TopicDTO> save(TopicDTO topicDTO) {
//        log.debug("Request to save Topic : {}", topicDTO);
//
//        Topic topic = topicMapper.toEntity(topicDTO);
//        topic = topicRepository.save(topic);
//        System.out.println("************* thread: " + Thread.currentThread().getName());
//
//        return CompletableFuture.completedFuture(topicMapper.toDto(topic));
//    }
//
//    @Async
//    @Override
//    public CompletableFuture<Optional<TopicDTO>> partialUpdate(TopicDTO topicDTO) {
//        log.debug("Request to partially update Topic : {}", topicDTO);
//
//        return CompletableFuture.completedFuture(
//                topicRepository
//                        .findById(topicDTO.getId())
//                        .map(existingTopic -> {
//                            topicMapper.partialUpdate(existingTopic, topicDTO);
//
//                            return existingTopic;
//                        })
//                        .map(topicRepository::save)
//                        .map(topicMapper::toDto));
//    }
//
//    @Async
//    @Override
//    @Transactional(readOnly = true)
//    public CompletableFuture<LinkedList<TopicDTO>> findAll() {
//        log.debug("Request to get all Topics");
//
//        return CompletableFuture.completedFuture(
//                topicRepository.findAll().stream().map(topicMapper::toDto)
//                        .collect(Collectors.toCollection(LinkedList::new)));
//    }
//
//    @Async
//    @Override
//    @Transactional(readOnly = true)
//    public CompletableFuture<Optional<TopicDTO>> findOne(Long id) {
//        log.debug("Request to get Topic : {}", id);
//
//        return CompletableFuture.completedFuture(topicRepository.findById(id).map(topicMapper::toDto));
//    }
//
//    @Async
//    @Override
//    public void delete(Long id) {
//        log.debug("Request to delete Topic : {}", id);
//
//        topicRepository.deleteById(id);
//    }
}

