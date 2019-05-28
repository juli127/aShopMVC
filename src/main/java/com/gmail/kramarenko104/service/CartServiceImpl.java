package com.gmail.kramarenko104.service;

import com.gmail.kramarenko104.dao.CartDaoImpl;
import com.gmail.kramarenko104.dao.ProductDaoImpl;
import com.gmail.kramarenko104.model.Cart;
import com.gmail.kramarenko104.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartDaoImpl cartDao;

    @Autowired
    private ProductDaoImpl productDao;

    private static Logger logger = LoggerFactory.getLogger(CartService.class);

    public void setDaos(CartDaoImpl cartDao, ProductDaoImpl productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    @Override
    public Cart createCart(long userId) {
        return cartDao.createCart(userId);
    }

    public Cart getCartByUserId(long userId) {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart != null && cart.getProducts().size() > 0) {
            cart = recalculateCart(cart);
        }
        return cart;
    }

    @Override
    public void addProduct(long userId, long productId, int quantity) {
        cartDao.addProduct(userId, productDao.get(productId), quantity);
    }

    @Override
    public void removeProduct(long userId, long productId, int quantity) {
        cartDao.removeProduct(userId, productDao.get(productId), quantity);
    }

    @Override
    public void clearCartByUserId(long userId) {
        cartDao.clearCartByUserId(userId);
    }

    public boolean isDbConnected() {
        return cartDao.isDbConnected();
    }

    private Cart recalculateCart(Cart cart) {
//        System.out.println(">>>>CartServiceImpl.recalculateCart ..enter with cart: cart: id:"
//                + cart.getCartId() + ", count:" + cart.getItemsCount() + ", sum: " + cart.getTotalSum());
        Map<Product, Integer> productsInCart = cart.getProducts();
        int itemsCount = 0;
        int totalSum = 0;
        if (productsInCart.size() > 0) {
            int quantity = 0;
            for (Map.Entry<Product, Integer> entry : productsInCart.entrySet()) {
                quantity = entry.getValue();
                itemsCount += quantity;
                totalSum += quantity * entry.getKey().getPrice();
            }
        }
        if (itemsCount > 0) {
            cart.setItemsCount(itemsCount);
            cart.setTotalSum(totalSum);
        }
//        System.out.println(">>>>CartServiceImpl.recalculateCart ..upd cart: id:"
//                + cart.getCartId() + ", count:" + cart.getItemsCount() + ", sum: " + cart.getTotalSum());
        return cart;
    }
}
