package com.qapital.savings.rule;

import com.qapital.savings.event.SavingsEvent;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/savings/rule")
public class SavingsRulesController {

    private final SavingsRulesService savingsRulesService;

    @Autowired
    public SavingsRulesController(SavingsRulesService savingsRulesService) {
        this.savingsRulesService = savingsRulesService;
    }

    @GetMapping("/active/{userId}")
    public List<SavingsRule> activeRulesForUser(@PathVariable Long userId) {
        return savingsRulesService.activeRulesForUser(userId);
    }


    @PostMapping(
            value = "/createSavings", consumes = "application/json", produces = "application/json")
    public List<SavingsEvent> savings(@RequestBody SavingsRule savingsRule) {
        return savingsRulesService.executeRule(savingsRule);
    }


}
