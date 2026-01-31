package com.mss.backOffice.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.stereotype.Service;


@Service
public class PasswordService {

  public boolean isPasswordValid(String password) {


    String regExpn =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    CharSequence inputStr = password;

    Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputStr);

    return(matcher.matches());

  }

  public String generatePassayPassword() {
    PasswordGenerator gen = new PasswordGenerator();
    CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
    CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
    lowerCaseRule.setNumberOfCharacters(2);

    CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
    CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
    upperCaseRule.setNumberOfCharacters(2);

    CharacterData digitChars = EnglishCharacterData.Digit;
    CharacterRule digitRule = new CharacterRule(digitChars);
    digitRule.setNumberOfCharacters(2);

    CharacterData specialChars = new CharacterData() {
      public String getErrorCode() {
        return "ERRONEOUS_SPECIAL_CHARS ";
      }

      public String getCharacters() {
        return "!@#$%^&*()_+";
      }
    };
    CharacterRule splCharRule = new CharacterRule(specialChars);
    splCharRule.setNumberOfCharacters(2);

    return gen.generatePassword(10, splCharRule, lowerCaseRule,
        upperCaseRule, digitRule);
  }

}
