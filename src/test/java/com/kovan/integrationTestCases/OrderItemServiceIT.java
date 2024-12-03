package com.kovan.integrationTestCases;

import com.kovan.entities.*;
import com.kovan.repository.*;
import com.kovan.service.OrderItemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderItemServiceIT {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private Book savedBook;

    @BeforeEach
    void setup() {
        // Ensure all data is cleaned up before starting tests
        cleanup();

        User user = User.builder()
                .firstName("Test User")
                .email("Test@gmail.com")
                .build();
        userRepository.save(user);

        Address address = Address.builder()
                .city("Test City")
                .state("Test State")
                .build();
        addressRepository.save(address);

        Category testCategory = Category.builder().name("Fiction").build();
        testCategory = categoryRepository.save(testCategory);

        savedBook = bookRepository.save(
                Book.builder()
                        .title("Test Book")
                        .author("Author")
                        .price(20.00)
                        .category(testCategory)
                        .build()
        );
    }

    @AfterEach
    void cleanup() {
        orderItemRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testAddOrderItem() {
        int quantity = 2;
        Double unitPrice = 20.00;

        OrderItem orderItem = orderItemService.addOrderItem(
                savedBook.getBookId(),
                quantity,
                unitPrice
        );

        assertNotNull(orderItem.getOrderItemId());
        assertEquals(savedBook.getBookId(), orderItem.getBook().getBookId());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(unitPrice, orderItem.getUnitPrice());
        assertEquals((unitPrice * quantity), orderItem.getTotalPrice());
    }

    @Test
    void testGetAllOrderItems() {
        OrderItem orderItem1 = OrderItem.builder()
                .book(savedBook)
                .quantity(1)
                .unitPrice(20.00)
                .totalPrice(20.00)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .book(savedBook)
                .quantity(3)
                .unitPrice(15.00)
                .totalPrice(45.00)
                .build();

        orderItemRepository.saveAll(List.of(orderItem1, orderItem2));

        List<OrderItem> orderItems = orderItemService.getAllOrderItems();

        assertEquals(2, orderItems.size());
    }

    @Test
    void testGetOrderItemById() {
        OrderItem orderItem = orderItemRepository.save(
                OrderItem.builder()
                        .book(savedBook)
                        .quantity(2)
                        .unitPrice(20.00)
                        .totalPrice(40.00)
                        .build()
        );

        OrderItem fetchedOrderItem = orderItemService.getOrderItemById(orderItem.getOrderItemId());

        assertNotNull(fetchedOrderItem);
        assertEquals(orderItem.getOrderItemId(), fetchedOrderItem.getOrderItemId());
    }

    @Test
    void testDeleteOrderItem() {
        OrderItem orderItem = orderItemRepository.save(
                OrderItem.builder()
                        .book(savedBook)
                        .quantity(2)
                        .unitPrice(20.00)
                        .totalPrice(40.00)
                        .build()
        );

        orderItemService.deleteOrderItem(orderItem.getOrderItemId());

        assertFalse(orderItemRepository.existsById(orderItem.getOrderItemId()));
    }
}
