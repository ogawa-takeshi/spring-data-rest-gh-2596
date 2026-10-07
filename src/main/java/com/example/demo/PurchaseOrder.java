package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OrderColumn;

@Entity
public class PurchaseOrder {

	@Id @GeneratedValue Long id;
	@ElementCollection @OrderColumn List<LineItem> items = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public List<LineItem> getItems() {
		return items;
	}

	public void setItems(List<LineItem> items) {
		this.items = items;
	}
}
