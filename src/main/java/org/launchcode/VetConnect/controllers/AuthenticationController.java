package org.launchcode.VetConnect.controllers;

import org.launchcode.VetConnect.models.User;
import org.launchcode.VetConnect.models.dto.LoginFormDTO;
import org.launchcode.VetConnect.models.dto.RegisterFormDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class AuthenticationController extends VetConnectController{

    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute("user", user.getId());
    }

    @GetMapping("/register")
    public String displayRegistrationForm(Model model, HttpServletRequest request) {
        User user = getUserFromSession(request.getSession(false));

        if(!(user == null)) {
            return "redirect:dashboard";
        }

        model.addAttribute(new RegisterFormDTO());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid RegisterFormDTO registerFormDTO,
                                          Errors errors, HttpServletRequest request,
                                          Model model) {

        if (errors.hasErrors()) {
//            model.addAttribute("title", "Register");
            return "register";
        }

        User existingUser = userRepository.findByEmailAddress(registerFormDTO.getEmailAddress());

        if (existingUser != null) {
            errors.rejectValue("emailAddress", "emailAddress.alreadyexists", "A user with that email already exists");
//            model.addAttribute("title", "Register");
            return "register";
        }

        String password = registerFormDTO.getPassword();
        String verifyPassword = registerFormDTO.getVerifyPassword();
        if (!password.equals(verifyPassword)) {
            errors.rejectValue("password", "passwords.mismatch", "Passwords do not match");
          //  model.addAttribute("title", "Register");
            return "register";
        }

        User newUser = new User(registerFormDTO.getUserType(), registerFormDTO.getFirstName(), registerFormDTO.getLastName(), registerFormDTO.getEmailAddress(), registerFormDTO.getPassword());
        userRepository.save(newUser);
        setUserInSession(request.getSession(), newUser);

        return "redirect:dashboard";
    }

    @GetMapping(value="login")
    public String displayLoginForm(Model model, HttpServletRequest request) {
        User user = getUserFromSession(request.getSession(false));

        if(!(user == null)) {
            return "redirect:dashboard";
        }

        model.addAttribute(new LoginFormDTO());
        return "login";
    }

    @PostMapping("/login")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO,
                                   Errors errors, HttpServletRequest request,
                                   Model model) {

        if (errors.hasErrors()) {
            //model.addAttribute("title", "Log In");
            return "login";
        }

        User theUser = userRepository.findByEmailAddress(loginFormDTO.getEmailAddress());

        if (theUser == null) {
            errors.rejectValue("emailAddress", "user.invalid", "The given email address does not exist");
            //model.addAttribute("title", "Log In");
            return "login";
        }

        String password = loginFormDTO.getPassword();

        if (!theUser.isMatchingPassword(password)) {
            errors.rejectValue("password", "password.invalid", "Invalid password");
           // model.addAttribute("title", "Log In");
            return "login";
        }

        setUserInSession(request.getSession(), theUser);

        return "redirect:dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        return "redirect:login";
    }



}
