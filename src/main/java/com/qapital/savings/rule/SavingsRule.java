package com.qapital.savings.rule;


import java.util.ArrayList;
import java.util.List;

/**
 * The core configuration object for a Savings Rule.
 */
public class SavingsRule {
	
	private Long id;
	private Long userId;

	private String placeDescription;
	private Double amount;
	private List<Long> savingsGoalIds;
	private RuleType ruleType;
	private Status status;
	
	public SavingsRule() {}

	public static SavingsRule createGuiltyPleasureRule(Long id, Long userId, String placeDescription, Double penaltyAmount) {
		SavingsRule guiltyPleasureRule = new SavingsRule();
        guiltyPleasureRule.setId(id);
		guiltyPleasureRule.setUserId(userId);
		guiltyPleasureRule.setPlaceDescription(placeDescription);
		guiltyPleasureRule.setAmount(penaltyAmount);
		guiltyPleasureRule.setSavingsGoalIds(new ArrayList<>());
		guiltyPleasureRule.setRuleType(RuleType.guiltypleasure);
		guiltyPleasureRule.setStatus(Status.active);
		return guiltyPleasureRule;
	}
	
	public static SavingsRule createRoundupRule(Long id, Long userId, Double roundupToNearest) {
		SavingsRule roundupRule = new SavingsRule();
        roundupRule.setId(id);
		roundupRule.setUserId(userId);
		roundupRule.setAmount(roundupToNearest);
		roundupRule.setSavingsGoalIds(new ArrayList<>());
		roundupRule.setRuleType(RuleType.roundup);
		roundupRule.setStatus(Status.active);
		return roundupRule;
	}

	public void addSavingsGoal(Long savingsGoalId) {
		if (!savingsGoalIds.contains(savingsGoalId)) {
			savingsGoalIds.add(savingsGoalId);
		}
	}

	public void removeSavingsGoal(Long savingsGoalId) {
		savingsGoalIds.remove(savingsGoalId);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

    public String getPlaceDescription() {
        return placeDescription;
    }

    public void setPlaceDescription(String placeDescription) {
        this.placeDescription = placeDescription;
    }

    public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public List<Long> getSavingsGoalIds() {
		return savingsGoalIds;
	}

	public void setSavingsGoalIds(List<Long> savingsGoalIds) {
		this.savingsGoalIds = savingsGoalIds;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isActive() {
		return Status.active.equals(getStatus());
	}
	
	public enum RuleType {
        guiltypleasure, roundup
	}
	
	public enum Status {
		active, deleted, paused
	}

}
