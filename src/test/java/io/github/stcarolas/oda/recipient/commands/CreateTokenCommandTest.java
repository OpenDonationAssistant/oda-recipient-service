package io.github.stcarolas.oda.recipient.commands;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class CreateTokenCommandTest {

  @Test
  public void testCreateingKey()
    throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, IOException {
    // var keyFactory = KeyFactory.getInstance("RSA");
    // final RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
    //   new RSAPrivateKeySpec(BigInteger.valueOf(2048), BigInteger.valueOf(256))
    // );

    var pairGenerator = KeyPairGenerator.getInstance("RSA");
    pairGenerator.initialize(2048);
    var pair = pairGenerator.generateKeyPair();

    Base64.Encoder encoder = Base64.getEncoder();

    var privateKeyFile = new FileOutputStream("private.key");
    privateKeyFile.write("-----BEGIN PRIVATE KEY-----\n".getBytes());
    privateKeyFile.write(
      encoder.encodeToString(pair.getPrivate().getEncoded()).getBytes()
    );
    privateKeyFile.write("\n-----END PRIVATE KEY-----\n".getBytes());

    var publicKeyFile = new FileOutputStream("public.pub");
    publicKeyFile.write("-----BEGIN RSA PUBLIC KEY-----\n".getBytes());
    publicKeyFile.write(
      encoder.encodeToString(pair.getPublic().getEncoded()).getBytes()
    );
    publicKeyFile.write("\n-----END RSA PUBLIC KEY-----\n".getBytes());

    Algorithm algorithm = Algorithm.RSA256(
      (RSAPublicKey) pair.getPublic(),
      (RSAPrivateKey) pair.getPrivate()
    );

    String token = JWT.create().withIssuer("auth0").sign(algorithm);

    System.out.println(token);

    JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();

    var decodedJWT = verifier.verify(token);
    System.out.println(decodedJWT.getIssuer());
  }
}
