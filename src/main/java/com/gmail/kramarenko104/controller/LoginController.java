package com.gmail.kramarenko104.controller;

import com.gmail.kramarenko104.model.Cart;
import com.gmail.kramarenko104.model.Order;
import com.gmail.kramarenko104.model.User;
import com.gmail.kramarenko104.service.CartServiceImpl;
import com.gmail.kramarenko104.service.OrderServiceImpl;
import com.gmail.kramarenko104.service.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManagerFactory;

@Controller
@SessionAttributes(value = {"showLoginForm", "message", "attempt", "order", "user", "login", "startTime", "cart", "isAdmin", "warning"})
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int WAIT_SECONDS_BEFORE_LOGIN_FORM_RELOAD = 15;
    private static final String DB_WARNING = "Check your connection to DB!";
    private static final String adminLog = "admin";
    private ModelAndView modelAndView;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private EntityManagerFactory emf;

    public LoginController() {
    }

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    protected ModelAndView doGet(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        System.out.println("LoginController.doGet:   enter.. user: " + modelAndView.getModel().get("user"));
        modelAndView.addObject("user", null);
        modelAndView.addObject("cart", null);
        modelAndView.addObject("order", null);
        modelAndView.addObject("message", null);
        modelAndView.addObject("attempt", null);
        modelAndView.addObject("showLoginForm", true);
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView doPost(ModelAndView modelAndView,
                                  @RequestParam("login") String login,
                                  @RequestParam("password") String pass) {
        String viewToGo = "login";
        boolean showLoginForm = true;
        boolean accessGranted = false;
        StringBuilder msgText = new StringBuilder();
        boolean isAdmin = false;
        User currentUser = null;
        int attempt;
//        modelAndView = new ModelAndView();

        // Connection to DB is open
        if (emf != null) {
            Object attemptObj = modelAndView.getModel().get("attempt");
            attempt = (attemptObj == null) ? 0 : (int) attemptObj;

            // already logged in
            if (modelAndView.getModel().get("user") != null) {
                currentUser = (User) modelAndView.getModel().get("user");
                accessGranted = true;
            } // not logged in yet
            else {
                long waitTime = 0;

                if ((login != null) && !("".equals(login))) {
                    modelAndView.addObject("login", login);
                    currentUser = userService.getUserByLogin(login);
                    boolean exist = (currentUser != null);

                    if (exist) {
                        String passVerif = userService.hashString(pass);
                        accessGranted = (currentUser.getPassword().equals(passVerif));
                        showLoginForm = !accessGranted && attempt < MAX_LOGIN_ATTEMPTS;
                        System.out.println("LoginServlet: JULIA: currentUser: " + currentUser);

                        if (accessGranted) {
                            attempt = 0;
                            showLoginForm = false;
                            modelAndView.addObject("user", currentUser);
                            modelAndView.addObject("login", null);
                            logger.debug("LoginServlet: User " + currentUser.getName() + " was registered and passed autorization");
                            if (adminLog.equals(login) && userService.getUserByLogin(adminLog).getPassword().equals(passVerif)) {
                                isAdmin = true;
                            }
                        } else {
                            attempt++;
                            if (attempt >= MAX_LOGIN_ATTEMPTS) {
                                if (attempt == MAX_LOGIN_ATTEMPTS) {
                                    modelAndView.addObject("startTime", System.currentTimeMillis());
                                }
                                waitTime = WAIT_SECONDS_BEFORE_LOGIN_FORM_RELOAD - (System.currentTimeMillis() - (Long) modelAndView.getModel().get("startTime")) / 1000;
                                if (waitTime > 0) {
                                    msgText.append("<br><font size=3 color='red'><b> Attempts' limit is exceeded. Login form will be available in " + waitTime + " seconds</b></font>");
                                    showLoginForm = false;
                                } else {
                                    attempt = 0;
                                    showLoginForm = true;
                                }
                            } else if (attempt >= 0) {
                                msgText.append("<b><font size=3 color='red'>Wrong password, try again! You have 3 attempts. (attempt #" + attempt + ")</font>");
                            }
                        }
                    } else {
                        attempt = 0;
                        showLoginForm = false;
                        msgText.append("<br>This user wasn't registered yet. <a href='registration'>Register, please,</a> or <a href='login'>login</a>");
                    }
                } else {
                    attempt = 0;
                }
            }

            // for authorized user getProduct the corresponding shopping Cart
            if (accessGranted) {
                showLoginForm = false;
                Cart userCart = cartService.getCartByUserId(currentUser.getUserId());
                System.out.println("LoginController:  GOT userCart == " + userCart);
                modelAndView.addObject("cart", userCart);

                Order userOrder = orderService.getLastOrderByUserId(currentUser.getUserId());
                System.out.println("LoginController:  GOT userOrder == " + userOrder);
                modelAndView.addObject("order", userOrder);

                if (userCart != null && userCart.getItemsCount() > 0) {
                    System.out.println("LoginController: cart is not empty, goto cart.jsp");
                    viewToGo = "cart";
                } else if (userOrder != null && userOrder.getItemsCount() > 0) {
                    System.out.println("LoginController: order is not empty, goto cart.jsp");
                    viewToGo = "order";
                } else {
                    System.out.println("LoginController: cart is empty, order is empty, goto product.jsp");
                    viewToGo = "product";
                }
            }
            modelAndView.addObject("showLoginForm", showLoginForm);
            modelAndView.addObject("message", msgText.toString());
            modelAndView.addObject("attempt", attempt);
            modelAndView.addObject("isAdmin", isAdmin);
        } else { // connection to DB is closed
            modelAndView.addObject("warning", DB_WARNING);
        }

        if (!"login".equals(viewToGo)) {
            viewToGo = "redirect:" + viewToGo;
        }
        modelAndView.setViewName(viewToGo);

        return modelAndView;
    }
}
