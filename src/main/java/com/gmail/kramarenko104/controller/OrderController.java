package com.gmail.kramarenko104.controller;

import com.gmail.kramarenko104.factoryDao.DaoFactory;
import com.gmail.kramarenko104.model.Cart;
import com.gmail.kramarenko104.model.Order;
import com.gmail.kramarenko104.service.CartService;
import com.gmail.kramarenko104.service.OrderService;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
public class OrderController {

    private static Logger logger = Logger.getLogger(OrderController.class);

    @Autowired
    private DaoFactory daoFactory;

    public OrderController() {
    }

    @RequestMapping(method = RequestMethod.GET)
    protected String doGet() {
        return "order";
    }

    @RequestMapping(method = RequestMethod.POST, produces="text/json")
    protected String doPost(@RequestParam("action") String action,
                            @RequestParam("userId") int userId,
                            Model model) {
        daoFactory.openConnection();
        boolean needRefresh = false;
        String jsondata = null;

        if (model.asMap().get("user") != null) {
            // get info from Ajax POST request (updateCart.js)
            if (action != null && (action.equals("makeOrder"))) {
                logger.debug("OrderServlet.POST: got userId from POST request: " + userId);

                // any user can have only one existing now cart and many processed orders (userId uniquely identifies cart)
                CartService cartService = daoFactory.getCartService();
                Cart cart = cartService.getCart(userId);
                logger.debug("OrderServlet.POST: got cart from DB: " + cart);

                // order will be created based on the cart's content
                OrderService orderService = daoFactory.getOrderService();
                Order newOrder = orderService.createOrder(userId, cart.getProducts());
                logger.debug("OrderServlet.POST: !!! new Order was created: " + newOrder);
                model.addAttribute("newOrder", newOrder);

                // send JSON back with the new Order to show on order.jsp
                if (newOrder != null) {
                    jsondata = new Gson().toJson(newOrder);
                    logger.debug("OrderServlet: send JSON data to cart.jsp ---->" + jsondata);
                }
                cartService.deleteCart(Integer.valueOf(userId));
                model.addAttribute("userCart", null);
                daoFactory.deleteCartService(cartService);
                daoFactory.deleteOrderService(orderService);
            }
        }
        daoFactory.closeConnection();
        return jsondata;
    }
}
