package com.mss.backOffice.security.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClock;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.mss.backOffice.request.UserPrincipal;
import com.mss.unified.entities.Role;
import java.util.Date;
import java.util.function.Function;


@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private Clock clock = DefaultClock.INSTANCE;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpiration;
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
    }

    public String createToken(String username, Set<Role> set ) {
        Claims claims = Jwts.claims().setSubject(username);
//        claims.put("roles", set);
        Date now = new Date();
        Date validity = calculateExpirationDate(now);
        return Jwts.builder()//
            .setClaims(claims)//
            .setIssuedAt(now)//
            .setExpiration(validity)//
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal =(UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = calculateExpirationDate(now);
        return Jwts.builder()
            .setSubject(userPrincipal.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    public String generateRefreshToken(String token) {
        final Date createdDate = clock.now();
        final Date expirationDate = calculateExpirationRefreshDate(createdDate);
        final Claims claims = getAllClaimsFromToken(token);
        System.out.println(claims.getSubject());
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);
        return Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }


    public String refreshToken(String token) {
        final Date createdDate = clock.now();
        final Date expirationDate = calculateExpirationDate(createdDate);
        final Claims claims = getAllClaimsFromToken(token);
        System.out.println(claims.getSubject());
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);
        return Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }



    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(clock.now());
    }

    private Boolean ignoreTokenExpiration(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getIssuedAtDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset) && (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }


    private Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + jwtExpiration  );
    }

    private Date calculateExpirationRefreshDate(Date createdDate) {
        return new Date(createdDate.getTime() + jwtExpiration );
    }

    public String getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken)
    {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Expired or invalid JWT token");
            return false ;
        }
    }



}
