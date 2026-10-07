# Reproducer for spring-projects/spring-data-rest#2596

A merge patch (`PATCH` with `application/json`) does not update a linkable association of an element that already exists in an array.

`PurchaseOrder` has an `@ElementCollection` of `LineItem` embeddables, each with a `@ManyToOne` to the exported `Product`. Sending

```
PATCH /purchaseOrders/{id}
{ "items" : [ { "quantity" : 2, "product" : "/products/{banana}" } ] }
```

to an order whose single item is `{ "quantity" : 1, "product" : apple }` changes `quantity` to 2 but keeps `apple`. Sending the same body to an order without items stores `banana` as expected.

```
./mvnw test
```

`MergePatchTests.patchUpdatesAssociationOfExistingArrayElement` fails, `patchSetsAssociationOfAppendedArrayElement` passes (Spring Boot 4.1.1 / Spring Data REST 5.1.1).
