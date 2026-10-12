package dev.emreozcan.pazarsync.mock;

import java.util.List;

public final class Catalog {

    public record Product(String barcode, String name, int quantity, long salePrice) {
    }

    public static final List<Product> PRODUCTS = List.of(
            new Product("BARCODE-MUG", "Stoneware mug", 40, 24900),
            new Product("BARCODE-TEE", "Cotton tee", 12, 39900),
            new Product("BARCODE-BAG", "Canvas tote", 5, 54900));

    private Catalog() {
    }
}
