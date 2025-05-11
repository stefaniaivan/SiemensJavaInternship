package com.siemens.internship;

import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class InternshipApplicationTests {

	@Mock
	private ItemRepository itemRepositoryMock;
	private ItemService itemService;
	private ItemController itemController;

	@BeforeEach
	public void setUp(){
		MockitoAnnotations.openMocks(this);
		itemService = new ItemService(itemRepositoryMock);
		itemController = new ItemController(itemService);
	}

	@Test
	void testGetAll() {
		Item mockItem1 = new Item(1L, "Laptop", "Asus", "NEW", "altex@gmail.com");
		Item mockItem2 = new Item(2L, "Mouse", "Hama", "NEW", "altex@gmail.com");
		when(itemRepositoryMock.findAll()).thenReturn(List.of(mockItem1, mockItem2));

		List<Item> result = itemService.findAll();

		assertEquals(2, result.size());
		assertEquals("Laptop", result.get(0).getName());
		assertEquals("Mouse", result.get(1).getName());
	}

	@Test
	public void createItemTest() {
		Item expectedItem = new Item(1L, "Laptop", "Asus", "NEW", "altex@gmail.com");
		itemService.save(expectedItem);
		verify(itemRepositoryMock).save(expectedItem);
	}

	@Test
	public void createItemInvalidEmailTest(){
		Item invalidItem = new Item(1L, "Laptop", "Asus", "NEW", "invalid");

		BindingResult mockBindingResult = mock(BindingResult.class);
		when(mockBindingResult.hasErrors()).thenReturn(true);

		ResponseEntity<Item> response = itemController.createItem(invalidItem, mockBindingResult);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNull(response.getBody());

		verify(itemRepositoryMock, never()).save(any());
	}

	@Test
	public void getByIdTest(){
		Long id = 2L;
		itemService.findById(id);
		verify(itemRepositoryMock).findById(id);
	}


	@Test
	public void deleteItemTest(){
		Long id = 2L;
		itemService.deleteById(id);
		verify(itemRepositoryMock).deleteById(id);
	}

	@Test
	void processTest() throws Exception {
		List<Long> itemIds = List.of(1L, 2L);
		Item item1 = new Item(1L, "Laptop", "Asus", "NEW", "altex@gmail.com");
		Item item2 = new Item(2L, "Phone", "Samsung", "NEW", "emag@gmail.com");

		when(itemRepositoryMock.findAllIds()).thenReturn(itemIds);
		when(itemRepositoryMock.findById(1L)).thenReturn(Optional.of(item1));
		when(itemRepositoryMock.findById(2L)).thenReturn(Optional.of(item2));
		when(itemRepositoryMock.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get();

		assertEquals(2, result.size());
		assertEquals("PROCESSED", result.get(0).getStatus());
		assertEquals("PROCESSED", result.get(1).getStatus());
		verify(itemRepositoryMock, times(2)).save(any());
	}

	@Test
	void contextLoads() {
	}

}
