package org.baeldung.web.controller;

import javax.validation.Valid;
import org.baeldung.persistence.model.User;
import org.baeldung.persistence.service.EmailExistsException;
import org.baeldung.persistence.service.UserDto;
import org.baeldung.persistence.service.UserService;
import org.baeldung.persistence.service.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class RegistrationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);
    private UserService service;
    @Autowired
    private MessageSource messages;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserValidator validator;
    
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(this.validator);
    }
    @Autowired
    public RegistrationController(UserService service) {
        this.service = service;
    }
    
    @RequestMapping(value = "/user/registration", method = RequestMethod.GET)
    public String showRegistrationForm(WebRequest request, Model model) {
        LOGGER.debug("Rendering registration page.");
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }
  
    @RequestMapping(value = "/user/registration", method = RequestMethod.POST)
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid UserDto userAccountData, BindingResult result, WebRequest request, Errors errors) {
        boolean goodEmailCheck = validator.validateEmail(userAccountData.getUsername());
        if (!goodEmailCheck)
            result.rejectValue("username", "message.badEmail");
        User registered = null;
        if (!result.hasErrors())
            registered = createUserAccount(userAccountData, result);
        if (registered == null && !userAccountData.getUsername().isEmpty() && goodEmailCheck) {
            result.rejectValue("username", "message.regError");
        }
        if (result.hasErrors()) {
            return new ModelAndView("registration", "user", userAccountData);
        } else {
            return new ModelAndView("successRegister", "user", userAccountData);
        }

    }
    
    private User createUserAccount(UserDto userAccountData, BindingResult result) {
        User registered = null;
        try {
            registered = service.registerNewUserAccount(userAccountData);
        } catch (EmailExistsException e) {
            return null;
        }    
        return registered;
    } 
}
