package com.gmail.kramarenko104.service;

import com.gmail.kramarenko104.model.Cart;

import java.util.List;

public interface CartService {

    long createCart(Cart cart);

    Cart getCartByUserId(long userId);

    void addProduct(long cartId, long productId, int quantity);

    void removeProduct(long userId, long productId, int quantity);

    void deleteCartByUserId(long Id);

    List<Cart> getAllCartsByUserId(long userId);

    List<Cart> getAllCarts();

}
