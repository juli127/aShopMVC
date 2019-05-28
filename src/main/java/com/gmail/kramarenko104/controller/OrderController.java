package com.gmail.kramarenko104.controller;

import com.gmail.kramarenko104.dto.OrderDto;
import com.gmail.kramarenko104.model.Cart;
import com.gmail.kramarenko104.model.Order;
import com.gmail.kramarenko104.model.User;
import com.gmail.kramarenko104.service.CartServiceImpl;
import com.gmail.kramarenko104.service.OrderServiceImpl;
import com.gmail.kramarenko104.service.UserServiceImpl;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.persistence.EntityManagerFactory;

@Controller
@RequestMapping("/order")
@SessionAttributes(value = {"warning", "user"})
public class OrderController {

    private final static Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final String DB_WARNING = "Check your connection to DB!";

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    EntityManagerFactory emf;

    public OrderController() {
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView modelAndView,
                              @ModelAttribute(name = "user") User user) {
        modelAndView.setViewName("order");
        Cart cart = cartService.getCartByUserId(user.getUserId());
        Order order = orderService.getLastOrderByUserId(user.getUserId());
        modelAndView.addObject("user", user);
        modelAndView.addObject("cart", cart);
        modelAndView.addObject("order", order);
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    String doPost(ModelAndView modelAndView,
                  @RequestParam("action") String action,
                  @RequestParam("userId") long userId) {
        String jsondata = null;
        modelAndView.setViewName("order");
        System.out.println("OrderController.POST: .......enter......CREATE NEW ORDER ........................");

        if (emf != null) {
            User dbUser = userService.getUser(userId);
            if (dbUser != null) {
                modelAndView.addObject("user", dbUser);

                // getProduct info from Ajax POST request (updateCart.js)
                if (action != null && (action.equals("makeOrder"))) {
                    System.out.println("OrderController.POST: got userId from POST request: " + userId);

                    // any user can have only one existing now cart and many processed orders (userId uniquely identifies cart)
                    Cart cart = cartService.getCartByUserId(userId);
                    System.out.println("OrderController.POST: got cart from DB: " + cart);

                    // order will be created based on the cart's content
                    Order newOrder = orderService.createOrder(userId, cart.getProducts());

                    // transfer DTO Order for view, not complex Order
                    OrderDto jsonOrder = new OrderDto(userId);
                    jsonOrder.setOrderNumber(newOrder.getOrderNumber());
                    jsonOrder.setProducts(newOrder.getProducts());
                    jsonOrder.setItemsCount(newOrder.getItemsCount());
                    jsonOrder.setTotalSum(newOrder.getTotalSum());
                    System.out.println("OrderController.POST: !!! new Order was created: " + jsonOrder);
                    modelAndView.addObject("order", newOrder);

                    // send JSON back with the new Order to show on order.jsp
                    if (jsonOrder != null) {
                        jsondata = new GsonBuilder().setPrettyPrinting().create().toJson(jsonOrder);
                        System.out.println("OrderController.POST: send JSON data to cart.jsp ---->" + jsondata);
                    }
                    cartService.clearCartByUserId(userId);
                    modelAndView.addObject("cart", null);
                }
            }
        } else { // connection to DB is closed
            modelAndView.addObject("warning", DB_WARNING);
        }
//        System.out.println(">>>>>>>>>>OrderController.POST:  exit .. cart: " + modelAndView.getModel().get("cart"));
//        System.out.println(">>>>>>>>>>OrderController.POST:  exit .. order: " + modelAndView.getModel().get("order"));
        return jsondata;
    }

    @ModelAttribute("cart")
    public Cart getCart() {
        System.out.println("!!!!! OrderController.getCart... !!!!!");
//        Thread.dumpStack();
        return new Cart();

    }

    @ModelAttribute("order")
    public Order getOrder() {
        System.out.println("!!!!! OrderController.getOrder... !!!!!");
//        Thread.dumpStack();
        return new Order();

    }

}
