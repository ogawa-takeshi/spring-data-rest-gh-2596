package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
class MergePatchTests {

	@Autowired MockMvcTester mvc;
	@Autowired ProductRepository products;
	@Autowired PurchaseOrderRepository orders;
	@Autowired TransactionTemplate tx;

	Product apple;
	Product banana;

	@BeforeEach
	void setUp() {
		orders.deleteAll();
		products.deleteAll();
		apple = products.save(product("apple"));
		banana = products.save(product("banana"));
	}

	@Test // fails since Spring Data REST 4.3
	void patchUpdatesAssociationOfExistingArrayElement() {

		PurchaseOrder order = orders.save(order(item(1, apple)));

		patchItems(order, 2, banana);

		LineItem item = loadSingleItem(order);
		assertThat(item.getQuantity()).isEqualTo(2); // passes
		assertThat(item.getProduct().getId()).isEqualTo(banana.getId()); // fails: still apple
	}

	@Test // passes: same payload, but the element does not exist yet
	void patchSetsAssociationOfAppendedArrayElement() {

		PurchaseOrder order = orders.save(order());

		patchItems(order, 2, banana);

		LineItem item = loadSingleItem(order);
		assertThat(item.getQuantity()).isEqualTo(2);
		assertThat(item.getProduct().getId()).isEqualTo(banana.getId());
	}

	private void patchItems(PurchaseOrder order, int quantity, Product product) {

		String body = """
				{ "items" : [ { "quantity" : %d, "product" : "/products/%d" } ] }
				""".formatted(quantity, product.getId());

		assertThat(mvc.patch().uri("/purchaseOrders/{id}", order.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.hasStatus2xxSuccessful();
	}

	private LineItem loadSingleItem(PurchaseOrder order) {

		return tx.execute(status -> {
			LineItem item = orders.findById(order.getId()).orElseThrow().getItems().get(0);
			item.getProduct().getId();
			return item;
		});
	}

	private static Product product(String name) {

		Product product = new Product();
		product.setName(name);
		return product;
	}

	private static LineItem item(int quantity, Product product) {

		LineItem item = new LineItem();
		item.setQuantity(quantity);
		item.setProduct(product);
		return item;
	}

	private static PurchaseOrder order(LineItem... items) {

		PurchaseOrder order = new PurchaseOrder();
		order.getItems().addAll(java.util.List.of(items));
		return order;
	}
}
