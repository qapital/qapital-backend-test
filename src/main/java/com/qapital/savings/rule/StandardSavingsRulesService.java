package com.qapital.savings.rule;

import com.qapital.bankdata.transaction.Transaction;
import com.qapital.bankdata.transaction.TransactionsService;
import com.qapital.savings.event.SavingsEvent;
import com.qapital.savings.event.SavingsEvent.EventName;
import com.qapital.savings.rule.SavingsRule.RuleType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StandardSavingsRulesService implements SavingsRulesService {

    final TransactionsService transactionsService;

    @Autowired
    public StandardSavingsRulesService(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @Override
    public List<SavingsRule> activeRulesForUser(Long userId) {

        SavingsRule guiltyPleasureRule = SavingsRule
                .createGuiltyPleasureRule(1l, userId, "Starbucks", 3.00d);
        guiltyPleasureRule.addSavingsGoal(1l);
        guiltyPleasureRule.addSavingsGoal(2l);
        SavingsRule roundupRule = SavingsRule.createRoundupRule(2l, userId, 2.00d);
        roundupRule.addSavingsGoal(1l);

        return List.of(guiltyPleasureRule, roundupRule);
    }

    @Override
    public List<SavingsEvent> executeRule(SavingsRule savingsRule) {
// list of transactions
        List<Transaction> latestTransactionsForUser = transactionsService
                .latestTransactionsForUser(savingsRule.getUserId());

        List<SavingsEvent> savingsEventList = new ArrayList<SavingsEvent>();

        //finds negative expenses
        List<Transaction> expenseTransactions = latestTransactionsForUser.stream()
                .filter(transaction -> transaction.getAmount() < 0).collect(Collectors.toList());

        //applies rule to those transactions and adds them to saving list
        for (Transaction transaction : expenseTransactions) {
            savingsEventList.addAll(applyRoundOffRule(transaction, savingsRule));
            savingsEventList.addAll(applyGuiltyPleasureRule(transaction, savingsRule));
        }
        return savingsEventList;
    }

    //checks for description
    private static List<SavingsEvent> applyGuiltyPleasureRule(Transaction transaction,
            SavingsRule savingsRule) {

        if (transaction.getDescription().equals(savingsRule.getPlaceDescription())) {
            return ruleFunction(savingsRule, savingsRule.getAmount(), RuleType.guiltypleasure);
        }
        return List.of();

    }

    private static List<SavingsEvent> applyRoundOffRule(Transaction transaction,
            SavingsRule savingsRule) {
        Double savedAmount = roundUp(transaction.getAmount(), savingsRule.getAmount());
        return ruleFunction(savingsRule, savedAmount, RuleType.roundup);

    }

    //if multiple  goals  split the amount equally
    private static List<SavingsEvent> ruleFunction(SavingsRule savingsRule, Double amount,
            RuleType ruleType) {
        LocalDate localdate
                = LocalDate.ofInstant(
                Instant.now(),
                ZoneId.systemDefault());
        Double savedAmount = amount / savingsRule.getSavingsGoalIds().size();
        return savingsRule.getSavingsGoalIds().stream()
                .map(savingsGoalId -> {
                    SavingsEvent savingsEvent = new SavingsEvent();
                    savingsEvent.setId(savingsRule.getUserId());
                    savingsEvent.setAmount(savedAmount);
                    savingsEvent.setCreated(Instant.now());
                    savingsEvent.setSavingsRuleId(savingsRule.getId());
                    savingsEvent.setRuleType(ruleType);
                    savingsEvent.setSavingsGoalId(savingsGoalId);
                    savingsEvent.setUserId(savingsRule.getUserId());
                    savingsEvent.setEventName(EventName.rule_application);
                    savingsEvent.setDate(localdate);
                    return savingsEvent;
                })
                .collect(Collectors.toList());
    }

    //it rounds the amount on the transaction to the nearest multiple of the configured roundup amount and generates a value with the difference as the saved amount.
    private static Double roundUp(Double expense, Double configuredAmount) {
        return Math.ceil(expense / configuredAmount) * configuredAmount - expense;
    }
}


