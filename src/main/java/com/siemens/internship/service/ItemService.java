package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        //se obtin toate id-urile
        List<Long> itemIds = itemRepository.findAllIds();

        //lista de task-uri
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> { //pentru fiecare id se creeaza un task asincron
                try {
                    Thread.sleep(100); //simularea procesarii
                    Item item = itemRepository.findById(id).orElse(null); //se obtine item-ul din baza de date

                    if (item != null) {
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);
                    }
                    //daca s-a gasit item-ul, statusul se seteaza la "PROCESSED"
                    return null;
                } catch (Exception e) {
                    System.err.println("Error processing item " + id);
                    return null;
                }
            }, executor);

            futures.add(future); //se adauga task-ul curent in lista de task-uri
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])) //se returneaza lista dupa ce s-a finalizat procesarea tuturor task-urilor
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    //metoda initiala returna imediat lista processedItems, care era goala, deoarece task-urile nu apucau sa se finalizeze

}

