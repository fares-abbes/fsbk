package com.mss.backOffice.services;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

@Service
public class CryptOracleService {

  private static JdbcTemplate jdbcTemplate;
 private static final String CHECK_ACTIVE_APPL_QUERY = "SELECT porteur.my_decrypt(DE02_PAN,:KEY) FROM SWITCH.SWITCH";
  @PersistenceContext
  private EntityManager em;




 public List<String> checkApplicationActive(String key) {
    return (List<String> )em.createNativeQuery(CHECK_ACTIVE_APPL_QUERY)

        .setParameter("KEY", key)
        .getResultList();


  }
  public static String decryptCryptogramme(String pan, String key) {

    jdbcTemplate.setResultsMapCaseInsensitive(true);

    SimpleJdbcCall simpleJdbcCallFunction1 = new SimpleJdbcCall(jdbcTemplate).withFunctionName("MY_DECRYPT")
        .withSchemaName("PORTEUR");
    MapSqlParameterSource map = new MapSqlParameterSource();
    map.addValue("PAN", pan);
    map.addValue("KEY", key);

    String crypto = simpleJdbcCallFunction1.executeFunction(String.class, map);
    return (crypto);
  }



  public String getCryptogramme(String pan, String key) {

    jdbcTemplate.setResultsMapCaseInsensitive(true);

    SimpleJdbcCall simpleJdbcCallFunction1 = new SimpleJdbcCall(jdbcTemplate).withFunctionName("my_decrypt")
        .withSchemaName("PORTEUR");
    MapSqlParameterSource map = new MapSqlParameterSource();
    map.addValue("KEY", key);
    map.addValue("PAN", pan);
    String crypto = simpleJdbcCallFunction1.executeFunction(String.class, map);
    return (crypto);
  }

}
