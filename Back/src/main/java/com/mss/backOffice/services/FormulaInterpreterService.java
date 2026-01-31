package com.mss.backOffice.services;

import org.springframework.stereotype.Service;

 import com.mss.backOffice.controller.FileRequest;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public strictfp class FormulaInterpreterService {
	public BigDecimal evaluateFormulawithBigDecimal(String formula ) {
		Stack<BigDecimal> values = new Stack<>();
		Stack<Character> operators = new Stack<>();
		int i = 0;
		while (i < formula.length()) {
			char c = formula.charAt(i);
			if (Character.isDigit(c) || c == '.') {
				StringBuilder num = new StringBuilder();
				while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) {
					num.append(formula.charAt(i));
					i++;
				}
				values.push(new BigDecimal(num.toString()));
				i--;
			} else if (c == '(') {
				int closingParenthesisIndex = findClosingParenthesisIndex(formula, i);
				String subExpression = formula.substring(i + 1, closingParenthesisIndex);
				values.push(evaluateFormulawithBigDecimal(subExpression));
				i = closingParenthesisIndex;
			} else if (c == ')') {
				throw new IllegalArgumentException("Unbalanced parentheses");
			} else if (c == '+' || c == '-' || c == '*' || c == '/') {
				while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
					values.push(applyOperatorBigdecimal(operators.pop(), values.pop(), values.pop()));
				}
				operators.push(c);
			}
			i++;
		}

		while (!operators.isEmpty()) {
			values.push(applyOperatorBigdecimal(operators.pop(), values.pop(), values.pop()));
		}

		return values.pop();
	}
	private BigDecimal applyOperatorBigdecimal(char operator, BigDecimal b, BigDecimal a) {
		switch (operator) {
		case '+':
			return  a.add(b) ;
		case '-':
	 
			return a.subtract(b);
		case '*':
			return a.multiply(b);
		case '/':
			if (b.floatValue() == 0) {
				throw new ArithmeticException("Division by zero");
			}
			return a.divide(b);
		}
		return new BigDecimal(0); // Invalid operator
	}
	public Float evaluateFormula(String formula, Map<String, Float> elements) {
		Stack<Float> values = new Stack<>();
		Stack<Character> operators = new Stack<>();
		int i = 0;
		while (i < formula.length()) {
			char c = formula.charAt(i);
			if (Character.isDigit(c) || c == '.') {
				StringBuilder num = new StringBuilder();
				while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) {
					num.append(formula.charAt(i));
					i++;
				}
				values.push(Float.parseFloat(num.toString()));
				i--;
			} else if (c == '(') {
				int closingParenthesisIndex = findClosingParenthesisIndex(formula, i);
				String subExpression = formula.substring(i + 1, closingParenthesisIndex);
				values.push(evaluateFormula(subExpression, elements));
				i = closingParenthesisIndex;
			} else if (c == ')') {
				throw new IllegalArgumentException("Unbalanced parentheses");
			} else if (c == '+' || c == '-' || c == '*' || c == '/') {
				while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
					values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
				}
				operators.push(c);
			}
			i++;
		}

		while (!operators.isEmpty()) {
			values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
		}

		return values.pop();
	}

	private boolean hasPrecedence(char op1, char op2) {
		if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
			return false;
		}
		return true;
	}

	private Float applyOperator(char operator, Float b, Float a) {
		switch (operator) {
		case '+':
			return a + b;
		case '-':
			return a - b;
		case '*':
			return a * b;
		case '/':
			if (b == 0) {
				throw new ArithmeticException("Division by zero");
			}
			return a / b;
		}
		return Float.valueOf(0); // Invalid operator
	}

	public List<String> extractElementsFromFormula(String formula) {
		// Use regular expressions to extract elements (variables) from the formula
		Pattern pattern = Pattern.compile("([A-Z0-9_]+)");
		Matcher matcher = pattern.matcher(formula);
		List<String> elements = new ArrayList<>();

		while (matcher.find()) {
			elements.add(matcher.group(1));
		}

		return elements;
	}

	public BigDecimal evaluateWithElementswithBigDecimal(String formula, Map<String,  BigDecimal> elements) {
		if ((formula == null) || (formula.isEmpty()) || ("".equals(formula.trim()))) {
			return new BigDecimal(0);
		}
		for (String element : elements.keySet()) {
//			DecimalFormat decimalFormat = new DecimalFormat("00000000000000000.00");

			// Format the float value
//			String formattedValue = String.format("%.2f", elements.get(element));
			String formattedValue =   elements.get(element).toString();
			formattedValue = formattedValue.replaceAll(",", ".");
			formula = formula.replace(element, formattedValue);
		}
//			FileRequest.print("formula without variables =>;" + formula+";", FileRequest.getLineNumber());

		return evaluateFormulawithBigDecimal(formula);
	}
	public Float evaluateWithElements(String formula, Map<String, Float> elements) {
		if ((formula == null) || (formula.isEmpty()) || ("".equals(formula.trim()))) {
			return 0f;
		}
		for (String element : elements.keySet()) {
//			DecimalFormat decimalFormat = new DecimalFormat("00000000000000000.00");
			
			// Format the float value
			String formattedValue = String.format("%.2f", elements.get(element));
			formattedValue = formattedValue.replaceAll(",", ".");
			formula = formula.replace(element, formattedValue);
		}
//			FileRequest.print("formula without variables =>;" + formula+";", FileRequest.getLineNumber());
		
		return evaluateFormula(formula, new HashMap<>());
	}

	private int findClosingParenthesisIndex(String formula, int startIndex) {
		int openParenthesesCount = 0;
		for (int i = startIndex; i < formula.length(); i++) {
			if (formula.charAt(i) == '(') {
				openParenthesesCount++;
			} else if (formula.charAt(i) == ')') {
				openParenthesesCount--;
				if (openParenthesesCount == 0) {
					return i;
				}
			}
		}
		throw new IllegalArgumentException("Unbalanced parentheses");
	}
}