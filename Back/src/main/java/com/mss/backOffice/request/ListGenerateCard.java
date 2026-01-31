package com.mss.backOffice.request;

import java.util.List;

public class ListGenerateCard {
	private List<GenerateCardPrep> generateCard;

	public List<GenerateCardPrep> getGenerateCard() {
		return generateCard;
	}

	public void setGenerateCard(List<GenerateCardPrep> generateCard) {
		this.generateCard = generateCard;
	}

	@Override
	public String toString() {
		return "ListGenerateCard{" +
				"generateCard=" + generateCard +
				'}';
	}
}
