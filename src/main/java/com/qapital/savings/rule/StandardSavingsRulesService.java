package com.qapital.savings.rule;

import com.qapital.bankdata.transaction.Transaction;
import com.qapital.bankdata.transaction.TransactionsService;
import com.qapital.savings.event.SavingsEvent;
import com.qapital.savings.rule.SavingsRule.RuleType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StandardSavingsRulesService implements SavingsRulesService {

    private TransactionsService transactionsService = null;

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

        //finds negative expense
        List<Transaction> expenseTransactions = latestTransactionsForUser.stream()
                .filter(transaction -> transaction.getAmount() < 0).collect(
                        Collectors.toList());

        //applies rule to those transactions and adds them to saving list
        for (int i = 0; i < expenseTransactions.size() ; i++) {
            Transaction transaction = expenseTransactions.get(i);
            SavingsEvent roundOffRuleEvent = applyRoundOffRule(transaction, savingsRule);
            SavingsEvent guiltyPleasureRuleEvent = applyGuiltyPleasureRule(transaction, savingsRule);
            savingsEventList.add(roundOffRuleEvent);
            savingsEventList.add(guiltyPleasureRuleEvent);

        }
        return savingsEventList;
    }

    //checks for description, then goals and if multiple  goals  present splits the amount equally amongst them
    private SavingsEvent applyGuiltyPleasureRule(Transaction transaction, SavingsRule savingsRule) {
        SavingsEvent savingsEvent = new SavingsEvent();
        if (transaction.getDescription().equals(savingsRule.getPlaceDescription())) {
            savingsEvent.setRuleType(RuleType.guiltypleasure);
            ruleFunction(transaction, savingsRule, savingsRule.getAmount());
        }

        return savingsEvent;
    }

    private SavingsEvent applyRoundOffRule(Transaction transaction, SavingsRule savingsRule) {
        Double savedAmount = roundUp(transaction.getAmount(), savingsRule.getAmount());
        SavingsEvent savingsEvent = new SavingsEvent();
        //can we pass on rule type in arguments?
    savingsEvent.setRuleType(RuleType.roundup);
        ruleFunction(transaction, savingsRule, savedAmount);
        return savingsEvent;
    }


    SavingsEvent ruleFunction(Transaction transaction, SavingsRule savingsRule, Double amount) {
        if (savingsRule.getSavingsGoalIds().size() > 1) {
            Double savedAmount =
                    amount / savingsRule.getSavingsGoalIds().size();
            for (int j = 0; j < savingsRule.getSavingsGoalIds().size(); j++) {
                SavingsEvent savingsEvent = new SavingsEvent();
                savingsEvent.setSavingsGoalId(savingsRule.getSavingsGoalIds().get(j));
                savingsEvent.setAmount(savedAmount);
                savingsEvent.setSavingsRuleId(savingsRule.getId());

            }
        } else {
            SavingsEvent savingsEvent = new SavingsEvent();
            savingsEvent.setUserId(transaction.getUserId());
            savingsEvent.setAmount(amount);
            savingsEvent.setSavingsGoalId(savingsRule.getSavingsGoalIds().get(0));
            return savingsEvent;
        }
        return null;
    }

    //it rounds the amount on the transaction to the nearest multiple of the configured roundup amount and generates a value with the difference as the saved amount.
    public double roundUp(Double expense, Double configuredAmount) {
        double roundedAmount = expense >= 0 ? ((expense + configuredAmount - 1) / configuredAmount)
                * configuredAmount : (expense / configuredAmount) * configuredAmount;
        return (expense - roundedAmount);
    }
}


