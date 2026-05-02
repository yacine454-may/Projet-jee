package com.jeu.reflexion.controller;

import com.jeu.reflexion.model.Player;
import com.jeu.reflexion.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final PlayerService playerService;

    public AuthController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("playerId") != null) return "redirect:/home";
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session, @RequestParam(required = false) String registered, Model model) {
        if (session.getAttribute("playerId") != null) return "redirect:/home";
        if (registered != null) model.addAttribute("success", "Compte créé ! Connectez-vous.");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session, Model model) {
        Optional<Player> player = playerService.login(username, password);
        if (player.isPresent()) {
            session.setAttribute("playerId", player.get().getId());
            session.setAttribute("playerUsername", player.get().getUsername());
            return "redirect:/home";
        }
        model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(HttpSession session) {
        if (session.getAttribute("playerId") != null) return "redirect:/home";
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirm,
                           Model model) {
        if (!password.equals(confirm)) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas");
            return "register";
        }
        try {
            playerService.register(username, password);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
