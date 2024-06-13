package net.kdt.pojavlaunch.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public interface HashGenerator {
    HashGenerator SHA1_GENERATOR = DigestUtils::sha1;
    HashGenerator SHA256_GENERATOR = DigestUtils::sha256;
    byte[] generate(InputStream input) throws IOException;
}
