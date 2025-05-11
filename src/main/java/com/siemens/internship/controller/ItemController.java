package com.siemens.internship.controller;

import com.siemens.internship.service.ItemService;
import com.siemens.internship.model.Item;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Validation Error: " + error.getDefaultMessage());
            });
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); //se returneaza HttpStatus.BAD_REQUEST in caz de erori
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED); //daca se creeaza item-ul se returneaza HttpStatus.CREATED
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //returneaza HttpStatus_NOT_FOUND in cazul in care nu se gaseste item-ul cu id-ul respectiv
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Validation Error: " + error.getDefaultMessage());
            });
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK); //in cazul in care se modifica un item deja existent, se returneaza HttpStatus.OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //returneaza HttpStatus.NOT_FOUND daca nu se gaseste item-ul
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> existingItem = itemService.findById(id);
        if(existingItem.isPresent()){
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK); //mai intai verific daca exista item-ul in baza de date si se returneaza HttpStatus.OK daca item-ul exista si stergerea se realizeaza cu succes
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //daca item-ul nu se gaseste in baza de date se returneaza HttpStatus.NOT_FOUND
        }
    }

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(ResponseEntity::ok);
    }

}
