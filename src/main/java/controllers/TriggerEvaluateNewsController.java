package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import services.NewsScheduler;

@Controller
public class TriggerEvaluateNewsController {

    private final NewsScheduler newsScheduler;

    @Autowired
    public TriggerEvaluateNewsController(NewsScheduler newsScheduler) {
        this.newsScheduler = newsScheduler;
    }

    @PostMapping("/api/triggerEvaluateNews")
    public String triggerEvaluateNews(Model model) {
        newsScheduler.evaluateAndStoreNewsEvery12Hours(); // Přímo voláme metodu
        model.addAttribute("evaluateTriggered", true);
        return "index"; // Vraťte název stránky
    }

    @GetMapping("/triggerEvaluatePage")
    public String showTriggerPage() {
        return "index";
    }
}